package com.m.cammstrind.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils

object BitmapUtils {

    fun getThumbnail(path: String): Bitmap {
        return ThumbnailUtils.extractThumbnail(
            BitmapFactory.decodeFile(path),
            80,
            80
        )
    }
}