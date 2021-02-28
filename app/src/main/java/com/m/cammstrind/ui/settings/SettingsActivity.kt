package com.m.cammstrind.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.m.cammstrind.R
import com.m.cammstrind.base.BaseActivity
import com.m.cammstrind.storage.AppPref
import com.m.cammstrind.storage.SharedPreferenceConstants
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
                Log.e("CamMaster",""+isOn)
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_YES
                    )
                AppPref.putBoolean(SharedPreferenceConstants.DARK_MODE_ENABLED, true)
            } else {
                Log.e("CamMaster",""+isOn)
                AppCompatDelegate
                    .setDefaultNightMode(
                        AppCompatDelegate
                            .MODE_NIGHT_NO
                    )
                AppPref.putBoolean(SharedPreferenceConstants.DARK_MODE_ENABLED, false)
            }
        }
    }
}