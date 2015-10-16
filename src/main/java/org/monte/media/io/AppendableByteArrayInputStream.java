/*
 * @(#)AppendableByteArrayInputStream.java  1.0  2011-08-28
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.io;

import java.io.ByteArrayInputStream;
import java.util.zip.Adler32;

/**
 * A {@code ByteArrayInputStream} which allows to replace the byte buffer underneath.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-28 Created.
 */
public class AppendableByteArrayInputStream extends ByteArrayInputStream {

    public AppendableByteArrayInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
        //  System.out.println("AppendableByteArrayInputStream   pos="+pos+" count="+count);
    }

    public AppendableByteArrayInputStream(byte[] buf) {
        super(buf);
        // System.out.println("AppendableByteArrayInputStream   pos="+pos+" count="+count);
    }

    @Override
    public synchronized int read() {
        int b = super.read();
        /*
        if (b!=-1) {
        String hex="0"+Integer.toHexString(b);
        System.out.println(hex.substring(hex.length()-2));
        }*/
        return b;
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) {
        // System.out.println("AppendableByteArrayInputStream.read...   pos="+pos+" count="+count);
        int count = super.read(b, off, len);
        /*
        if (count>0) {
        for (int i=0;i<count;i++) {
        if (i%16==15) System.out.println();
        String hex="0"+Integer.toHexString(b[i]);
        System.out.print(hex.substring(hex.length()-2));
        }
        System.out.println("   pos="+pos+" count="+this.count);
        }*/
        return count;
    }

    /** Appends new data to the buffer. 
     * 
     * @param buf Data.
     * @param offset Offset in the data.
     * @param length Length of the data.
     * @param discard True if data which has already been read can be discarded.
     */
    public void appendBuffer(byte[] buf, int offset, int length, boolean discard) {
        if (discard) {
            if (this.buf.length >= count - pos + length) {
                // => the buffer has enough space for the existing data and the new data
                System.arraycopy(this.buf, pos, this.buf, 0, count - pos);
                System.arraycopy(buf, offset, this.buf, count - pos, length);
                this.count = count - pos + length;
                this.pos = 0;
                this.mark = 0;
            } else {
                // => the buffer does not have enough space for the new data
                byte[] newBuf = new byte[(count - pos + length + 31) / 32 * 32];
                System.arraycopy(this.buf, pos, newBuf, 0, count - pos);
                System.arraycopy(buf, offset, newBuf, count - pos, length);
                this.buf = newBuf;
                this.count = count - pos + length;
                this.pos = 0;
                this.mark = 0;
            }
        } else {
            if (this.buf.length >= count + length) {
                // => the buffer has enough space for the existing data and the new data
                System.arraycopy(buf, offset, this.buf, count, length);
                this.count = count + length;
            } else {
                // => the buffer does not have enough space for the new data
                byte[] newBuf = new byte[(this.buf.length + length + 31) / 32 * 32];
                System.arraycopy(this.buf, 0, newBuf, 0, count);
                System.arraycopy(buf, offset, newBuf, count, length);
                this.buf = newBuf;
                this.count = count + length;
            }
        }

        //System.out.println("AppendableByteArrayInputStream.appendBuffer   pos="+pos+" count="+count);
    }

    /** Sets the buffer and resets the stream. 
     * This will overwrite the data array in the buffer, if it is large enough.
     * Otherwise it will create a new data array and copy the data into it.
     * 
     * @param buf Data.
     * @param offset Offset in the data.
     * @param length Length of the data.
     */
    public void setBuffer(byte[] buf, int offset, int length) {
        if (this.buf.length >= length) {
            // => the buffer has enough space for the existing data and the new data
            System.arraycopy(buf, offset, this.buf, 0, length);
            this.count = length;
            this.pos = 0;
            this.mark = 0;
        } else {
            // => the buffer may not be overwritten or does not have enough space for the new data
            this.buf = null;
            this.buf = new byte[(length + 31) & ~31];
            System.arraycopy(buf, offset, this.buf, 0, length);
            this.count = length;
            this.pos = 0;
            this.mark = 0;

        }
    }

    public static void main(String[] args) {
        byte[] b = new byte[5];
        int count = 0;
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) count++;
        }
        AppendableByteArrayInputStream in = new AppendableByteArrayInputStream(b);

        for (int j = 0; j < 3; j++) {
            System.out.println(in.read());
        }

        b = new byte[4];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) count++;
        }
        in.appendBuffer(b, 0, b.length, true);
        for (int j = 0; j < 3; j++) {
            System.out.println(in.read());
        }
        b = new byte[6];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) count++;
        }
        in.appendBuffer(b, 0, b.length, true);

        for (int d = in.read(); d >= 0; d = in.read()) {
            System.out.println(d);
        }
    }
}
