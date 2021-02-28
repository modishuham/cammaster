package com.m.cammstrind.storage

import android.content.Context
import android.content.SharedPreferences
import com.m.cammstrind.CamMasterApp

object AppPref {

    private var sharedPreferences: SharedPreferences = CamMasterApp.applicationContext().getSharedPreferences(
        "CamMasterPreferences", Context.MODE_PRIVATE
    )

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit()?.putBoolean(key, value)?.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }
}