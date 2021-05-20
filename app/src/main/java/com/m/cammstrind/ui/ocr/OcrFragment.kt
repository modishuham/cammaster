package com.m.cammstrind.ui.ocr

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.storage.AppPref
import com.m.cammstrind.storage.SharedPreferenceConstants
import com.m.cammstrind.utils.DialogUtils
import com.scanlibrary.BitmapUtils.getBitmap
import com.scanlibrary.CameraActivity
import com.scanlibrary.ScanConstants
import com.scanlibrary.Utils
import kotlinx.android.synthetic.main.fragment_ocr.*
import java.io.File
import java.io.IOException
import java.util.*


class OcrFragment : Fragment() {

    private var filePath: String = ""
    private var textToSpeech: TextToSpeech? = null
    private var resultText: String = ""
    private var isSpeaking: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ocr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppAnalytics.trackScreenLaunch("OCR")
        DialogUtils.openSelectImageDialog(
            requireContext(),
            cameraClickListener,
            filesClickListener
        )

        textToSpeech = TextToSpeech(
            requireContext()
        ) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech?.language = Locale.UK
            }
        }

        iv_speak.setOnClickListener {
            if (resultText.isNotEmpty()) {
                if (!isSpeaking) {
                    isSpeaking = true
                    textToSpeech?.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    isSpeaking = false
                    textToSpeech?.stop()
                }
            }
        }
    }

    private val cameraClickListener = View.OnClickListener {
        openCameraX()
        DialogUtils.dismissDialog()
    }

    private val filesClickListener = View.OnClickListener {
        openFiles()
        DialogUtils.dismissDialog()
    }

    private fun openFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, ScanConstants.PICK_FILE_REQUEST_CODE)
    }

    private fun openDeviceCamera() {
        val ocrCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val tempFileUri = FileProvider.getUriForFile(
                requireActivity().applicationContext,
                "com.scanlibrary.provider1",  // As defined in Manifest
                file
            )
            ocrCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
        } else {
            val tempFileUri = Uri.fromFile(file)
            ocrCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
        }
        startActivityForResult(ocrCameraIntent, ScanConstants.START_CAMERA_REQUEST_CODE)
    }

    private fun openCameraX() {
        startActivityForResult(
            Intent(
                requireContext(),
                CameraActivity::class.java
            ).putExtra(
                ScanConstants.CAMERA_CLICK_SOUND,
                AppPref.getBoolean(SharedPreferenceConstants.CAMERA_SOUND_ENABLED)
            ),
            ScanConstants.START_CAMERA_REQUEST_CODE
        )
    }

    private fun createImageFile(): File {
        clearTempImages()
        val file = File(
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "IMG_OCR.jpg"
        )
        filePath = file.absolutePath
        return file
    }

    private fun clearTempImages() {
        try {
            val tempFolder = File(
                requireActivity().getExternalFilesDir(null),
                Environment.DIRECTORY_PICTURES
            )
            for (f in tempFolder.listFiles()) f.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setResultByDeviceCamera() {
        getBitmap(requireActivity(), Uri.parse(filePath))?.let {
            iv_ocr_image.setImageBitmap(it)
            val image = InputImage.fromBitmap(it, 0)
            val recognizer = TextRecognition.getClient()
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    if (visionText.text != "") {
                        tv_ocr_result.text = visionText.text
                        tv_ocr_hint.visibility = View.VISIBLE
                        tv_ocr_error.visibility = View.GONE
                    } else {
                        tv_ocr_hint.visibility = View.GONE
                        tv_ocr_error.visibility = View.VISIBLE
                        tv_ocr_error.text =
                            getString(R.string.ocr_error_message)
                    }
                }
                .addOnFailureListener { e ->
                    tv_ocr_hint.visibility = View.GONE
                    tv_ocr_error.visibility = View.VISIBLE
                    tv_ocr_error.text = e.message
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanConstants.START_CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null || data.extras == null) {
                    Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack()
                }
                val uriString = data?.extras!!.getString(ScanConstants.SELECTED_CAMERA_BITMAP)
                val bitmap = Utils.getBitmap(requireActivity(), Uri.parse(uriString))
                convertImageToText(bitmap)
            } else {
                findNavController().popBackStack()
            }
        } else if (requestCode == ScanConstants.PICK_FILE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    if (data == null || data.data == null) {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().popBackStack()
                    }
                    val bitmap = getBitmap(
                        requireActivity(),
                        data!!.data!!
                    )
                    convertImageToText(bitmap!!)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                findNavController().popBackStack()
            }
        } else {
            findNavController().popBackStack()
        }
    }

    private fun convertImageToText(bitmap: Bitmap) {
        iv_ocr_image.setImageBitmap(bitmap)
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient()
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text != "") {
                    resultText = visionText.text
                    tv_ocr_result.text = visionText.text
                    tv_ocr_hint.visibility = View.VISIBLE
                    tv_ocr_error.visibility = View.GONE
                    iv_speak.visibility = View.VISIBLE
                } else {
                    iv_speak.visibility = View.GONE
                    tv_ocr_hint.visibility = View.GONE
                    tv_ocr_error.visibility = View.VISIBLE
                    tv_ocr_error.text =
                        getString(R.string.ocr_error_message)
                }
            }
            .addOnFailureListener { e ->
                tv_ocr_hint.visibility = View.GONE
                tv_ocr_error.visibility = View.VISIBLE
                tv_ocr_error.text = e.message
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        DialogUtils.dismissDialog()
    }
}