/*
 * @(#)TechSmithCodec.java 
 *
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.avi;

import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.Point;
import java.awt.image.DirectColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.ColorModel;
import java.util.Hashtable;
import org.monte.media.Format;
import java.awt.image.WritableRaster;
import java.awt.Rectangle;
import org.monte.media.AbstractVideoCodec;
import org.monte.media.Buffer;
import org.monte.media.io.SeekableByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.monte.media.BufferFlag;
import static org.monte.media.VideoFormatKeys.*;
import static org.monte.media.BufferFlag.*;

/**
 * {@code TechSmithCodec} (tscc) encodes a BufferedImage as a byte[] array. <p>
 * The TechSmith codec works with AVI and QuickTime. <p> This codec supports
 * encoding from a {@code BufferedImage} into the file format, and decoding from
 * the file format to a {@code BufferedImage}. <p> <p> This codec does not
 * encode the color palette of an image. This must be done separately. <p>
 * Supported input formats: <ul> {@code Format} with
 * {@code BufferedImage.class}, any width, any height, depth=8,16 or 24. </ul>
 * Supported output formats: <ul> {@code Format} with {@code byte[].class}, same
 * width and height as input format, depth=8,16 or 24. </ul> The codec supports
 * lossless delta- and key-frame encoding of images with 8, 16 or 24 bits per
 * pixel. <p> Compression of a frame is performed in two steps: In the first,
 * step a frame is compressed line by line from bottom to top. In the second
 * step the resulting data is compressed again using zlib compression. <p> Apart
 * from the second compression step and the support for 16- and 24-bit data,
 * this encoder is identical to the {@link RunLengthCodec}. <p> Each line of a
 * frame is compressed individually. A line consists of two-byte op-codes
 * optionally followed by data. The end of the line is marked with the EOL
 * op-code. <p> The following op-codes are supported: <ul> <li>{@code 0x00 0x00}
 * <br>Marks the end of a line.</li>
 *
 * <li>{@code  0x00 0x01} <br>Marks the end of the bitmap.</li>
 *
 * <li>{@code 0x00 0x02 x y} <br> Marks a delta (skip). {@code x} and {@code y}
 * indicate the horizontal and vertical offset from the current position.
 * {@code x} and {@code y} are unsigned 8-bit values.</li>
 *
 * <li>{@code 0x00 n pixel{n} 0x00?} <br> Marks a literal run. {@code n} gives
 * the number of 8-, 16- or 24-bit pixels that follow. {@code n} must be between
 * 3 and 255. If n is odd and 8-bit pixels are used, a pad byte with the value
 * 0x00 must be added. </li> <li>{@code n pixel} <br> Marks a repetition.
 * {@code n} gives the number of times the given pixel is repeated. {@code n}
 * must be between 1 and 255. </li> </ul> Example:
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
 * References:<br/> <a
 * href="http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec"
 * >http://wiki.multimedia.cx/index.php?title=TechSmith_Screen_Capture_Codec</a><br>
 *
 *
 * @author Werner Randelshofer
 * @version $Id: TechSmithCodec.java 299 2013-01-03 07:40:18Z werner $
 */
public class TechSmithCodec extends AbstractVideoCodec {

    private TechSmithCodecCore state;
    private Object previousPixels;
    private int frameCounter;
    private Object oldPixels;
    private Object newPixels;

    public TechSmithCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 8), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 16), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 24), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 8), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 16), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 24), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE, FixedFrameRateKey, true), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 8), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 16), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 24), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 8), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 16), //
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    CompressorNameKey, COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DataClassKey, byte[].class,
                    FixedFrameRateKey, true, DepthKey, 24), //
                });
        name = "TechSmith Screen Capture";
    }

    @Override
    public void reset() {
        state = null;
        frameCounter = 0;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (state == null) {
            state = new TechSmithCodecCore();
        }
        if (in.isFlag(BufferFlag.DISCARD)) {
            out.setMetaTo(in);
            return CODEC_OK;
        }
        
        if (outputFormat.get(EncodingKey).equals(ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE)) {
            return encode(in, out);
        } else {
            return decode(in, out);
        }
    }

    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        out.length = 1;
        out.offset = 0;

        if (state == null) {
            state = new TechSmithCodecCore();
        }

        int width = outputFormat.get(WidthKey);
        int height = outputFormat.get(HeightKey);
        int inputDepth = inputFormat.get(DepthKey);
        int outputDepth = outputFormat.get(DepthKey);

        boolean isKeyFrame;
        try {
            if (outputDepth == 8) {
                if (!(newPixels instanceof byte[]) || ((byte[]) newPixels).length != width * height) {
                    newPixels = new byte[width * height];
                }
                isKeyFrame = state.decode8((byte[]) in.data, in.offset, in.length, (byte[]) newPixels, (byte[]) newPixels, width, height, false);
            } else {
                if (!(newPixels instanceof int[]) || ((int[]) newPixels).length != width * height) {
                    newPixels = new int[width * height];
                }
                if (inputDepth == 8) {
                    isKeyFrame = state.decode8((byte[]) in.data, in.offset, in.length, (int[]) newPixels, (int[]) newPixels, width, height, false);
                } else if (inputDepth == 16) {
                    isKeyFrame = state.decode16((byte[]) in.data, in.offset, in.length, (int[]) newPixels, (int[]) newPixels, width, height, false);
                } else {
                    isKeyFrame = state.decode24((byte[]) in.data, in.offset, in.length, (int[]) newPixels, (int[]) newPixels, width, height, false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        MyBufferedImage img = null;
        if (out.data instanceof MyBufferedImage) {
            img = (MyBufferedImage) out.data;
        }
        switch (outputDepth) {
            case 8: {
                int imgType = BufferedImage.TYPE_BYTE_INDEXED; // FIXME - Don't hardcode this value
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    int[] cmap = new int[256];
                    IndexColorModel icm = new IndexColorModel(8, 256, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
                    img = new MyBufferedImage(width, height, imgType, icm);
                } else {
                    MyBufferedImage oldImg = img;
                    img = new MyBufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                int[] cmap = new int[256];//state.getPalette();
                for (int i = 0; i < 256; i++) {
                    cmap[i] = 255 << 24 | i | i << 8 | i << 16;
                }
                IndexColorModel icm = new IndexColorModel(8, 256, cmap, 0, false, -1, DataBuffer.TYPE_BYTE);
                img.setColorModel(icm);
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((byte[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 15: {
                int imgType = BufferedImage.TYPE_USHORT_555_RGB;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    DirectColorModel cm = new DirectColorModel(15, 0x1f << 10, 0x1f << 5, 0x1f << 0);
                    img = new MyBufferedImage(cm, Raster.createWritableRaster(cm.createCompatibleSampleModel(width, height), new Point(0, 0)), false, new Hashtable());
                } else {
                    MyBufferedImage oldImg = img;
                    img = new MyBufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                short[] pixels = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((short[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 16: {
                //int imgType = BufferedImage.TYPE_USHORT_565_RGB;
                int imgType = BufferedImage.TYPE_INT_RGB;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    //DirectColorModel cm = new DirectColorModel(24, 0x1f << 11, 0x3f << 5, 0x1f << 0);
                    DirectColorModel cm = new DirectColorModel(24, 0xff << 16, 0xff << 8, 0xff << 0);
                    img = new MyBufferedImage(cm, Raster.createWritableRaster(cm.createCompatibleSampleModel(width, height), new Point(0, 0)), false, new Hashtable());
                } else {
                    MyBufferedImage oldImg = img;
                    img = new MyBufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                /*
                 short[] pixels = ((DataBufferUShort) img.getRaster().getDataBuffer()).getData();
                 System.arraycopy((short[]) newPixels, 0, pixels, 0, width * height);
                 */
                int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((int[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            case 24: {
                int imgType = BufferedImage.TYPE_INT_RGB;
                if (img == null || img.getWidth() != width || img.getHeight() != height || img.getType() != imgType) {
                    DirectColorModel cm = new DirectColorModel(24, 0xff << 16, 0xff << 8, 0xff << 0);
                    img = new MyBufferedImage(cm, Raster.createWritableRaster(cm.createCompatibleSampleModel(width, height), new Point(0, 0)), false, new Hashtable());
                } else {
                    MyBufferedImage oldImg = img;
                    img = new MyBufferedImage(oldImg.getColorModel(), oldImg.getRaster(), oldImg.isAlphaPremultiplied(), null);
                }
                int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                System.arraycopy((int[]) newPixels, 0, pixels, 0, width * height);
            }
            break;
            default:
                throw new UnsupportedOperationException("Unsupported depth:" + outputDepth);
        }

        out.setFlag(KEYFRAME, isKeyFrame);

        out.data = img;
        return CODEC_OK;
    }

    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        SeekableByteArrayOutputStream tmp;
        if (out.data instanceof byte[]) {
            tmp = new SeekableByteArrayOutputStream((byte[]) out.data);
        } else {
            tmp = new SeekableByteArrayOutputStream();
        }

        boolean isKeyframe = frameCounter == 0
                || frameCounter % outputFormat.get(KeyFrameIntervalKey, outputFormat.get(FrameRateKey).intValue()) == 0;
        out.setFlag(KEYFRAME, isKeyframe);
        out.clearFlag(SAME_DATA);
        frameCounter++;

        // Handle sub-image
        Rectangle r;
        int scanlineStride;
        if (in.data instanceof BufferedImage) {
            BufferedImage image = (BufferedImage) in.data;
            WritableRaster raster = image.getRaster();
            scanlineStride = raster.getSampleModel().getWidth();
            r = raster.getBounds();
            r.x -= raster.getSampleModelTranslateX();
            r.y -= raster.getSampleModelTranslateY();
            out.header = image.getColorModel();
        } else {
            r = new Rectangle(0, 0, outputFormat.get(WidthKey), outputFormat.get(HeightKey));
            scanlineStride = outputFormat.get(WidthKey);
            out.header = null;
        }
        int offset = r.x + r.y * scanlineStride;

        try {
            switch (outputFormat.get(DepthKey)) {
                case 8: {
                    byte[] pixels = getIndexed8(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }

                    if (isKeyframe) {
                        state.encodeKey8(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta8(tmp, pixels, (byte[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        } else {
                            state.encodeDelta8(tmp, pixels, (byte[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        }
                        out.clearFlag(KEYFRAME);
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (byte[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                case 16: {
                    short[] pixels = getRGB15(in); // 16-bit TSCC is actually just 15-bit
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }

                    if (isKeyframe) {
                        state.encodeKey16(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta16(tmp, pixels, (short[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        } else {
                            state.encodeDelta16(tmp, pixels, (short[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        }
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (short[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                case 24: {
                    int[] pixels = getRGB24(in);
                    if (pixels == null) {
                        out.setFlag(DISCARD);
                        return CODEC_OK;
                    }

                    if (isKeyframe) {
                        state.encodeKey24(tmp, pixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        out.setFlag(KEYFRAME);
                    } else {
                        if (in.isFlag(SAME_DATA)) {
                            state.encodeSameDelta24(tmp, pixels, (int[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        } else {
                            state.encodeDelta24(tmp, pixels, (int[]) previousPixels, outputFormat.get(WidthKey), outputFormat.get(HeightKey), offset, scanlineStride);
                        }
                        out.clearFlag(KEYFRAME);
                    }
                    if (previousPixels == null) {
                        previousPixels = pixels.clone();
                    } else {
                        System.arraycopy(pixels, 0, (int[]) previousPixels, 0, pixels.length);
                    }
                    break;
                }
                default: {
                    out.setFlag(DISCARD);
                    return CODEC_FAILED;
                }
            }

            out.format = outputFormat;
            out.data = tmp.getBuffer();
            out.offset = 0;
            out.sampleCount = 1;
            out.length = tmp.size();
            return CODEC_OK;
        } catch (IOException ex) {
            ex.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_OK;
        }
    }

    private static class MyBufferedImage extends BufferedImage {

        private ColorModel colorModel;

        public MyBufferedImage(ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied, Hashtable<?, ?> properties) {
            super(cm, raster, isRasterPremultiplied, properties);
            colorModel = cm;
        }

        public MyBufferedImage(int width, int height, int imageType, IndexColorModel cm) {
            super(width, height, imageType, cm);
            colorModel = cm;
        }

        public MyBufferedImage(int width, int height, int imageType) {
            super(width, height, imageType);
        }

        @Override
        public ColorModel getColorModel() {
            return colorModel;
        }

        public void setColorModel(ColorModel newValue) {
            this.colorModel = newValue;
        }
    }
}
