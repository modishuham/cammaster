package com.m.cammstrind.ui.splash

import android.Manifest.permission.*
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.base.BaseActivity
import com.m.cammstrind.ui.home.HomeActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    private val requestPermissionCode = 999
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppAnalytics.trackScreenLaunch("Splash")
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setContentView(R.layout.activity_splash)

        iv_splash_logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_scale_in))


        handler.postDelayed({
            checkPermission()
        }, 2000)
    }

    private fun checkPermission() {
        val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            READ_MEDIA_IMAGES else WRITE_EXTERNAL_STORAGE

        val storagePermission = ContextCompat.checkSelfPermission(
            this,
            readImagePermission
        ) == PackageManager.PERMISSION_GRANTED
        val cameraPermission =
            ContextCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED
        if (storagePermission && cameraPermission) {
            openNextScreen()
        } else {
            requestPermissions(
                arrayOf(
                    readImagePermission,
                    CAMERA
                ), requestPermissionCode
            )
        }
    }

    private fun openNextScreen() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            requestPermissionCode -> {
                val storageRequestAccepted: Boolean =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                val cameraRequestAccepted: Boolean =
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (storageRequestAccepted && cameraRequestAccepted) {
                    openNextScreen()
                } else {
                    if (shouldShowRequestPermissionRationale(CAMERA)) {
                        showMessageOKCancel(
                            "Both permissions are required to access this application. Please allow both permissions."
                        ) { _, _ ->
                            checkPermission()
                        }
                    } else {
                        showMessageOKCancel(
                            "Without giving permissions you can not access this app. Please go to Settings > Apps > CamMaster > Permissions and allow both permission"
                        ) { _, _ ->
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .create()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}