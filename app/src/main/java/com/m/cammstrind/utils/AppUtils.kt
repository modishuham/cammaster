package com.m.cammstrind.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object AppUtils {

    var isNetworkConnected: Boolean = false

    fun deleteDoc(context: Context, docName: String) {
        val mediaStorageDir: String =
            "" + context.getExternalFilesDir(null) + "/CamMaster"
        val mFolder = File(mediaStorageDir, docName)
        if (mFolder.exists()) {
            mFolder.delete()
        }
    }

    fun removeFileExtension(fileName: String): String {
        if (fileName.indexOf(".") > 0)
            return fileName.substring(0, fileName.lastIndexOf("."))
        else
            return fileName
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

    fun getFileSize(fileLength: Long): String {
        var size = ((fileLength / 1024) / 1024).toFloat()
        if (size < 1) {
            size = (fileLength / 1024).toFloat()
            return "$size KB"
        } else {
            return "$size MB"
        }
    }

    fun isInternetConnected(): Boolean {
        return isNetworkConnected
    }


    fun getNetworkState(context: Context) = try {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(object : NetworkCallback() {
                override fun onAvailable(network: Network) {
                    isNetworkConnected = true
                }

                override fun onLost(network: Network) {
                    isNetworkConnected = false
                }
            })
        } else {
            val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
            isNetworkConnected = activeNetwork?.isConnectedOrConnecting == true
        }
    } catch (e: java.lang.Exception) {

    }

}