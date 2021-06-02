package com.pedro.encoder.audio

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Created by pedro on 19/01/17.
 */
interface AudioEncoderListener {

    fun onAacDataReceived(aacBuffer: ByteBuffer?, info: MediaCodec.BufferInfo?)

    fun onAudioFormatChanged(mediaFormat: MediaFormat?)
}