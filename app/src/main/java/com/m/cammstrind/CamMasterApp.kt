package com.m.cammstrind

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.m.cammstrind.storage.AppPref
import com.m.cammstrind.storage.SharedPreferenceConstants

class CamMasterApp : Application() {

    private var isLightModeEnabled: Boolean = false

    init {
        instance = this
    }

    companion object {
        private var instance: CamMasterApp? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(
            this
        ) { Log.e("CamMaster", "ADD Initialized") }
        checkDarkMode()
        System.loadLibrary("opencv_java4")
    }

    private fun checkDarkMode() {
        isLightModeEnabled = AppPref.getBoolean(SharedPreferenceConstants.LIGHT_MODE_ENABLED)
        Log.e("CamMaster", "Dark Mode $isLightModeEnabled")
        if (isLightModeEnabled) {
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_NO
                )
        } else {
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_YES
                )
        }
    }
}