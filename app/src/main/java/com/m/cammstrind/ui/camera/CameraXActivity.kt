package com.m.cammstrind.ui.camera

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaActionSound
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.m.cammstrind.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_camerax.*
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class CameraXActivity:AppCompatActivity() {
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private var currentCamLens = CAMERA_BACK
    private var flashMode: Int = ImageCapture.FLASH_MODE_OFF
    private var cameraClickSound: Boolean = false

    companion object {
        private const val TAG = "CameraX"
        //private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val CAMERA_FRONT = "frontCamera"
        private const val CAMERA_BACK = "backCamera"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.m.cammstrind.R.layout.activity_camerax)
        cameraClickSound = intent.getBooleanExtra("camera_click_sound", false)
        startCamera()
        findViewById<ImageView>(com.m.cammstrind.R.id.btn_capture).setOnClickListener { takePhoto() }
        findViewById<ImageView>(com.m.cammstrind.R.id.btn_flash_mode).setOnClickListener { enableFlash() }
        findViewById<ImageView>(com.m.cammstrind.R.id.btn_rotate_camera).setOnClickListener { toggleCamera() }
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        // Create time-stamped output file to hold the image
        /* val photoFile = File(
             outputDirectory,
             SimpleDateFormat(
                 FILENAME_FORMAT, Locale.US
             ).format(System.currentTimeMillis()) + ".jpg"
         )*/

        val photoFile = File(
            outputDirectory,
            "capture.jpg"
        )

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        if (cameraClickSound) {
            try {
                val sound = MediaActionSound()
                sound.play(MediaActionSound.SHUTTER_CLICK)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        findViewById<ImageView>(com.m.cammstrind.R.id.iv_camera_effect).animate().alpha(0.3F).duration = 500

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        baseContext,
                        "Photo capture failed: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    Handler(Looper.getMainLooper()).postDelayed({
                        val bitmap = BitmapUtils.getBitmapFromURIWithRotation(this@CameraXActivity, savedUri)
                        bitmap?.let {
                            postImagePick(bitmap)
                        }
                    }, 0)
                    val msg = "Photo capture succeeded: $savedUri"
                    //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().setFlashMode(flashMode)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }

            // Select back camera as a default
            var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            if (currentCamLens == CAMERA_FRONT) {
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(com.m.cammstrind.R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        override fun analyze(image: ImageProxy) {

            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)

            image.close()
        }
    }

    private fun toggleCamera() {
        if (currentCamLens == CAMERA_BACK) {
            currentCamLens = CAMERA_FRONT
            startCamera()
        } else {
            currentCamLens = CAMERA_BACK
            startCamera()
        }
    }

    private fun enableFlash() {
        when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> {
                flashMode = ImageCapture.FLASH_MODE_ON
                findViewById<ImageView>(com.m.cammstrind.R.id.btn_flash_mode).setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        com.m.cammstrind.R.drawable.ic_flash_on
                    )
                )
            }
            ImageCapture.FLASH_MODE_ON -> {
                flashMode = ImageCapture.FLASH_MODE_OFF
                findViewById<ImageView>(com.m.cammstrind.R.id.btn_flash_mode).setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        com.m.cammstrind.R.drawable.ic_flash_off
                    )
                )
            }
        }
        startCamera()
    }

    private fun postImagePick(bitmap: Bitmap) {
        val uri = BitmapUtils.getUri(this, bitmap)
        bitmap.recycle()
        val intent = Intent()
        intent.putExtra("selectedCameraBitmap", uri.toString())
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        setResult(Activity.RESULT_CANCELED, null)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}