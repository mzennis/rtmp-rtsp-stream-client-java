package com.pedro.encoder

import android.media.MediaCodec
import android.media.MediaFormat

/**
 * Created by pedro on 18/09/19.
 */
interface EncoderCallback {

    @Throws(IllegalStateException::class)
    fun inputAvailable(mediaCodec: MediaCodec, inBufferIndex: Int)

    @Throws(IllegalStateException::class)
    fun outputAvailable(mediaCodec: MediaCodec, outBufferIndex: Int,
                        bufferInfo: MediaCodec.BufferInfo)

    fun formatChanged(mediaCodec: MediaCodec, mediaFormat: MediaFormat)
}