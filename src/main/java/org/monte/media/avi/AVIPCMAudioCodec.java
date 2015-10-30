/*
 * @(#)PCMAudioCodec.java  1.0  2011-07-10
 * 
 * Copyright (c) 2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.media.avi;

import static org.monte.media.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.AudioFormatKeys.ENCODING_AVI_PCM;
import static org.monte.media.AudioFormatKeys.ENCODING_PCM_SIGNED;
import static org.monte.media.AudioFormatKeys.ENCODING_PCM_UNSIGNED;
import static org.monte.media.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.AudioFormatKeys.SignedKey;
import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.MIME_AVI;
import static org.monte.media.FormatKeys.MIME_JAVA;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.FormatKeys.MimeTypeKey;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.audio.PCMAudioCodec;

/**
 * {@code PCMAudioCodec} performs sign conversion, endian conversion and
 * quantization conversion of PCM audio data.
 * <p>
 * Does not perform sampling rate conversion or channel conversion.
 * <p>
 * FIXME Maybe create separate subclasses for AVI PCM and QuickTime PCM.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-07-10 Created.
 */
public class AVIPCMAudioCodec extends PCMAudioCodec
{
	
	private final static HashSet<String> supportedEncodings = new HashSet<String>(Arrays.asList(new String[]{ENCODING_PCM_SIGNED, ENCODING_PCM_UNSIGNED, ENCODING_AVI_PCM,}));
	
	public AVIPCMAudioCodec()
	{
		super(new Format[]{new Format(MediaTypeKey, MediaType.AUDIO,//
				EncodingKey, ENCODING_PCM_SIGNED,//
				MimeTypeKey, MIME_JAVA,//
				SignedKey, true),//
				new Format(MediaTypeKey, MediaType.AUDIO,//
						EncodingKey, ENCODING_PCM_UNSIGNED,//
						MimeTypeKey, MIME_JAVA,//
						SignedKey, false),//
						new Format(MediaTypeKey, MediaType.AUDIO,//
								EncodingKey, ENCODING_AVI_PCM,//
								MimeTypeKey, MIME_AVI,//
								SignedKey, false, SampleSizeInBitsKey, 8),//
								new Format(MediaTypeKey, MediaType.AUDIO,//
										EncodingKey, ENCODING_AVI_PCM,//
										MimeTypeKey, MIME_AVI,//
										ByteOrderKey, ByteOrder.LITTLE_ENDIAN, SignedKey, true, SampleSizeInBitsKey, 16),//
										new Format(MediaTypeKey, MediaType.AUDIO,//
												EncodingKey, ENCODING_AVI_PCM,//
												MimeTypeKey, MIME_AVI,//
												ByteOrderKey, ByteOrder.LITTLE_ENDIAN, SignedKey, true, SampleSizeInBitsKey, 24),//
												new Format(MediaTypeKey, MediaType.AUDIO,//
														EncodingKey, ENCODING_AVI_PCM,//
														MimeTypeKey, MIME_AVI,//
														ByteOrderKey, ByteOrder.LITTLE_ENDIAN, SignedKey, true, SampleSizeInBitsKey, 32),//
		});
		name = "AVI PCM Codec";
	}
	
}
