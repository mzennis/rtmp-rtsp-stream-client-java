package com.pedro.rtplibrary.view

import android.graphics.Bitmap

/**
 * Created by pedro on 16/07/18.
 */
interface TakePhotoCallback {
    fun onTakePhoto(bitmap: Bitmap?)
}