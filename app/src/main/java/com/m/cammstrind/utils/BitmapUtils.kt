package com.m.cammstrind.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils

object BitmapUtils {

    fun getThumbnail(path: String): Bitmap? {
        return try {
            ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(path),
                100,
                100
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}