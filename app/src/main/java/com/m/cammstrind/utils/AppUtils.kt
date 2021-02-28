package com.m.cammstrind.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import com.m.cammstrind.base.BaseActivity
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

    fun verifyBioMetricExistence(activity: BaseActivity): Boolean {
        val biometricManager = BiometricManager.from(activity)
        val authenticationTypes = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        when (biometricManager.canAuthenticate(authenticationTypes)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("Biometric", "App can authenticate using biometrics.")
                return true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("Biometric", "No biometric features available on this device.")
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("Biometric", "Biometric features are currently unavailable.")
                return false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d(
                    "Biometric",
                    "The user hasn't associated any biometric credentials with their account."
                )
                return false
            }
            else -> return false
        }
    }

}