package com.pedro.encoder.input.audio

import com.pedro.encoder.Frame

/**
 * Created by pedro on 19/01/17.
 */
interface MicrophoneListener {

    fun inputPCMData(frame: Frame?)
}