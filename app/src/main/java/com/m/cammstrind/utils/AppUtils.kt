package com.m.cammstrind.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object AppUtils {

    fun deleteDoc(context: Context, docName: String) {
        val mediaStorageDir: String =
            "" + context.getExternalFilesDir(null) + "/CamMaster"
        val mFolder = File(mediaStorageDir, docName)
        if (mFolder.exists()) {
            mFolder.delete()
        }
    }

    fun getDateForDurationEvent(timeStamp: Long): String? {
        return try {
            val sdf =
                SimpleDateFormat("dd/MM/yyyy hh.mm aa")
            val netDate = Date(timeStamp)
            sdf.format(netDate)
        } catch (ex: Exception) {
            ""
        }
    }

}