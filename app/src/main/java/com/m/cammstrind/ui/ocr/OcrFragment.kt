package com.m.cammstrind.ui.ocr

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.m.cammstrind.R
import kotlinx.android.synthetic.main.fragment_ocr.*
import java.io.File
import java.io.FileInputStream

class OcrFragment : Fragment() {

    private val ocrCameraRequest = 100
    private var filePath: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ocr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ocrCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = createImageFile()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val tempFileUri = FileProvider.getUriForFile(
                requireActivity().applicationContext,
                "com.scanlibrary.provider1",  // As defined in Manifest
                file!!
            )
            ocrCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
        } else {
            val tempFileUri = Uri.fromFile(file)
            ocrCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempFileUri)
        }
        startActivityForResult(ocrCameraIntent, ocrCameraRequest)
    }

    private fun createImageFile(): File? {
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

    private fun getBitmap(path: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val f = File(path)
            val options = BitmapFactory.Options()
            options.inSampleSize = 2
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
            bitmap?.let {
                bitmap = getRotatedBitmap(it, FileInputStream(f))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun getRotatedBitmap(original: Bitmap, inputStream: FileInputStream): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val ei =
                ExifInterface(inputStream)
            val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
            val rotatedBitmap: Bitmap
            rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(original, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(original, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(original, 270f)
                ExifInterface.ORIENTATION_NORMAL -> original
                else -> original
            }
            return rotatedBitmap
        } else {
            return original
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ocrCameraRequest && resultCode == Activity.RESULT_OK) {
            getBitmap(filePath)?.let {
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
        } else {
            findNavController().popBackStack()
        }
    }
}