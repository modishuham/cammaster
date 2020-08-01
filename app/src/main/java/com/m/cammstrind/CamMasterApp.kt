package com.m.cammstrind

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.MobileAds

class CamMasterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(
            this
        ) { Log.e("CamMaster", "ADD Initialized") }
    }
}