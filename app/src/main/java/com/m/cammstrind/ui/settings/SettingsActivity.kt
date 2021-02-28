package com.m.cammstrind.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.m.cammstrind.R
import com.m.cammstrind.base.BaseActivity
import com.m.cammstrind.storage.AppPref
import com.m.cammstrind.storage.SharedPreferenceConstants
import com.m.cammstrind.utils.AppUtils
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {

    private var isDarkModeEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        isDarkModeEnabled = AppPref.getBoolean(SharedPreferenceConstants.DARK_MODE_ENABLED)
        switch_dark_mode.isChecked = isDarkModeEnabled

        switch_dark_mode.setOnCheckedChangeListener { _, isOn ->
            if (isOn) {
                Log.e("CamMaster", "" + isOn)
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_YES
                    )
                AppPref.putBoolean(SharedPreferenceConstants.DARK_MODE_ENABLED, true)
            } else {
                Log.e("CamMaster", "" + isOn)
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_NO
                    )
                AppPref.putBoolean(SharedPreferenceConstants.DARK_MODE_ENABLED, false)
            }
        }

        handleAppLock()
    }

    private fun handleAppLock() {
        val isBioMetricAvailable = AppUtils.verifyBioMetricExistence(this)
        if (isBioMetricAvailable) {
            cl_app_lock.visibility = View.VISIBLE
            if (AppPref.getBoolean(SharedPreferenceConstants.APP_LOCK_ENABLED)) {
                switch_app_lock.isChecked = true
            }
        } else {
            cl_app_lock.visibility = View.GONE
        }

        switch_app_lock.setOnCheckedChangeListener { _, isOn ->
            if (isOn) {
                Log.e("CamMaster", "AppLock $isOn")
                AppPref.putBoolean(SharedPreferenceConstants.APP_LOCK_ENABLED, true)
            } else {
                Log.e("CamMaster", "AppLock $isOn")
                AppPref.putBoolean(SharedPreferenceConstants.APP_LOCK_ENABLED, false)
            }
        }
    }
}