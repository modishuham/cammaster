package com.m.cammstrind

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.ads.MobileAds
import com.m.cammstrind.storage.AppPref
import com.m.cammstrind.storage.SharedPreferenceConstants

class CamMasterApp : Application() {

    private var isDarkModeEnabled: Boolean = false

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
    }

    private fun checkDarkMode() {
        isDarkModeEnabled = AppPref.getBoolean(SharedPreferenceConstants.DARK_MODE_ENABLED)
        Log.e("CamMaster", "Dark Mode $isDarkModeEnabled")
        if (isDarkModeEnabled) {
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_YES
                )
        } else {
            AppCompatDelegate
                .setDefaultNightMode(
                    AppCompatDelegate
                        .MODE_NIGHT_NO
                )
        }
    }
}