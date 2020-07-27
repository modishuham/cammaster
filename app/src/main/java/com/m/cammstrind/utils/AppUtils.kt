package com.m.cammstrind.utils

import android.content.Context
import java.io.File

object AppUtils {

    fun deleteDoc(context: Context, docName: String) {
        val mediaStorageDir: String =
            "" + context.getExternalFilesDir(null) + "/CamMaster"
        val mFolder = File(mediaStorageDir, docName)
        if (mFolder.exists()) {
            mFolder.delete()
        }
    }

}