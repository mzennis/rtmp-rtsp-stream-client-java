package com.pedro.encoder.audio

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.pedro.encoder.BaseEncoder
import com.pedro.encoder.Frame
import com.pedro.encoder.FrameListener
import com.pedro.encoder.input.audio.MicrophoneListener
import com.pedro.encoder.utils.CodecUtil
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by pedro on 19/01/17.
 *
 * Encode PCM audio data to ACC and return in a callback
 */
class AudioEncoder(
        private val listener: AudioEncoderListener
) : BaseEncoder(), MicrophoneListener {

    companion object {
        private const val AUDIO_TAG = "AudioEncoder"
    }

    private var mBitRate = 64 * 1024 // in kbps
    private var mSampleRate = 32000 // in hz
    private var mMaxInputSize = 0
    private var mStereo = true
    private var frameListener: FrameListener? = null

    @JvmOverloads
    fun prepareAudioEncoder(
            bitRate: Int = mBitRate,
            sampleRate: Int = mSampleRate,
            isStereo: Boolean = mStereo,
            maxInputSize: Int = mMaxInputSize
    ): Boolean {
        mBitRate = bitRate
        mSampleRate = sampleRate
        mMaxInputSize = maxInputSize
        mStereo = isStereo
        isBufferMode = true
        return try {
            val encoder = chooseEncoder(CodecUtil.AAC_MIME)
            codec = if (encoder != null) {
                Log.i(AUDIO_TAG, "Encoder selected " + encoder.name)
                MediaCodec.createByCodecName(encoder.name)
            } else {
                Log.e(AUDIO_TAG, "Valid encoder not found")
                return false
            }
            val channelCount = if (isStereo) 2 else 1
            val audioFormat = MediaFormat.createAudioFormat(CodecUtil.AAC_MIME, sampleRate, channelCount)
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxInputSize)
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            codec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            running = false
            Log.i(AUDIO_TAG, "prepared")
            true
        } catch (e: Exception) {
            Log.e(AUDIO_TAG, "Create AudioEncoder failed.", e)
            this.stop()
            false
        }
    }

    fun setFrameListener(frameListener: FrameListener?) {
        this.frameListener = frameListener
    }

    override fun start(resetTs: Boolean) {
        shouldReset = resetTs
        Log.i(AUDIO_TAG, "started")
    }

    override fun stopImp() {
        Log.i(AUDIO_TAG, "stopped")
    }

    override fun reset() {
        stop(false)
        prepareAudioEncoder(mBitRate, mSampleRate, mStereo, mMaxInputSize)
        restart()
    }

    @Throws(InterruptedException::class)
    override fun getInputFrame(): Frame? {
        return if (frameListener != null) frameListener?.inputFrame else queue.take()
    }

    override fun checkBuffer(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        fixTimeStamp(bufferInfo)
    }

    override fun sendBuffer(byteBuffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        listener.onAacDataReceived(byteBuffer, bufferInfo)
    }

    /**
     * Set custom PCM (Pulse-code modulation) data.
     * Use it after prepareAudioEncoder(int sampleRate, int channel).
     * Used too with microphone.
     */
    override fun inputPCMData(frame: Frame?) {
        if (running && !queue.offer(frame)) {
            Log.i(AUDIO_TAG, "frame discarded")
        }
    }

    override fun chooseEncoder(mime: String): MediaCodecInfo? {
        val mediaCodecInfoList: List<MediaCodecInfo> = when (force) {
            CodecUtil.Force.HARDWARE -> CodecUtil.getAllHardwareEncoders(CodecUtil.AAC_MIME)
            CodecUtil.Force.SOFTWARE -> CodecUtil.getAllSoftwareEncoders(CodecUtil.AAC_MIME)
            else -> CodecUtil.getAllEncoders(CodecUtil.AAC_MIME)
        }
        Log.i(AUDIO_TAG, mediaCodecInfoList.size.toString() + " encoders found")
        for (mci in mediaCodecInfoList) {
            val name = mci.name.toLowerCase(Locale.ROOT)
            Log.i(AUDIO_TAG, "Encoder " + mci.name)
            if (name.contains("omx.google") && mediaCodecInfoList.size > 1) {
                //skip omx.google.aac.encoder if possible
                continue
            }
            return mci
        }
        return null
    }

    fun setSampleRate(sampleRate: Int) {
        this.mSampleRate = sampleRate
    }

    override fun formatChanged(mediaCodec: MediaCodec, mediaFormat: MediaFormat) {
        listener.onAudioFormatChanged(mediaFormat)
    }
}