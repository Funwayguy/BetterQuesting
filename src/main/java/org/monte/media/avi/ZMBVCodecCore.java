/*
 * @(#)ZMBVCodecCore.java  1.0  2011-08-29
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.avi;

//import com.jcraft.jzlib.InflaterInputStream;
import java.util.zip.InflaterInputStream;
import java.io.IOException;
import org.monte.media.io.AppendableByteArrayInputStream;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.io.UncachedImageInputStream;
import java.nio.ByteOrder;
import javax.imageio.stream.ImageInputStream;
import static java.lang.Math.*;

/**
 * Implements the DosBox Capture Codec {@code "ZMBV"}.
 * 
 * <p>This is a codec added to the DosBox project to capture screen data 
 * (like Vmware VMNC).</p>
 * 
 * <p>This codec employs ZLIB compression and has intraframes and delta frames.
 * Delta frames seem to have blocks either copied from the previous frame or 
 * XOR'ed with some block from the previous frame.
 * <p>
 * The FourCC for this codec is ZMBV which ostensibly stands for Zip Motion 
 * Blocks Video. The data is most commonly stored in AVI files.
 * </p>
 * <p><b>Data Format</b></p>
 * <p>Byte 0 of a ZMBV data chunk contains the following flags:</p>
 * <pre>
 * bits 7-2  undefined
 * bit 1     palette change
 * bit 0     1 = intraframe, 0 = interframe
 * </pre>
 * 
 * <p>If the frame is an intra frame as indicated by bit 0 of byte 0, the next
 * 6 bytes in the data chunk are formatted as follows:</p>
 * <pre>
 * byte 1    major version
 * byte 2    minor version
 * byte 3    compression type (0 = uncompressed, 1 = zlib-compressed)
 * byte 4    video format
 * byte 5    block width
 * byte 6    block height
 * </pre>
 * <p>Presently, the only valid major/minor version pair is 0/1. A block width or
 * height of 0 is invalid. These are the video modes presently defined:</p>
 * <pre>
 * 0  none
 * 1  1 bit/pixel, palettized
 * 2  2 bits/pixel, palettized
 * 3  4 bits/pixel, palettized
 * 4  8 bits/pixel, palettized
 * 5  15 bits/pixel
 * 6  16 bits/pixel
 * 7  24 bits/pixel
 * 8  32 bits/pixel
 * </pre>
 * 
 * <p>Presently, only modes 4 (8 bpp), 5 (15 bpp), 6 (16 bpp) and 8 (32 bpp) are
 * supported.</p>
 * 
 * <p>If the compression type is 1, the remainder of the data chunk is compressed
 * using the standard zlib package. Decompress the data before proceeding with 
 * the next step. Otherwise, proceed to the next step. Also note that you must 
 * reset zlib for intraframes.</p>
 * 
 * <p>If bit 1 of the frame header (palette change) is set then the first 768 
 * bytes of the uncompressed data represent 256 red-green-blue palette triplets.
 * Each component is one byte and ranges from 0..255.</p>
 * 
 * <p>An intraframe consists of 768 bytes of palette data (for palettized modes)
 * and raw frame data.</p>
 * 
 * </p>An interframe is comprised of up to three parts:</p>
 * <ol>
 * <li>if palette change flag was set then first 768 bytes represent XOR'ed 
 * palette difference</li>
 * <li>block info (2 bytes per block, padded to 4 bytes length)</li>
 * <li>block differences</li>
 * </ol>
 * <p>Block info is composed from a motion vector and a flag: first byte is 
 * (dx &lt;&lt; 1) | flag, second byte is (dy &lt;&lt; 1). Motion vectors can go
 * out of bounds and in that case you need to zero the out-of-bounds part. 
 * Also note that currently motion vectors are limited to a range of (-16..16).
 * Flag tells whether the codec simply copies the block from the decoded offset
 * or copies it and XOR's it with data from block differences. All XORing for
 * 15/16 bpp and 32 bpp modes is done with little-endian integers.</p>
 * 
 * <p>Interframe decoding can be done this way:</p>
 * <pre>
 * for each block {
 *   a = block_info[current_block][0];
 *   b = block_info[current_block][1];
 *   dx = a &gt;&gt; 1;
 *   dy = b &gt;&gt; 1;
 *   flag = a & 1;
 *   copy block from offset (dx, dy) from previous frame.
 *   if (flag) {
 *     XOR block with data read from stream.
 *   }
 * }
 * </pre>
 * 
 * <p>References<br>
 * <a href="http://wiki.multimedia.cx/index.php?title=ZMBV"
 * >http://wiki.multimedia.cx/index.php?title=ZMBV</a>
 * </p>
 * 
 * <p>Note: We use the JZLib library for decoding compressed input streams,
 * because the {@code javax.zip.InflaterInputStream} sometimes fails to decode
 * the data.</p>
 * 
 * *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-29 Created.
 */
public class ZMBVCodecCore {

    public final static int VIDEOMODE_NONE = 0;
    public final static int VIDEOMODE_1_BIT_PALETTIZED = 1;
    public final static int VIDEOMODE_2_BIT_PALETTIZED = 2;
    public final static int VIDEOMODE_4_BIT_PALETTIZED = 3;
    public final static int VIDEOMODE_8_BIT_PALETTIZED = 4;
    public final static int VIDEOMODE_15_BIT_BGR = 5;
    public final static int VIDEOMODE_16_BIT_BGR = 6;
    public final static int VIDEOMODE_24_BIT_BGR = 7;
    public final static int VIDEOMODE_32_BIT_BGR = 8;
    public final static int COMPRESSION_NONE = 0;
    public final static int COMPRESSION_ZLIB = 1;
    /**
     * 
     * @param inDat Input data.
     * @param off Input data offset.
     * @param length Input data length.
     * @param outDat Output data. 32 bits per pixel: {palette index, red, green, blue}.
     * @param prevDat Previous output data array. This is needed because the
     * codec uses double buffering.  32 bits per pixel: {palette index, red, green, blue}.
     * @param width Image width.
     * @param height Image height.
     * @param state Codec state.
     * @return true if keyframe.
     */
    private int majorVersion;
    private int minorVersion;
    private int compressionType;
    private int videoFormat;
    private int blockWidth, blockHeight;
    private InflaterInputStream inflaterInputStream;
    private AppendableByteArrayInputStream byteArrayInputStream;
    private int[] palette;
    private byte[] blockDataBuf;
    private byte[] blockHeaderBuf;

    /** Decodes to 32-bit RGB. 
     * Returns true if a key-frame was decoded.
     */
    public boolean decode(byte[] inDat, int off, int length, int[] outDat, int[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) {
        boolean isKeyframe = false;
        try {
            ImageInputStream in = new ByteArrayImageInputStream(inDat, off, length, ByteOrder.LITTLE_ENDIAN);

            int flags = in.readUnsignedByte();
            isKeyframe = (flags & 1) != 0;

            if (onlyDecodeIfKeyframe && !isKeyframe) {
                System.out.println("ZMBVCodec cannot decode delta without preceeding keyframe.");
                return false;
            }

            if (isKeyframe) {
                // => Key frame 
                //System.out.println("ZMBVCode Keyframe w,h=" + width + "," + height);
                majorVersion = in.readUnsignedByte();
                minorVersion = in.readUnsignedByte();
                compressionType = in.readUnsignedByte();
                videoFormat = in.readUnsignedByte();
                blockWidth = in.readUnsignedByte();
                blockHeight = in.readUnsignedByte();
            }
            if (majorVersion != 0 || minorVersion != 1) {
                System.err.println("unsupported version " + majorVersion + "." + minorVersion);
                return isKeyframe;
            }


            switch (compressionType) {
                case COMPRESSION_ZLIB:

                    if (!isKeyframe && inflaterInputStream != null) {
                        // => streams are present.
                        //    Append new data.
                        AppendableByteArrayInputStream bais = byteArrayInputStream;
                        bais.appendBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()), true);
                    } else {
                        // => Keyframe or no Inflater Stream present. Create new one, and ensure
                        //    that we can append new data to it later on.
                        if (byteArrayInputStream != null) {
                            byteArrayInputStream.setBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        } else {
                            byteArrayInputStream = new AppendableByteArrayInputStream(inDat.clone(), (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        }
                        if (inflaterInputStream != null) {
                            inflaterInputStream.close();
                        }
                        inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
                    }
                    in = new UncachedImageInputStream(inflaterInputStream, ByteOrder.LITTLE_ENDIAN);
                    break;
                case COMPRESSION_NONE:
                    System.out.println(" NO COMPRESSION");
                    return isKeyframe;
                default:
                    System.err.println("unsupported compression type " + compressionType);
                    return isKeyframe;

            }

            switch (videoFormat) {
                case VIDEOMODE_8_BIT_PALETTIZED:
                    decode8to32(in, outDat, prevDat, flags, width, height);
                    break;

                case VIDEOMODE_15_BIT_BGR:
                    decode15to32(in, outDat, prevDat, flags, width, height);
                    break;
                case VIDEOMODE_16_BIT_BGR:
                    decode16to32(in, outDat, prevDat, flags, width, height);
                    break;
                case VIDEOMODE_32_BIT_BGR:
                    decode32to32(in, outDat, prevDat, flags, width, height);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported video format " + videoFormat);
            }
        } catch (IOException ex) {
            //System.out.println("ZMBVCodecCore "+ex);
            System.err.println("ZMBVCodecCore decoding, isKeyframe=" + isKeyframe);
            ex.printStackTrace();
        }
        return isKeyframe;
    }

    /** Decodes to 8-bit palettised. 
     * Returns true if a key-frame was decoded.
     */
    public boolean decode(byte[] inDat, int off, int length, byte[] outDat, byte[] prevDat, int width, int height, boolean onlyDecodeIfKeyframe) {
        boolean isKeyframe = false;
        try {
            ImageInputStream in = new ByteArrayImageInputStream(inDat, off, length, ByteOrder.LITTLE_ENDIAN);

            int flags = in.readUnsignedByte();
            isKeyframe = (flags & 1) != 0;

            if (onlyDecodeIfKeyframe && !isKeyframe) {
                System.out.println("ZMBVCodec cannot decode delta without preceeding keyframe.");
                return false;
            }

            if (isKeyframe) {
                // => Key frame 
                //System.out.println("ZMBVCode Keyframe w,h=" + width + "," + height);
                majorVersion = in.readUnsignedByte();
                minorVersion = in.readUnsignedByte();
                compressionType = in.readUnsignedByte();
                videoFormat = in.readUnsignedByte();
                blockWidth = in.readUnsignedByte();
                blockHeight = in.readUnsignedByte();
            }
            if (majorVersion != 0 || minorVersion != 1) {
                System.err.println("unsupported version " + majorVersion + "." + minorVersion);
                return isKeyframe;
            }


            switch (compressionType) {
                case COMPRESSION_ZLIB:

                    if (!isKeyframe && inflaterInputStream != null) {
                        // => streams are present.
                        //    Append new data.
                        AppendableByteArrayInputStream bais = byteArrayInputStream;
                        bais.appendBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()), true);
                    } else {
                        // => Keyframe or no Inflater Stream present. Create new one, and ensure
                        //    that we can append new data to it later on.
                        if (byteArrayInputStream != null) {
                            byteArrayInputStream.setBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        } else {
                            byteArrayInputStream = new AppendableByteArrayInputStream(inDat.clone(), (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        }
                        if (inflaterInputStream != null) {
                            inflaterInputStream.close();
                        }
                        inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
                    }
                    in = new UncachedImageInputStream(inflaterInputStream, ByteOrder.LITTLE_ENDIAN);
                    break;
                case COMPRESSION_NONE:
                    System.out.println(" NO COMPRESSION");
                    return isKeyframe;
                default:
                    System.err.println("unsupported compression type " + compressionType);
                    return isKeyframe;

            }

            switch (videoFormat) {
                case VIDEOMODE_8_BIT_PALETTIZED:
                    decode8to8(in, outDat, prevDat, flags, width, height);
                    break;
                /*
                case VIDEOMODE_15_BIT_BGR:
                decode15BitBGR(in, outDat, prevDat, flags, width, height);
                break;
                case VIDEOMODE_16_BIT_BGR:
                decode16BitBGR(in, outDat, prevDat, flags, width, height);
                break;
                case VIDEOMODE_32_BIT_BGR:
                decode32BitBGR(in, outDat, prevDat, flags, width, height);
                break;
                 * */
                default:
                    throw new UnsupportedOperationException("Unsupported video format " + videoFormat);
            }
        } catch (IOException ex) {
            //System.out.println("ZMBVCodecCore "+ex);
            System.err.println("ZMBVCodecCore decoding, isKeyframe=" + isKeyframe);
            ex.printStackTrace();
        }
        return isKeyframe;
    }

    /** Decodes to 8-bit, 15-bit, 16-bit or 32-bit RGB depending on input data. 
     * Returns the number of decoded bits.
     * Returns a negative number if keyframe.
     * Returns 0 in case of failure.
     */
    public int decode(byte[] inDat, int off, int length, Object[] outDatHolder, Object[] prevDatHolder, int width, int height, boolean onlyDecodeIfKeyframe) {
        boolean isKeyframe = false;
        int depth = 0;
        try {
            ImageInputStream in = new ByteArrayImageInputStream(inDat, off, length, ByteOrder.LITTLE_ENDIAN);

            int flags = in.readUnsignedByte();
            isKeyframe = (flags & 1) != 0;

            if (onlyDecodeIfKeyframe && !isKeyframe) {
                System.err.println("ZMBVCodec cannot decode delta without preceeding keyframe.");
                return 0;
            }

            if (isKeyframe) {
                // => Key frame 
                //System.out.println("ZMBVCode Keyframe w,h=" + width + "," + height);
                majorVersion = in.readUnsignedByte();
                minorVersion = in.readUnsignedByte();
                compressionType = in.readUnsignedByte();
                videoFormat = in.readUnsignedByte();
                blockWidth = in.readUnsignedByte();
                blockHeight = in.readUnsignedByte();
            }
            if (majorVersion != 0 || minorVersion != 1) {
                System.err.println("unsupported version " + majorVersion + "." + minorVersion);
                return 0;
            }


            switch (compressionType) {
                case COMPRESSION_ZLIB:

                    if (!isKeyframe && inflaterInputStream != null) {
                        // => streams are present.
                        //    Append new data.
                        AppendableByteArrayInputStream bais = byteArrayInputStream;
                        bais.appendBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()), true);
                    } else {
                        // => Keyframe or no Inflater Stream present. Create new one, and ensure
                        //    that we can append new data to it later on.
                        if (byteArrayInputStream != null) {
                            byteArrayInputStream.setBuffer(inDat, (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        } else {
                            byteArrayInputStream = new AppendableByteArrayInputStream(inDat.clone(), (int) in.getStreamPosition() + off, (int) (length - in.getStreamPosition()));
                        }
                        if (inflaterInputStream != null) {
                            inflaterInputStream.close();
                        }
                        inflaterInputStream = new InflaterInputStream(byteArrayInputStream);
                    }
                    in = new UncachedImageInputStream(inflaterInputStream, ByteOrder.LITTLE_ENDIAN);
                    break;
                case COMPRESSION_NONE:
                    System.err.println(" NO COMPRESSION");
                    return 0;
                default:
                    System.err.println("unsupported compression type " + compressionType);
                    return 0;

            }

            switch (videoFormat) {
                case VIDEOMODE_8_BIT_PALETTIZED:
                    depth = 8;
                    if (!(outDatHolder[0] instanceof byte[])) {
                        outDatHolder[0] = new byte[width * height];
                    }
                    if (!(prevDatHolder[0] instanceof byte[])) {
                        prevDatHolder[0] = new byte[width * height];
                    }
                    decode8to8(in, (byte[]) outDatHolder[0], (byte[]) prevDatHolder[0], flags, width, height);
                    break;

                case VIDEOMODE_15_BIT_BGR:
                    depth = 15;
                    if (!(outDatHolder[0] instanceof short[])) {
                        outDatHolder[0] = new short[width * height];
                    }
                    if (!(prevDatHolder[0] instanceof short[])) {
                        prevDatHolder[0] = new short[width * height];
                    }
                    decode15to15(in, (short[]) outDatHolder[0], (short[]) prevDatHolder[0], flags, width, height);
                    break;
                case VIDEOMODE_16_BIT_BGR:
                    depth = 16;
                    if (!(outDatHolder[0] instanceof short[])) {
                        outDatHolder[0] = new short[width * height];
                    }
                    if (!(prevDatHolder[0] instanceof short[])) {
                        prevDatHolder[0] = new short[width * height];
                    }
                    decode16to16(in, (short[]) outDatHolder[0], (short[]) prevDatHolder[0], flags, width, height);
                    break;
                case VIDEOMODE_32_BIT_BGR:
                    depth = 32;
                    if (!(outDatHolder[0] instanceof int[])) {
                        outDatHolder[0] = new short[width * height];
                    }
                    if (!(prevDatHolder[0] instanceof int[])) {
                        prevDatHolder[0] = new short[width * height];
                    }
                    decode32to32(in, (int[]) outDatHolder[0], (int[]) prevDatHolder[0], flags, width, height);
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported video format " + videoFormat);
            }
        } catch (IOException ex) {
            //System.out.println("ZMBVCodecCore "+ex);
            System.err.println("ZMBVCodecCore decoding, isKeyframe=" + isKeyframe);
            ex.printStackTrace();
        }
        return isKeyframe ? -depth : depth;
    }

    private void decode8to32(ImageInputStream in, int[] outDat, int[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;
        boolean isPaletteChange = (flags & 2) != 0;

        // palette each entry contains a 32-bit entry constisting of: 
        // {palette index, red, green, blue}.
        if (palette == null) {
            palette = new int[256];
        }
        int blockSize = blockWidth * blockHeight;

        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize)) {
            blockDataBuf = new byte[max(3, blockSize)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame. 

            // Read palette 
            for (int i = 0; i < 256; i++) {
                in.readFully(buf, 0, 3);
                palette[i] = ((buf[2] & 0xff)) | ((buf[1] & 0xff) << 8) | ((buf[0] & 0xff) << 16) | (i << 24);
            }

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                outDat[i] = palette[in.readUnsignedByte()];
            }

        } else {
            // => Delta frame. 

            // Optionally update palette
            if (isPaletteChange) {
                for (int i = 0; i < 256; i++) {
                    in.readFully(buf, 0, 3);
                    palette[i] ^= ((buf[2] & 0xff)) | ((buf[1] & 0xff) << 8) | ((buf[0] & 0xff) << 16);
                }
            }

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        //    motion vectors out of bounds are used to zero blocks.
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            if (py < 0 || height <= py) {
                                for (int x = 0; x < bw2; x++) {
                                    outDat[iout++] = palette[0];
                                }
                            } else {
                                for (int x = 0; x < bw2; x++) {
                                    int px = bx + x + dx;
                                    if (0 <= px && px < width) {
                                        outDat[iout++] = palette[prevDat[px + py * width] >>> 24];
                                    } else {
                                        outDat[iout++] = palette[0];
                                    }
                                }
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int paletteIndex = buf[iblock++] & 0xff;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    paletteIndex ^= prevDat[px + py * width] >>> 24;
                                }
                                outDat[iout] = palette[paletteIndex];
                                iout++;
                            }


                        }
                    }
                }
            }
        }
    }

    private void decode8to8(ImageInputStream in, byte[] outDat, byte[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;
        boolean isPaletteChange = (flags & 2) != 0;

        // palette each entry contains a 32-bit entry constisting of: 
        // {palette index, red, green, blue}.
        if (palette == null) {
            palette = new int[256];
        }
        int blockSize = blockWidth * blockHeight;

        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize)) {
            blockDataBuf = new byte[max(3, blockSize)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame. 

            // Read palette 
            for (int i = 0; i < 256; i++) {
                in.readFully(buf, 0, 3);
                palette[i] = ((buf[2] & 0xff)) | ((buf[1] & 0xff) << 8) | ((buf[0] & 0xff) << 16) | (i << 24);
            }

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                outDat[i] = in.readByte();
            }

        } else {
            // => Delta frame. 

            // Optionally update palette
            if (isPaletteChange) {
                for (int i = 0; i < 256; i++) {
                    in.readFully(buf, 0, 3);
                    palette[i] ^= ((buf[2] & 0xff)) | ((buf[1] & 0xff) << 8) | ((buf[0] & 0xff) << 16);
                }
            }

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        //    motion vectors out of bounds are used to zero blocks.
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            if (py < 0 || height <= py) {
                                for (int x = 0; x < bw2; x++) {
                                    outDat[iout++] = 0;
                                }
                            } else {
                                for (int x = 0; x < bw2; x++) {
                                    int px = bx + x + dx;
                                    if (0 <= px && px < width) {
                                        outDat[iout++] = prevDat[px + py * width];
                                    } else {
                                        outDat[iout++] = 0;
                                    }
                                }
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                byte paletteIndex = buf[iblock++];
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    paletteIndex ^= prevDat[px + py * width];
                                }
                                outDat[iout] = paletteIndex;
                                iout++;
                            }


                        }
                    }
                }
            }
        }
    }

    private void decode15to32(ImageInputStream in, int[] outDat, int[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 2)) {
            blockDataBuf = new byte[max(3, blockSize * 2)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame. 

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readUnsignedShort();
                outDat[i] = ((bgr & (0x1f << 5)) << 6) | ((bgr & (0x1c << 5)) << 1)//green
                        | ((bgr & (0x1f << 10)) << 9) | ((bgr & (0x1c << 10)) << 4) // red
                        | ((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                        ;
            }

        } else {
            // => Delta frame. 

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            //System.out.println("blockHeaderSize=" + blockHeaderSize + " blockSize x,y=" + blockWidth + "," + blockHeight);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        //    motion vectors out of bounds are used to zero blocks.
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            if (py < 0 || height <= py) {
                                for (int x = 0; x < bw2; x++) {
                                    outDat[iout++] = 0;
                                }
                            } else {
                                for (int x = 0; x < bw2; x++) {
                                    int px = bx + x + dx;
                                    if (0 <= px && px < width) {
                                        outDat[iout++] = prevDat[px + py * width];
                                    } else {
                                        outDat[iout++] = 0;
                                    }
                                }
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock++] & 0xff)) | ((buf[iblock++] & 0xff) << 8);
                                int rgb = ((bgr & (0x1f << 5)) << 6) | ((bgr & (0x1c << 5)) << 1)//green
                                        | ((bgr & (0x1f << 10)) << 9) | ((bgr & (0x1c << 10)) << 4) // red
                                        | ((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                                        ;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    rgb ^= prevDat[px + py * width];
                                }
                                outDat[iout] = rgb;
                                iout++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void decode15to15(ImageInputStream in, short[] outDat, short[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 2)) {
            blockDataBuf = new byte[max(3, blockSize * 2)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame. 

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readUnsignedShort();
                outDat[i] = (short) bgr;
            }

        } else {
            // => Delta frame. 

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            //System.out.println("blockHeaderSize=" + blockHeaderSize + " blockSize x,y=" + blockWidth + "," + blockHeight);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        //    motion vectors out of bounds are used to zero blocks.
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            if (py < 0 || height <= py) {
                                for (int x = 0; x < bw2; x++) {
                                    outDat[iout++] = 0;
                                }
                            } else {
                                for (int x = 0; x < bw2; x++) {
                                    int px = bx + x + dx;
                                    if (0 <= px && px < width) {
                                        outDat[iout++] = prevDat[px + py * width];
                                    } else {
                                        outDat[iout++] = 0;
                                    }
                                }
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock++] & 0xff)) | ((buf[iblock++] & 0xff) << 8);
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    bgr ^= prevDat[px + py * width];
                                }
                                outDat[iout] = (short) bgr;
                                iout++;
                            }
                        }
                    }
                }
            }
        }
    }

    private void decode16to32(ImageInputStream in, int[] outDat, int[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 2)) {
            blockDataBuf = new byte[max(3, blockSize * 2)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame. 

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readUnsignedShort();
                outDat[i] = ((bgr & (0x3f << 5)) << 5) | ((bgr & (0x30 << 5)) >> 1)//green
                        | ((bgr & (0x1f << 11)) << 8) | ((bgr & (0x1c << 11)) << 3) // red
                        | ((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                        ;
            }

        } else {
            // => Delta frame. 

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    outDat[iout] = prevDat[px + py * width];
                                } else {
                                    outDat[iout] = 0;
                                }
                                iout++;
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock++] & 0xff)) | ((buf[iblock++] & 0xff) << 8);
                                int rgb = ((bgr & (0x3f << 5)) << 5) | ((bgr & (0x30 << 5)) >> 1)//green
                                        | ((bgr & (0x1f << 11)) << 8) | ((bgr & (0x1c << 11)) << 3) // red
                                        | ((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                                        ;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    rgb ^= prevDat[px + py * width];
                                }
                                outDat[iout] = (short) bgr;
                                iout++;
                            }


                        }
                    }
                }
            }
        }
    }

    private void decode16to16(ImageInputStream in, short[] outDat, short[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 2)) {
            blockDataBuf = new byte[max(3, blockSize * 2)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame. 

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readUnsignedShort();
                outDat[i] = (short) bgr;
            }

        } else {
            // => Delta frame. 

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    outDat[iout] = prevDat[px + py * width];
                                } else {
                                    outDat[iout] = 0;
                                }
                                iout++;
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 2);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock++] & 0xff)) | ((buf[iblock++] & 0xff) << 8);
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    bgr ^= prevDat[px + py * width];
                                }
                                outDat[iout] = (short) bgr;
                                iout++;
                            }


                        }
                    }
                }
            }
        }
    }

    private void decode32to32(ImageInputStream in, int[] outDat, int[] prevDat, int flags, int width, int height) throws IOException {
        boolean isKeyframe = (flags & 1) != 0;

        int blockSize = blockWidth * blockHeight;
        if (blockDataBuf == null || blockDataBuf.length < max(3, blockSize * 4)) {
            blockDataBuf = new byte[max(3, blockSize * 4)];
        }

        byte[] buf = blockDataBuf;
        if (isKeyframe) {
            // => Key frame. 

            // Process raw pixels
            for (int i = 0, n = width * height; i < n; i++) {
                int bgr = in.readInt();
                outDat[i] = bgr;
                /*((bgr & (0x3f << 5))<<5)| ((bgr & (0x30 << 5)) >>1)//green
                |((bgr & (0x1f << 11)) << 8) | ((bgr & (0x1c << 11)) << 3) // red
                |((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                ; */
            }

        } else {
            // => Delta frame. 

            // Read block headers
            int nbx = (width + blockWidth - 1) / blockWidth;
            int nby = (height + blockHeight - 1) / blockHeight;
            int blockHeaderSize = ((nbx * nby * 2 + 3) & ~3);
            if (blockHeaderBuf == null || blockHeaderBuf.length < blockHeaderSize) {
                blockHeaderBuf = new byte[blockHeaderSize];
            }
            byte[] blocks = blockHeaderBuf;
            in.readFully(blocks, 0, blockHeaderSize);

            // Process block data
            int block = 0;
            for (int by = 0; by < height; by += blockHeight) {
                int bh2 = min(height - by, blockHeight);
                for (int bx = 0; bx < width; bx += blockWidth) {
                    int bw2 = min(width - bx, blockWidth);
                    int a = blocks[block++];
                    int b = blocks[block++];
                    int dx = a >> 1;
                    int dy = b >> 1;
                    int flag = a & 1;

                    if (flag == 0) {
                        // => copy block from offset dx,dy from previous frame
                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int rgb;
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    rgb = prevDat[px + py * width];
                                } else {
                                    rgb = 0;
                                }
                                outDat[iout] = rgb;
                                iout++;
                            }

                        }
                    } else {
                        // => XOR block with data read from stream
                        in.readFully(buf, 0, bw2 * bh2 * 4);
                        int iblock = 0;

                        for (int y = 0; y < bh2; y++) {
                            int py = by + y + dy;
                            int iout = bx + (by + y) * width;
                            for (int x = 0; x < bw2; x++) {
                                int px = bx + x + dx;
                                int bgr = ((buf[iblock] & 0xff)) | ((buf[iblock + 1] & 0xff) << 8) | ((buf[iblock + 2] & 0xff) << 16) | ((buf[iblock + 3] & 0xff) << 24);
                                iblock += 4;
                                int rgb = bgr;
                                /*((bgr & (0x3f << 5))<<5)| ((bgr & (0x30 << 5)) >>1)//green
                                |((bgr & (0x1f << 11)) << 8) | ((bgr & (0x1c << 11)) << 3) // red
                                |((bgr & (0x1f << 0)) << 3) | ((bgr & (0x1c << 0)) >>> 2) // blue
                                ; */
                                if (0 <= py && py < height && 0 <= px && px < width) {
                                    rgb ^= prevDat[px + py * width];
                                }
                                outDat[iout] = rgb;
                                iout++;
                            }


                        }
                    }
                }
            }
        }
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
}
