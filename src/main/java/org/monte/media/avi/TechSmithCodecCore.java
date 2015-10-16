/*
 * @(#)TechSmithCodecCore.java 
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.avi;

import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import org.monte.media.io.UncachedImageInputStream;
import java.io.ByteArrayInputStream;
import org.monte.media.AbstractVideoCodecCore;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.io.ByteArrayImageOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import static java.lang.Math.*;

/**
 * {@code TechSmithCodec} (tscc) encodes a BufferedImage as a byte[] array.
 * <p>
 * This codec does not encode the color palette of an image. This must be done
 * separately.
 * <p>
 * Supported input formats:
 * <ul>
 * {@code Format} with {@code BufferedImage.class}, any width, any height,
 * depth=8,16 or 24.
 * </ul>
 * Supported output formats:
 * <ul>
 * {@code Format} with {@code byte[].class}, same width and height as input
 * format, depth=8,16 or 24.
 * </ul>
 * The codec supports lossless delta- and key-frame encoding of images with 8, 16 or
 * 24 bits per pixel.
 * <p>
 * Compression of a frame is performed in two steps: In the first, step
 * a frame is compressed line by line from bottom to top. In the second step
 * the resulting data is compressed again using zlib compression.
 * <p>
 * Apart from the second compression step and the support for 16- and 24-bit
 * data, this encoder is identical to the {@link RunLengthCodec}.
 * <p>
 * Each line of a frame is compressed individually. A line consists of two-byte
 * op-codes optionally followed by data. The end of the line is marked with
 * the EOL op-code.
 * <p>
 * The following op-codes are supported:
 * <ul>
 * <li>{@code 0x00 0x00}
 * <br>Marks the end of a line.</li>
 *
 * <li>{@code  0x00 0x01}
 * <br>Marks the end of the bitmap.</li>
 *
 * <li>{@code 0x00 0x02 dx dy}
 * <br> Marks a delta (skip). {@code dx} and {@code dy}
 * indicate the horizontal and vertical offset from the current position.
 * {@code dx} and {@code dy} are unsigned 8-bit values.</li>
 *
 * <li>{@code 0x00 n pixel{n} 0x00?}
 * <br> Marks a literal run. {@code n}
 * gives the number of 8-, 16- or 24-bit pixels that follow.
 * {@code n} must be between 3 and 255.
 * If n is odd and 8-bit pixels are used, a pad byte with the value 0x00 must be
 * added.
 * </li>
 * <li>{@code n pixel}
 * <br> Marks a repetition. {@code n}
 * gives the number of times the given pixel is repeated. {@code n} must be
 * between 1 and 255.
 * </li>
 * </ul>
 * Example:
 * <pre>
 * Compressed data         Expanded data
 *
 * 03 04                   04 04 04
 * 05 06                   06 06 06 06 06
 * 00 03 45 56 67 00       45 56 67
 * 02 78                   78 78
 * 00 02 05 01             Move 5 right and 1 down
 * 02 78                   78 78
 * 00 00                   End of line
 * 09 1E                   1E 1E 1E 1E 1E 1E 1E 1E 1E
 * 00 01                   End of RLE bitmap
 * </pre>
 *
 * References:<br/>
 * <a href="http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec"
 * >http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec</a><br>
 *
 * <p><b>Palette colors</b></p>
 * <p>In an AVI file, palette changes are stored in chunks with id's with the
 * suffix "pc". "pc" chunks contain an AVIPALCHANGE struct as shown below.
 * </p>
 * <pre>
 * /* ------------------
 *  * AVI Palette Change
 *  * ------------------
 *  * /
 * 
 * // Values for this enum have been taken from:
 * // http://biodi.sdsc.edu/Doc/GARP/garp-1.1/define.h
 * enum {
 *     PC_EXPLICIT = 0x02,
 *     // Specifies that the low-order word of the logical palette entry 
 *     // designates a hardware palette index. This flag allows the application to 
 *     // show the contents of the display device palette.
 *     PC_NOCOLLAPSE = 0x04,
 *     // Specifies that the color be placed in an unused entry in the system 
 *     // palette instead of being matched to an existing color in the system 
 *     // palette. If there are no unused entries in the system palette, the color 
 *     // is matched normally. Once this color is in the system palette, colors in
 *     // other logical palettes can be matched to this color.
 *     PC_RESERVED = 0x01
 *     // Specifies that the logical palette entry be used for palette animation. 
 *     // This flag prevents other windows from matching colors to the palette 
 *     // entry since the color frequently changes. If an unused system-palette
 *     // entry is available, the color is placed in that entry. Otherwise, the 
 *     // color is not available for animation.
 * } peFlagsEnum;
 * /* 
 *  * The PALETTEENTRY structure specifies the color and usage of an entry in a
 *  * logical palette. A logical palette is defined by a LOGPALETTE structure.
 *  * /
 * typedef struct { 
 *   BYTE peRed; // Specifies a red intensity value for the palette entry.
 *   BYTE peGreen; // Specifies a green intensity value for the palette entry.
 *   BYTE peBlue; // Specifies a blue intensity value for the palette entry.
 *   BYTE enum peFlagsEnum peFlags; // Specifies how the palette entry is to be used.
 * } PALETTEENTRY;
 * 
 * typedef struct {
 *   AVIPALCHANGE avipalchange;
 * } AVIPALCHANGE0;
 * 
 * typedef struct {
 *     PALETTEENTRY  p[256];
 * } PALETTEENTRY_ALLENTRIES;
 * 
 * typedef struct {
 *     BYTE          firstEntry;
 *         // Specifies the index of the first palette entry to change.
 *     BYTE          numEntries;
 *         // Specifies the number of palette entries to change, or zero to change 
 *         // all 256 palette entries.
 *     WORD          flags;
 *         // Reserved.
 *     PALETTEENTRY  peNew[numEntries];
 *         // Specifies an array of PALETTEENTRY structures, of size "numEntries".
 *     PALETTEENTRY_ALLENTRIES  all[numEntries==0];
 * } AVIPALCHANGE;
 * </pre>
 * 
 *
 * @author Werner Randelshofer
 * @version $Id: TechSmithCodecCore.java 299 2013-01-03 07:40:18Z werner $
 */
public class TechSmithCodecCore extends AbstractVideoCodecCore {

    private ByteArrayImageOutputStream temp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);
    private byte[] temp2;
    private int[] palette;

    public TechSmithCodecCore() {
        reset();
    }

    public void reset() {
        palette = null;
    }

    public int[] getPalette() {
        if (palette == null) {
            palette = new int[256];
            // initalize palette with grayscale colors
            for (int i = 0; i < palette.length; i++) {
                palette[i] = (i) | (i << 8) | (i << 16);
            }
        }
        return palette;
    }

    /** Decodes an AVI palette change chunk. 
     * FIXME - This could be moved out into a separate class.
     */
    public void decodePalette(byte[] inDat, int off, int len) throws IOException {
        getPalette();
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(inDat, off, len, ByteOrder.LITTLE_ENDIAN);
        int firstEntry = in.readUnsignedByte();
        int numEntries = in.readUnsignedByte();
        if (numEntries == 0) {
            numEntries = 256;
        }
        int flags = in.readUnsignedShort();
        if (firstEntry + numEntries > 256) {
            throw new IOException("Illegal headers in pc chunk. firstEntry=" + firstEntry + ", numEntries=" + numEntries);
        }
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < numEntries; i++) {
            int rgbf = in.readInt();
            palette[i + firstEntry] = rgbf >> 8;
        }
    }

    /** Decodes to 8-bit palettised. 
     * Returns true if a key-frame was decoded.
     * 
     * 
     * @param inDat
     * @param off
     * @param length
     * @param outDat
     * @param prevDat The pixels decoded in the previous frame. Since no double
     *                buffering is used, this can be the same array than
     *                {@code outDat}.
     * @param width
     * @param height
     * @param onlyDecodeIfKeyframe
     * @return True if a key-frame was decoded.
     * @throws IOException 
     */
    public boolean decode8(byte[] inDat, int off, int length, byte[] outDat, byte[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {
        // Handle delta frame with all identical pixels
        if (length <= 2) {
            return false;
        }

        UncachedImageInputStream in = new UncachedImageInputStream(
                new InflaterInputStream(new ByteArrayInputStream(inDat, off, length)));

        int offset = 0;
        int scanlineStride = width;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline
        int verticalOffset = 0;
        boolean isKeyFrame = true;
        try {
            int y = 0;
            int xy = upsideDown;
            loop:
            while (true) {
                int opcode = in.readUnsignedByte();
                if (opcode == 0) {
                    opcode = in.readUnsignedByte();
                    switch (opcode) {
                        case 0x0000: // end of line
                            y++;
                            xy = (height - 1 - y) * scanlineStride + offset;
                            break;
                        case 0x0001: // end of bitmap
                            break loop;
                        case 0x0002: // delta skip
                            isKeyFrame = false;
                            int dx = in.readUnsignedByte();
                            int dy = in.readUnsignedByte();
                            y += dy;
                            int end = xy + dx - dy * scanlineStride;
                            if (prevDat != outDat) {
                                System.arraycopy(prevDat, xy, outDat, xy, end - xy);
                            }
                            xy = end;
                            break;
                        default: // literal run
                            in.readFully(outDat, xy, opcode);
                            xy += opcode;
                            if ((opcode & 1) == 1) {
                                int pad = in.readByte() & 0xff;
                                if (pad != 0) {
                                    throw new IOException("Illegal pad byte, pad=0x" + Integer.toHexString(pad));
                                }
                            }
                            break;
                    }
                } else {
                    // repetition
                    byte v = in.readByte();
                    for (int end = xy + opcode; xy < end; xy++) {
                        outDat[xy] = v;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            t.printStackTrace();
        }
        in.close();
        return isKeyFrame;
    }

    /** Decodes to 24-bit direct color. 
     * Returns true if a key-frame was decoded.
     * 
     * 
     * @param inDat
     * @param off
     * @param length
     * @param outDat
     * @param prevDat The pixels decoded in the previous frame. Since no double
     *                buffering is used, this can be the same array than
     *                {@code outDat}.
     * @param width
     * @param height
     * @param onlyDecodeIfKeyframe
     * @return True if a key-frame was decoded.
     * @throws IOException 
     */
    public boolean decode8(byte[] inDat, int off, int length, int[] outDat, int[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {
        // Handle delta frame with all identical pixels
        if (length <= 2) {
            return false;
        }
        if (temp2 == null || temp2.length < 255) {
            temp2 = new byte[255];
        }
        getPalette();

        UncachedImageInputStream in = new UncachedImageInputStream(
                new InflaterInputStream(new ByteArrayInputStream(inDat, off, length)));

        int offset = 0;
        int scanlineStride = width;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline
        int verticalOffset = 0;
        boolean isKeyFrame = true;
        try {
            int y = 0;
            int xy = upsideDown;
            loop:
            while (true) {
                int opcode = in.readUnsignedByte();
                if (opcode == 0) {
                    opcode = in.readUnsignedByte();
                    switch (opcode) {
                        case 0x0000: // end of line
                            y++;
                            xy = (height - 1 - y) * scanlineStride + offset;
                            break;
                        case 0x0001: // end of bitmap
                            break loop;
                        case 0x0002: { // delta skip
                            isKeyFrame = false;
                            int dx = in.readUnsignedByte();
                            int dy = in.readUnsignedByte();
                            y += dy;
                            int end = xy + dx - dy * scanlineStride;
                            if (prevDat != outDat) {
                                System.arraycopy(prevDat, xy, outDat, xy, end - xy);
                            }
                            xy = end;
                            break;
                        }
                        default: { // literal run
                            in.readFully(temp2, 0, opcode);
                            for (int i = 0; i < opcode; i++) {
                                outDat[xy + i] = palette[temp2[i] & 0xff];
                            }
                            xy += opcode;
                            if ((opcode & 1) == 1) {
                                int pad = in.readByte() & 0xff;
                                if (pad != 0) {
                                    throw new IOException("Illegal pad byte, pad=0x" + Integer.toHexString(pad));
                                }
                            }
                            break;
                        }
                    }
                } else {
                    // repetition
                    int v = palette[in.readUnsignedByte()];
                    for (int end = xy + opcode; xy < end; xy++) {
                        outDat[xy] = v;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            t.printStackTrace();
        }
        in.close();
        return isKeyFrame;
    }

    /** Decodes to 24-bit RGB. 
     * Returns true if a key-frame was decoded.
     */
    public boolean decode24(byte[] inDat, int off, int length, int[] outDat, int[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {
        // Handle delta frame with all identical pixels
        if (length <= 2) {
            return false;
        }

        UncachedImageInputStream in = new UncachedImageInputStream(
                new InflaterInputStream(new ByteArrayInputStream(inDat, off, length)));

        int offset = 0;
        int scanlineStride = width;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline
        int verticalOffset = 0;
        boolean isKeyFrame = true;
        try {
            int y = 0;
            int xy = upsideDown;
            loop:
            while (true) {
                int opcode = in.readUnsignedByte();
                if (opcode == 0) {
                    opcode = in.readUnsignedByte();
                    switch (opcode) {
                        case 0x0000: // end of line
                            y++;
                            xy = (height - 1 - y) * scanlineStride + offset;
                            break;
                        case 0x0001: // end of bitmap
                            break loop;
                        case 0x0002: {// delta skip
                            isKeyFrame = false;
                            int dx = in.readUnsignedByte();
                            int dy = in.readUnsignedByte();
                            y += dy;
                            int end = xy + dx - dy * scanlineStride;
                            if (prevDat != outDat) {
                                System.arraycopy(prevDat, xy, outDat, xy, end - xy);
                            }
                            xy = end;
                            break;
                        }
                        default: {// literal run
                            readInts24LE(in, outDat, xy, opcode);
                            xy += opcode;
                            break;
                        }
                    }
                } else {
                    // repetition
                    int v = readInt24LE(in);
                    for (int end = xy + opcode; xy < end; xy++) {
                        outDat[xy] = v;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            t.printStackTrace();
        }
        in.close();
        return isKeyFrame;
    }
    /** Decodes from 16-bit to 24-bit RGB. 
     * Returns true if a key-frame was decoded.
     */
    public boolean decode16(byte[] inDat, int off, int length, int[] outDat, int[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) throws IOException {
        // Handle delta frame with all identical pixels
        if (length <= 2) {
            if (outDat!=prevDat) {
                System.arraycopy(prevDat,0,outDat,0,width*height);
            }
            return false;
        }

        UncachedImageInputStream in = new UncachedImageInputStream(
                new InflaterInputStream(new ByteArrayInputStream(inDat, off, length)), ByteOrder.LITTLE_ENDIAN);

        int offset = 0;
        int scanlineStride = width;
        int upsideDown = (height - 1) * scanlineStride + offset;
        // Decode each scanline
        int verticalOffset = 0;
        boolean isKeyFrame = true;
        try {
            int y = 0;
            int xy = upsideDown;
            loop:
            while (true) {
                int opcode = in.readUnsignedByte();
                if (opcode == 0) {
                    opcode = in.readUnsignedByte();
                    switch (opcode) {
                        case 0x0000: // end of line
                            y++;
                            xy = (height - 1 - y) * scanlineStride + offset;
                            break;
                        case 0x0001: // end of bitmap
                            break loop;
                        case 0x0002: {// delta skip
                            isKeyFrame = false;
                            int dx = in.readUnsignedByte();
                            int dy = in.readUnsignedByte();
                            y += dy;
                            int end = xy + dx - dy * scanlineStride;
                            if (prevDat != outDat) {
                                System.arraycopy(prevDat, xy, outDat, xy, end - xy);
                            }
                            xy = end;
                            break;
                        }
                        default: {// literal run
                            readRGBs555to24(in, outDat, xy, opcode);
                            xy += opcode;
                            break;
                        }
                    }
                } else {
                    // repetition
                    int v = readRGB555to24(in);
                    for (int end = xy + opcode; xy < end; xy++) {
                        outDat[xy] = v;
                    }
                }

            }
        } catch (ArrayIndexOutOfBoundsException t) {
            t.printStackTrace();
        }
        in.close();
        return isKeyFrame;
    }

    /** Encodes an 8-bit delta frame with indexed colors.
     *
     * @param out The output stream. 
     * @param data The image data.
     * @param prev The image data of the previous frame.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to add to offset to get to the next scanline.
     */
    public void encodeDelta8(OutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {

        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }


            while (verticalOffset > 0 || skipCount > 0) {
                temp.write(0x00); // Escape code
                temp.write(0x02); // Skip OP-code
                temp.write(min(255, skipCount)); // horizontal offset
                temp.write(min(255, verticalOffset)); // vertical offset
                skipCount -= min(255, skipCount);
                verticalOffset -= min(255, verticalOffset);
            }

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            temp.write(1); // Repeat OP-code
                            temp.write(data[xy - literalCount]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            temp.write(0); // Escape code
                            temp.write(literalRun); // Literal OP-code
                            temp.write(data, xy - literalCount, literalRun);
                            if ((literalRun & 1) == 1) {
                                temp.write(0); // pad byte
                            }
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            temp.write(0); // Escape code
                            temp.write(0x0002); // Skip OP-code
                            temp.write(min(255, skipCount));
                            temp.write(0);
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        temp.write(repeatCount); // Repeat OP-code
                        temp.write(v);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    temp.write(1); // Repeat OP-code
                    temp.write(data[xy - literalCount]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    temp.write(0);
                    temp.write(literalRun); // Literal OP-code
                    temp.write(data, xy - literalCount, literalRun);
                    if ((literalRun & 1) == 1) {
                        temp.write(0); // pad byte
                    }
                    literalCount -= literalRun;
                }
            }

            temp.write(0); // Escape code
            temp.write(0x00); // End of line OP-code
        }
        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap


        if (temp.length() == 2) {
            temp.toOutputStream(out);
        } else {
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            temp.toOutputStream(defl);
            defl.finish();
        }
    }

    /** Encodes an 8-bit delta frame with indexed colors to 24-bit.
     *
     * @param out The output stream. 
     * @param data The image data.
     * @param prev The image data of the previous frame.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to add to offset to get to the next scanline.
     */
    public void encodeDelta8to24(OutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {

        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }


            while (verticalOffset > 0 || skipCount > 0) {
                temp.write(0x00); // Escape code
                temp.write(0x02); // Skip OP-code
                temp.write(min(255, skipCount)); // horizontal offset
                temp.write(min(255, verticalOffset)); // vertical offset
                skipCount -= min(255, skipCount);
                verticalOffset -= min(255, verticalOffset);
            }

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            temp.write(1); // Repeat OP-code
                            writeInt24LE(temp, palette[data[xy - literalCount]&0xff]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            temp.write(0); // Escape code
                            temp.write(literalRun); // Literal OP-code
                            for (int i = xy - literalCount, end = xy - literalCount + literalRun; i < end; i++) {
                                writeInt24LE(temp, palette[data[i]&0xff]);
                            }
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            temp.write(0); // Escape code
                            temp.write(0x0002); // Skip OP-code
                            temp.write(min(255, skipCount));
                            temp.write(0);
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        temp.write(repeatCount); // Repeat OP-code
                        writeInt24LE(temp, palette[v&0xff]);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    temp.write(1); // Repeat OP-code
                    writeInt24LE(temp, palette[data[xy - literalCount]]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    temp.write(0);
                    temp.write(literalRun); // Literal OP-code
                    for (int i = xy - literalCount, end = xy - literalCount + literalRun; i < end; i++) {
                        writeInt24LE(temp, palette[data[i]&0xff]);
                    }
                    /*
                    temp.write(data, xy - literalCount, literalRun);
                    if (literalRun & 1 == 1) {
                    temp.write(0); // pad byte
                    }*/
                    literalCount -= literalRun;
                }
            }

            temp.write(0); // Escape code
            temp.write(0x00); // End of line OP-code
        }
        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap


        if (temp.length() == 2) {
            temp.toOutputStream(out);
        } else {
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            temp.toOutputStream(defl);
            defl.finish();
        }
    }

    /** Encodes a delta frame which is known to have the same content than
     * the previous frame.
     * 
     * @param out
     * @param data
     * @param prev
     * @param width
     * @param height
     * @param offset
     * @param scanlineStride
     * @throws IOException 
     */
    public void encodeSameDelta8(OutputStream out, byte[] data, byte[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {
        /*
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap
        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
        */
        out.write(0); // Escape code
        out.write(0x01);// End of bitmap
    }

    /** Encodes a delta frame which is known to have the same content than
     * the previous frame.
     * 
     * @param out
     * @param data
     * @param prev
     * @param width
     * @param height
     * @param offset
     * @param scanlineStride
     * @throws IOException 
     */
    public void encodeSameDelta24(OutputStream out, int[] data, int[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {
        /*
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap
        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
        */
        out.write(0); // Escape code
        out.write(0x01);// End of bitmap
    }

    /** Encodes a delta frame which is known to have the same content than
     * the previous frame.
     * 
     * @param out
     * @param data
     * @param prev
     * @param width
     * @param height
     * @param offset
     * @param scanlineStride
     * @throws IOException 
     */
    public void encodeSameDelta16(OutputStream out, short[] data, short[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {
        /*
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap
        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
        */
        out.write(0); // Escape code
        out.write(0x01);// End of bitmap
    }

    /** Encodes a 8-bit key frame with indexed colors.
     *
     * @param out The output stream.
     * @param data The image data.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to add to offset to get to the next scanline.
     */
    public void encodeKey8(OutputStream out, byte[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        temp.write(0);
                        temp.write(literalCount); // Literal OP-code
                        temp.write(data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                temp.write(1); // Repeat OP-code
                                temp.write(data[xy - literalCount]);
                            }
                        } else {
                            temp.write(0);
                            temp.write(literalCount); // Literal OP-code
                            temp.write(data, xy - literalCount, literalCount);
                            if ((literalCount & 1) == 1) {
                                temp.write(0); // pad byte
                            }
                            literalCount = 0;
                        }
                    }
                    temp.write(repeatCount); // Repeat OP-code
                    temp.write(v);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        temp.write(1); // Repeat OP-code
                        temp.write(data[xy - literalCount]);
                    }
                } else {
                    temp.write(0);
                    temp.write(literalCount);
                    temp.write(data, xy - literalCount, literalCount);
                    if ((literalCount & 1) == 1) {
                        temp.write(0); // pad byte
                    }
                }
                literalCount = 0;
            }

            temp.write(0);
            temp.write(0x0000);// End of line
        }
        temp.write(0);
        temp.write(0x0001);// End of bitmap
        //temp.toOutputStream(out);

        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
    }

    /** Encodes a 8-bit key frame with indexed colors to 24-bit.
     *
     * @param out The output stream. 
     * @param data The image data.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to add to offset to get to the next scanline.
     */
    public void encodeKey8to24(OutputStream out, byte[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                byte v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        temp.write(0);
                        temp.write(literalCount); // Literal OP-code
                        for (int i = xy - literalCount + 1, end = xy + 1; i < end; i++) {
                            writeInt24LE(temp, palette[data[i]&0xff]);
                        }
                        //temp.write(data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                temp.write(1); // Repeat OP-code
                                writeInt24LE(temp, palette[data[xy - literalCount]&0xff]);
                            }
                        } else {
                            temp.write(0);
                            temp.write(literalCount); // Literal OP-code
                            for (int i = xy - literalCount, end = xy; i < end; i++) {
                                writeInt24LE(temp, palette[data[i]&0xff]);
                            }
                            //temp.write(data, xy - literalCount, literalCount);
                            //if ((literalCount & 1) == 1) {
                            //    temp.write(0); // pad byte
                            //}
                            literalCount = 0;
                        }
                    }
                    temp.write(repeatCount); // Repeat OP-code
                    writeInt24LE(temp, palette[v&0xff]);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        temp.write(1); // Repeat OP-code
                        writeInt24LE(temp, palette[data[xy - literalCount]&0xff]);
                    }
                } else {
                    temp.write(0);
                    temp.write(literalCount);
                    for (int i = xy - literalCount, end = xy; i < end; i++) {
                        writeInt24LE(temp, palette[data[i]&0xff]);
                    }
                    //temp.write(data, xy - literalCount, literalCount);
                    //if ((literalCount & 1) == 1) {
                    //    temp.write(0); // pad byte
                    //}
                }
                literalCount = 0;
            }

            temp.write(0);
            temp.write(0x0000);// End of line
        }
        temp.write(0);
        temp.write(0x0001);// End of bitmap
        //temp.toOutputStream(out);

        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
    }

    /** Encodes a 16-bit delta frame.
     *
     * @param out The output stream. 
     * @param data The image data.
     * @param prev The image data of the previous frame.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to add to offset to get to the next scanline.
     */
    public void encodeDelta16(OutputStream out, short[] data, short[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {


        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }

            while (verticalOffset > 0 || skipCount > 0) {
                temp.write(0x00); // Escape code
                temp.write(0x02); // Skip OP-code
                temp.write(min(255, skipCount)); // horizontal offset
                temp.write(min(255, verticalOffset)); // vertical offset
                skipCount -= min(255, skipCount);
                verticalOffset -= min(255, verticalOffset);
            }

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                short v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            temp.write(1); // Repeat OP-code
                            temp.writeShort(data[xy - literalCount]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            temp.write(0); // Escape code
                            temp.write(literalRun); // Literal OP-code
                            temp.writeShorts(data, xy - literalCount, literalRun);
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            temp.write(0); // Escape code
                            temp.write(0x02); // Skip OP-code
                            temp.write(min(255, skipCount)); // horizontal skip
                            temp.write(0); // vertical skip
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        temp.write(repeatCount); // Repeat OP-code
                        temp.writeShort(v);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    temp.write(1); // Repeat OP-code
                    temp.writeShort(data[xy - literalCount]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    temp.write(0); // Escape code
                    temp.write(literalRun); // Literal OP-code
                    temp.writeShorts(data, xy - literalCount, literalRun);
                    literalCount -= literalRun;
                }
            }

            temp.write(0); // Escape code
            temp.write(0x00); // End of line OP-code
        }

        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap OP-code

        if (temp.length() == 2) {
            temp.toOutputStream(out);
        } else {
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            temp.toOutputStream(defl);
            defl.finish();
        }
    }

    /** Encodes a 24-bit key frame.
     *
     * @param out The output stream.
     * @param data The image data.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to add to offset to get to the next scanline.
     */
    public void encodeKey24(OutputStream out, int[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                int v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        temp.write(0);
                        temp.write(literalCount); // Literal OP-code
                        writeInts24LE(temp, data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                temp.write(1); // Repeat OP-code
                                writeInt24LE(temp, data[xy - literalCount]);
                            }
                        } else {
                            temp.write(0);
                            temp.write(literalCount); // Literal OP-code
                            writeInts24LE(temp, data, xy - literalCount, literalCount);
                            ///if (literalCount & 1 == 1) {
                            ///    temp.write(0); // pad byte
                            ///}
                            literalCount = 0;
                        }
                    }
                    temp.write(repeatCount); // Repeat OP-code
                    writeInt24LE(temp, v);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        temp.write(1); // Repeat OP-code
                        writeInt24LE(temp, data[xy - literalCount]);
                    }
                } else {
                    temp.write(0);
                    temp.write(literalCount);
                    writeInts24LE(temp, data, xy - literalCount, literalCount);
                    ///if (literalCount & 1 == 1) {
                    ///    temp.write(0); // pad byte
                    ///}
                }
                literalCount = 0;
            }

            temp.write(0);
            temp.write(0x0000);// End of line
        }
        temp.write(0);
        temp.write(0x0001);// End of bitmap
        //temp.toOutputStream(out);

        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
    }

    /** Encodes a 24-bit delta frame.
     *
     * @param out The output stream. 
     * @param data The image data.
     * @param prev The image data of the previous frame.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to add to offset to get to the next scanline.
     */
    public void encodeDelta24(OutputStream out, int[] data, int[] prev, int width, int height, int offset, int scanlineStride)
            throws IOException {

        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline
        int verticalOffset = 0;
        ScanlineLoop:
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            // determine skip count
            int skipCount = 0;
            for (; xy < xymax; ++xy, ++skipCount) {
                if (data[xy] != prev[xy]) {
                    break;
                }
            }
            if (skipCount == width) {
                // => the entire line can be skipped
                ++verticalOffset;
                continue;
            }

            while (verticalOffset > 0 || skipCount > 0) {
                temp.write(0x00); // Escape code
                temp.write(0x02); // Skip OP-code
                temp.write(min(255, skipCount)); // horizontal offset
                temp.write(min(255, verticalOffset)); // vertical offset
                skipCount -= min(255, skipCount);
                verticalOffset -= min(255, verticalOffset);
            }

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine skip count
                for (skipCount = 0; xy < xymax; ++xy, ++skipCount) {
                    if (data[xy] != prev[xy]) {
                        break;
                    }
                }
                xy -= skipCount;

                // determine repeat count
                int v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;

                if (skipCount < 4 && xy + skipCount < xymax && repeatCount < 3) {
                    literalCount++;
                } else {
                    while (literalCount > 0) {
                        if (literalCount < 3) {
                            temp.write(1); // Repeat OP-code
                            writeInt24LE(temp, data[xy - literalCount]);
                            literalCount--;
                        } else {
                            int literalRun = min(254, literalCount);
                            temp.write(0);
                            temp.write(literalRun); // Literal OP-code
                            writeInts24LE(temp, data, xy - literalCount, literalRun);
                            ///if (literalRun & 1 == 1) {
                            ///    temp.write(0); // pad byte
                            ///}
                            literalCount -= literalRun;
                        }
                    }
                    if (xy + skipCount == xymax) {
                        // => we can skip until the end of the line without
                        //    having to write an op-code
                        xy += skipCount - 1;
                    } else if (skipCount >= repeatCount) {
                        while (skipCount > 0) {
                            temp.write(0);
                            temp.write(0x0002); // Skip OP-code
                            temp.write(min(255, skipCount));
                            temp.write(0);
                            xy += min(255, skipCount);
                            skipCount -= min(255, skipCount);
                        }
                        xy -= 1;
                    } else {
                        temp.write(repeatCount); // Repeat OP-code
                        writeInt24LE(temp, v);
                        xy += repeatCount - 1;
                    }
                }
            }

            // flush literal run
            while (literalCount > 0) {
                if (literalCount < 3) {
                    temp.write(1); // Repeat OP-code
                    writeInt24LE(temp, data[xy - literalCount]);
                    literalCount--;
                } else {
                    int literalRun = min(254, literalCount);
                    temp.write(0);
                    temp.write(literalRun); // Literal OP-code
                    writeInts24LE(temp, data, xy - literalCount, literalRun);
                    ///if (literalRun & 1 == 1) {
                    ///   temp.write(0); // pad byte
                    ///}
                    literalCount -= literalRun;
                }
            }

            temp.write(0); // Escape code
            temp.write(0x00); // End of line OP-code
        }

        temp.write(0); // Escape code
        temp.write(0x01);// End of bitmap

        if (temp.length() == 2) {
            temp.toOutputStream(out);
        } else {
            DeflaterOutputStream defl = new DeflaterOutputStream(out);
            temp.toOutputStream(defl);
            defl.finish();
        }
    }

    /** Encodes a 16-bit key frame.
     *
     * @param out The output stream.
     * @param data The image data.
     * @param offset The offset to the first pixel in the data array.
     * @param width The width of the image in data elements.
     * @param scanlineStride The number to add to offset to get to the next scanline.
     */
    public void encodeKey16(OutputStream out, short[] data, int width, int height, int offset, int scanlineStride)
            throws IOException {
        temp.clear();temp.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int ymax = offset + height * scanlineStride;
        int upsideDown = ymax - scanlineStride + offset;

        // Encode each scanline separately
        for (int y = offset; y < ymax; y += scanlineStride) {
            int xy = upsideDown - y;
            int xymax = xy + width;

            int literalCount = 0;
            int repeatCount = 0;
            for (; xy < xymax; ++xy) {
                // determine repeat count
                short v = data[xy];
                for (repeatCount = 0; xy < xymax && repeatCount < 255; ++xy, ++repeatCount) {
                    if (data[xy] != v) {
                        break;
                    }
                }
                xy -= repeatCount;
                if (repeatCount < 3) {
                    literalCount++;
                    if (literalCount == 254) {
                        temp.write(0); // Escape code
                        temp.write(literalCount); // Literal OP-code
                        temp.writeShorts(data, xy - literalCount + 1, literalCount);
                        literalCount = 0;
                    }
                } else {
                    if (literalCount > 0) {
                        if (literalCount < 3) {
                            for (; literalCount > 0; --literalCount) {
                                temp.write(1); // Repeat OP-code
                                temp.writeShort(data[xy - literalCount]);
                            }
                        } else {
                            temp.write(0);
                            temp.write(literalCount); // Literal OP-code
                            temp.writeShorts(data, xy - literalCount, literalCount);
                            ///if (literalCount & 1 == 1) {
                            ///    temp.write(0); // pad byte
                            ///}
                            literalCount = 0;
                        }
                    }
                    temp.write(repeatCount); // Repeat OP-code
                    temp.writeShort(v);
                    xy += repeatCount - 1;
                }
            }

            // flush literal run
            if (literalCount > 0) {
                if (literalCount < 3) {
                    for (; literalCount > 0; --literalCount) {
                        temp.write(1); // Repeat OP-code
                        temp.writeShort(data[xy - literalCount]);
                    }
                } else {
                    temp.write(0);
                    temp.write(literalCount);
                    temp.writeShorts(data, xy - literalCount, literalCount);
                    ///if (literalCount & 1 == 1) {
                    ///    temp.write(0); // pad byte
                    ///}
                }
                literalCount = 0;
            }

            temp.write(0);
            temp.write(0x0000);// End of line
        }
        temp.write(0);
        temp.write(0x0001);// End of bitmap
        //temp.toOutputStream(out);

        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        temp.toOutputStream(defl);
        defl.finish();
    }

    public void setPalette(byte[] redValues, byte[] greenValues, byte[] blueValues) {
        if (palette==null)palette=new int[256];
        for (int i=0;i<256;i++) {
            palette[i]=((redValues[i]&0xff)<<16)
                    |((greenValues[i]&0xff)<<8)
                    |((blueValues[i]&0xff)<<0);
        }
    }
}
