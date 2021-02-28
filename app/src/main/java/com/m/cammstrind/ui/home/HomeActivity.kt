package com.m.cammstrind.ui.home

import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.m.cammstrind.R
import com.m.cammstrind.base.BaseActivity
import com.m.cammstrind.storage.AppPref
import com.m.cammstrind.storage.SharedPreferenceConstants
import java.lang.Exception

class HomeActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        try {
            if (AppPref.getBoolean(SharedPreferenceConstants.APP_LOCK_ENABLED)) {
                if(savedInstanceState?.get("IS_APP_LOCK") != "true"){
                    handleAppLock()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("IS_APP_LOCK",""+AppPref.getBoolean(SharedPreferenceConstants.APP_LOCK_ENABLED))
    }

    private fun handleAppLock() {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    finish()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    finish()
                }
            })

        val authenticationTypes =
            BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enter phone screen lock pattern, PIN, password or fingerprint")
            .setSubtitle("Unlock CamMaster")
            .setAllowedAuthenticators(authenticationTypes)
            //.setDeviceCredentialAllowed(true)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}