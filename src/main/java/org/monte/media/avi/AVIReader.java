/*
 * @(#)AVIReader.java  1.0  2011-08-24
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.avi;

import org.monte.media.Codec;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.EnumSet;
import org.monte.media.math.Rational;
import org.monte.media.Format;
import org.monte.media.Buffer;
import org.monte.media.MovieReader;
import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;
import static java.lang.Math.*;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;
import org.monte.media.BufferFlag;
import org.monte.media.Registry;
import static org.monte.media.BufferFlag.*;

/**
 * Provides high-level support for decoding and reading audio and video samples
 * from an AVI 1.0 file.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-24 Created.
 */
public class AVIReader extends AVIInputStream implements MovieReader {

    public final static Format AVI = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI);
    private Rational movieDuration = null;

    public AVIReader(ImageInputStream in) throws IOException {
        super(in);
    }

    public AVIReader(File file) throws IOException {
        super(file);
    }

    @Override
    public Format getFileFormat() throws IOException {
        return AVI;
    }

    @Override
    public Format getFormat(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).format;
    }

    /**
     * Reads a chunk of media data from the specified track.
     * <p>
     * If the track is a video track with palette change "..PC" chunks,
     * then the body of the palette change chunk can be found in the buffer.header.
     * 
     * @param track The track number.
     * @param buffer The buffer for the media data.
     * @throws IOException 
     */
    @Override
    public void read(int track, Buffer buffer) throws IOException {
        ensureRealized();
        Track tr = tracks.get(track);
        if (tr.readIndex >= tr.samples.size()) {
            buffer.setFlagsTo(END_OF_MEDIA, DISCARD);
            buffer.length = 0;
            return;
        }

        Sample s = tr.samples.get((int) tr.readIndex);
        in.seek(s.offset);
        {
            byte[] b;
            if (buffer.data instanceof byte[]) {
                b = (byte[]) buffer.data;
                if (b.length < s.length) {
                    buffer.data = b = new byte[(((int) s.length + 1023) / 1024) * 1024];
                }
            } else {
                buffer.data = b = new byte[(((int) s.length + 1023) / 1024) * 1024];
            }
            in.readFully(b, 0, (int) s.length);
        }
        buffer.offset = 0;
        buffer.length = (int) s.length;

        if (s.header != null) {
        {
            byte[] b;
            if (buffer.header instanceof byte[]) {
                b = (byte[]) buffer.data;
                if (b.length != s.header.length) {
                    buffer.header = b = new byte[(((int) s.header.length + 1023) / 1024) * 1024];
                }
            } else {
                buffer.header = b = new byte[(((int) s.header.length + 1023) / 1024) * 1024];
            }
            in.readFully(b, 0, (int) s.header.length);
        }
        } else {
            buffer.header = null;
        }



        switch (tr.mediaType) {
            case AUDIO: {
                Format af = tr.format;
                buffer.sampleCount = buffer.length / af.get(FrameSizeKey);
            }
            break;
            case VIDEO: {
                buffer.sampleCount = 1;
            }
            break;
            case MIDI:
            case TEXT:
            default:
                throw new UnsupportedOperationException("Unsupported media type " + tr.mediaType);
        }
        buffer.format = tr.format;
        buffer.track = track;
        buffer.sampleDuration = new Rational(tr.scale, tr.rate);
        buffer.timeStamp = new Rational((s.timeStamp + tr.startTime) * tr.scale, tr.rate);
        buffer.flags = s.isKeyframe ? EnumSet.of(KEYFRAME) : EnumSet.noneOf(BufferFlag.class);



        tr.readIndex++;

    }

    /**
     * Decodes the PC palette change chunk.
     */
    private void decodePC(byte[] pc, int offset, int length, byte[] r, byte[] g, byte[] b) {
        offset += 4; // FIXME - Parse firstEntry, numEntries and flags from PC
        for (int i = 0; i < 256; i++) {
            r[i] = pc[offset + i * 4];
            g[i] = pc[offset + i * 4 + 1];
            b[i] = pc[offset + i * 4 + 2];
        }

    }

    /**
     * Reads an image.
     *
     * @param track The track number
     * @param img An image that can be reused if it fits the media format of the
     * track. Pass null to create a new image on each read.
     * @return An image or null if the end of the media has been reached.
     * @throws IOException
     */
    public BufferedImage read(int track, BufferedImage img) throws IOException {
        Track tr = tracks.get(track);
        if (tr.inputBuffer == null) {
            tr.inputBuffer = new Buffer();
        }
        if (tr.codec == null) {
            createCodec(tr);
        }
        Buffer buf = new Buffer();
        buf.data = img;
        do {
            read(track, tr.inputBuffer);
            // FIXME - We assume a one-step codec here!
            tr.codec.process(tr.inputBuffer, buf);
        } while (buf.isFlag(DISCARD) && !buf.isFlag(END_OF_MEDIA));

        if (tr.inputBuffer.isFlag(END_OF_MEDIA)) {
            return null;
        }

        return (BufferedImage) buf.data;
    }

    private void createCodec(Track tr) {
        Format fmt = tr.format;
        Codec codec = createCodec(fmt);
        String enc = fmt.get(EncodingKey);
        if (codec == null) {
            throw new UnsupportedOperationException("Track " + tr + " no codec found for format " + fmt);
        } else {
            if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
                if (null == codec.setInputFormat(fmt)) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support input format " + fmt + ". codec=" + codec);
                }
                Format outFormat = fmt.prepend(MediaTypeKey, MediaType.VIDEO,//
                        MimeTypeKey, MIME_JAVA,
                        EncodingKey, ENCODING_BUFFERED_IMAGE, DataClassKey, BufferedImage.class);
                if (null == codec.setOutputFormat(outFormat)) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support output format " + outFormat + ". codec=" + codec);
                }
            }
        }

        tr.codec = codec;
    }

    private Codec createCodec(Format fmt) {
        Codec[] codecs = Registry.getInstance().getDecoders(fmt.prepend(MimeTypeKey, MIME_AVI));
        return codecs.length == 0 ? null : codecs[0];
    }

    @Override
    public Rational getReadTime(int track) throws IOException {
        Track tr = tracks.get(track);
        if (tr.samples.size() > tr.readIndex) {
            Sample s = tr.samples.get((int) tr.readIndex);
            return new Rational((s.timeStamp + tr.startTime) * tr.scale, tr.rate);
        }
        return new Rational(0, 1);
    }

    @Override
    public int nextTrack() throws IOException {
        ensureRealized();
        Rational ts = new Rational(Integer.MAX_VALUE, 1);
        int nextTrack = -1;
        for (int i = 0, n = tracks.size(); i < n; i++) {
            Track tr = tracks.get(i);

            if (tr.samples.isEmpty()) {
                continue;
            }

            Sample currentSample = tr.readIndex < tr.samples.size() ? tr.samples.get((int) tr.readIndex) : tr.samples.get(tr.samples.size() - 1);

            long readTimeStamp = currentSample.timeStamp;
            if (tr.readIndex >= tr.samples.size()) {
                readTimeStamp += currentSample.duration;
            }

            Rational trts = new Rational((readTimeStamp + tr.startTime) * tr.scale, tr.rate);
            if (trts.compareTo(ts) < 0 && tr.readIndex < tr.samples.size()) {
                ts = trts;
                nextTrack = i;
            }
        }
        return nextTrack;
    }

    @Override
    public Rational getDuration() {
        try {
            ensureRealized();
        } catch (IOException ex) {
            ex.printStackTrace();
            return new Rational(0, 1);
        }
        if (movieDuration == null) {
            Rational maxDuration = new Rational(0, 1);
            for (Track tr : tracks) {
                Rational trackDuration = new Rational((tr.length * tr.scale + tr.startTime), tr.rate);
                if (maxDuration.compareTo(trackDuration) < 0) {
                    maxDuration = trackDuration;
                }
            }
            movieDuration = maxDuration;
        }
        return movieDuration;
    }

    @Override
    public Rational getDuration(int track) {
        Track tr = tracks.get(track);
        Rational trackDuration = new Rational((tr.length * tr.scale + tr.startTime), tr.rate);
        return trackDuration;
    }

    @Override
    public long getTimeScale(int track) {
        return tracks.get(track).rate;
    }

    @Override
    public long timeToSample(int track, Rational time) {
        Track tr = tracks.get(track);
        // This only works, if all samples contain only one sample!
        // FIXME - We foolishly assume that only audio tracks have more than one
        // sample in a frame.
        // FIXME - We foolishly assume that all samples have a sampleDuration != 0.
        long index = time.getNumerator() * tr.rate / time.getDenominator() / tr.scale - tr.startTime;
        if (tr.mediaType == AVIMediaType.AUDIO) {
            int count = 0;
            // FIXME This is very inefficient, perform binary search with sample.timestamp
            // this will work for all media types!
            for (int i = 0, n = tr.samples.size(); i < n; i++) {
                long d = tr.samples.get(i).duration * tr.scale; // foolishly assume that sampleDuration = sample count
                if (count + d > index) {
                    index = i;
                    break;
                }
                count += d;

            }
        }

        return max(0, min(index, tr.samples.size()));
    }

    @Override
    public Rational sampleToTime(int track, long sampleIndex) throws IOException {
        ensureRealized();
        Track tr = tracks.get(track);
        Sample sample = tr.samples.get((int) max(0, min(tr.samples.size() - 1, sampleIndex)));
        long time = (tr.startTime + sample.timeStamp) * tr.scale;//
        if (sampleIndex >= tr.samples.size()) {
            time += sample.duration * tr.scale;
        }
        return new Rational(time, tr.rate);
    }

    @Override
    public void setMovieReadTime(Rational newValue) throws IOException {
        ensureRealized();
        for (int t = 0, n = tracks.size(); t < n; t++) {
            Track tr = tracks.get(t);
            int sample = (int) min(timeToSample(t, newValue), tr.samples.size() - 1);
            if (tr.readIndex > sample) {
                tr.readIndex = 0;
            }
            for (; sample >= 0 && sample > tr.readIndex && !tr.samples.get(sample).isKeyframe; sample--);
            tr.readIndex = sample;
        }
    }

    @Override
    public int findTrack(int fromTrack, Format format) throws IOException {
        for (int i = fromTrack, n = getTrackCount(); i < n; i++) {
            if (getFormat(i).matches(format)) {
                return i;
            }
        }
        return -1;
    }
}
