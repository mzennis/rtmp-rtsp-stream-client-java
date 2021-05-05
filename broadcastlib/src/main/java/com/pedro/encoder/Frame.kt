package com.pedro.encoder

import android.graphics.ImageFormat

/**
 * Created by pedro on 17/02/18.
 */
data class Frame(
        val buffer: ByteArray,
        val offset: Int = 0,
        val size: Int = buffer.size,
        val orientation: Int = 0,
        val isFlip: Boolean = false,
        val format: Int = ImageFormat.NV21 // nv21 or yv12 supported
)