/*
 * @(#)AbstractVideoCodecCore.java
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media;

import java.io.IOException;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * {@code AbstractVideoCodecCore}.
 *
 * @author Werner Randelshofer
 * @version $Id: AbstractVideoCodecCore.java 299 2013-01-03 07:40:18Z werner $
 */
public class AbstractVideoCodecCore {

    private byte[] byteBuf = new byte[4];

    protected void writeInt24(ImageOutputStream out, int v) throws IOException {
        byteBuf[0] = (byte) (v >>> 16);
        byteBuf[1] = (byte) (v >>> 8);
        byteBuf[2] = (byte) (v >>> 0);
        out.write(byteBuf, 0, 3);
    }

    protected void writeInt24LE(ImageOutputStream out, int v) throws IOException {
        byteBuf[2] = (byte) (v >>> 16);
        byteBuf[1] = (byte) (v >>> 8);
        byteBuf[0] = (byte) (v >>> 0);
        out.write(byteBuf, 0, 3);
    }

    protected void writeInts24(ImageOutputStream out, int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = byteBuf;
        for (int j = 0; j < len; j++) {
            int v = i[off + j];
            //b[boff++] = (byte)(v >>> 24);
            b[0] = (byte) (v >>> 16);
            b[1] = (byte) (v >>> 8);
            b[2] = (byte) (v >>> 0);
            out.write(b, 0, 3);
        }

    }

    protected void writeInts24LE(ImageOutputStream out, int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!");
        }

        byte[] b = byteBuf;
        for (int j = 0; j < len; j++) {
            int v = i[off + j];
            b[0] = (byte) (v >>> 0);
            b[1] = (byte) (v >>> 8);
            b[2] = (byte) (v >>> 16);
            out.write(b, 0, 3);
        }

    }

    protected void readInts24LE(ImageInputStream in, int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!, off=" + off + ", len=" + len);
        }

        byte[] b = byteBuf;
        for (int j = off, end = off + len; j < end; j++) {
            in.readFully(b, 0, 3);

            int v = (b[0] & 0xff) | ((b[1] & 0xff) << 8) | ((b[2] & 0xff) << 16);
            i[j] = v;
        }
    }

    protected int readInt24LE(ImageInputStream in) throws IOException {
        in.readFully(byteBuf, 0, 3);
        return ((byteBuf[2] & 0xff) << 16) | ((byteBuf[1] & 0xff) << 8) | ((byteBuf[0] & 0xff) << 0);
    }

    /** Reads 16-bit RGB and converts it to 24-bit RGB. Endian is defined by input stream. */
    protected void readRGBs565to24(ImageInputStream in, int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!, off=" + off + ", len=" + len);
        }

        for (int j = off, end = off + len; j < end; j++) {
            int v = in.readUnsignedShort();
            i[j] = ((v & 0xf800) << 8) | ((v & 0x3800) << 5)
                    | ((v & 0x07e0) << 5) | ((v & 0x0060) << 3)
                    | ((v & 0x001f) << 3) | ((v & 0x0007));
        }
    }

    /** Reads 16-bit RGB and converts it to 24-bit RGB. Endian is defined by input stream.  */
    protected int readRGB565to24(ImageInputStream in) throws IOException {
        int v = in.readUnsignedShort();
        return ((v & 0xf800) << 8) | ((v & 0x3800) << 5)
                | ((v & 0x07e0) << 5) | ((v & 0x0060) << 3)
                | ((v & 0x001f) << 3) | ((v & 0x0007));
    }
    /** Reads 16-bit RGB and converts it to 24-bit RGB. Endian is defined by input stream. */
    protected void readRGBs555to24(ImageInputStream in, int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!, off=" + off + ", len=" + len);
        }

        byte[] b = byteBuf;
        for (int j = off, end = off + len; j < end; j++) {
            int v = in.readUnsignedShort();
            i[j]=((v & (0x1f<<10)) << 9) | ((v & (0x1c<<10)) << 4) // red
                    | ((v & (0x1f<<5)) << 6) | ((v & (0x1c<<5)) << 1) // green
                    | ((v & (0x1f<<0)) << 3) | ((v & (0x1c<<0)) >> 2); // blue;
        }
    }

    /** Reads 16-bit RGB and converts it to 24-bit RGB. Endian is defined by input stream.  */
    protected int readRGB555to24(ImageInputStream in) throws IOException {
        int v = in.readUnsignedShort();
      return((v & (0x1f<<10)) << 9) | ((v & (0x1c<<10)) << 4) // red
                    | ((v & (0x1f<<5)) << 6) | ((v & (0x1c<<5)) << 1) // green
                    | ((v & (0x1f<<0)) << 3) | ((v & (0x1c<<0)) >> 2); // blue;
    }
    /** Reads 16-bit RGB and converts it to 24-bit RGB. Endian is defined by input stream. */
    protected void readRGBs555to24LE(ImageInputStream in, int[] i, int off, int len) throws IOException {
        if (off < 0 || len < 0 || off + len > i.length || off + len < 0) {
            throw new IndexOutOfBoundsException("off < 0 || len < 0 || off + len > i.length!, off=" + off + ", len=" + len);
        }

        byte[] b = byteBuf;
        for (int j = off, end = off + len; j < end; j++) {
            int v = in.readUnsignedShort();
      i[j]=((v & (0x1f<<0)) << 19) | ((v & (0x1c<<0)) << 14) // red
                    | ((v & (0x1f<<5)) << 6) | ((v & (0x1c<<5)) << 1) // green
                    | ((v & (0x1f<<10)) >>> 7) | ((v & (0x1c<<10)) >>> 12); // blue;
           /* i[j]=((v & (0x1f<<10)) << 9) | ((v & (0x1c<<10)) << 4) // red
                    | ((v & (0x1f<<5)) << 6) | ((v & (0x1c<<5)) << 1) // green
                    | ((v & (0x1f<<0)) << 3) | ((v & (0x1c<<0)) >> 2); // blue;*/
        }
    }

    /** Reads 16-bit RGB and converts it to 24-bit RGB. Endian is defined by input stream.  */
    protected int readRGB555to24LE(ImageInputStream in) throws IOException {
        int v = in.readUnsignedShort();
      return((v & (0x1f<<0)) << 19) | ((v & (0x1c<<0)) << 14) // red
                    | ((v & (0x1f<<5)) << 6) | ((v & (0x1c<<5)) << 1) // green
                    | ((v & (0x1f<<10)) >>> 7) | ((v & (0x1c<<10)) >>> 12); // blue;
/*      return((v & (0x1f<<10)) << 9) | ((v & (0x1c<<10)) << 4) // red
                    | ((v & (0x1f<<5)) << 6) | ((v & (0x1c<<5)) << 1) // green
                    | ((v & (0x1f<<0)) << 3) | ((v & (0x1c<<0)) >> 2); // blue;
 */   }
}
