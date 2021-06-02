package com.pedro.encoder.video

import android.media.MediaCodec
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 * Created by pedro on 20/01/17.
 */
interface VideoEncoderListener {

    fun onSpsPpsReceived(sps: ByteBuffer?, pps: ByteBuffer?)

    fun onSpsPpsVpsReceived(sps: ByteBuffer?, pps: ByteBuffer?, vps: ByteBuffer?)

    fun onVideoDataReceived(h264Buffer: ByteBuffer?, info: MediaCodec.BufferInfo?)

    fun onVideoFormatChanged(mediaFormat: MediaFormat?)
}