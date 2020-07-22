package com.m.cammstrind.ui.home

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import com.m.cammstrind.R
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.*
import java.util.*

class HomeFragment : Fragment() {

    private val requestCode = 99
    private lateinit var adapter: DocsAdapter
    private var docsList = arrayListOf<DOC>()
    private var mView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_home, container, false)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnCamera.setOnClickListener { openCamera() }
        btnGalary.setOnClickListener { openGalary() }
        if (docsList.isEmpty()) {
            progressBar.visibility = View.VISIBLE
            adapter = DocsAdapter()
            rv_docs.adapter = adapter
            getSavedImages()
        }
    }

    private fun openCamera() {
        val preference = ScanConstants.OPEN_CAMERA
        val intent = Intent(requireContext(), ScanActivity::class.java)
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference)
        startActivityForResult(intent, requestCode)

    }

    private fun openGalary() {
        val preference = ScanConstants.OPEN_MEDIA
        val intent = Intent(requireContext(), ScanActivity::class.java)
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference)
        startActivityForResult(intent, requestCode)
    }

    private fun saveReceivedImage(
        bitmap: Bitmap?,
        imageName: String,
        imageType: String
    ) {
        try {
            if (imageType == "IMG") {
                val outStream: FileOutputStream
                val mediaStorageDir: String =
                    "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
                val mFolder = File(mediaStorageDir)
                if (!mFolder.exists()) {
                    mFolder.mkdir()
                }
                val outFile = File(mFolder, "$imageName.png")
                outStream = FileOutputStream(outFile)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
                outStream.close()
                showImageInGalary(outFile.toString())
            } else {
                val mediaStorageDir: String =
                    "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
                val mFolder = File(mediaStorageDir)
                if (!mFolder.exists()) {
                    mFolder.mkdir()
                }
                val stream = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val image = Image.getInstance(stream.toByteArray())

                val pageSize = Rectangle(image.width, image.height)
                val document = Document(pageSize)
                PdfWriter.getInstance(
                    document, FileOutputStream(
                        File(
                            mediaStorageDir,
                            "$imageName.pdf"
                        )
                    )
                )

                image.alignment = Image.ALIGN_CENTER
                document.open()
                document.add(image)
                document.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getSavedImages() {
        Thread(Runnable {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
            val mFolder = File(mediaStorageDir)
            if (mFolder.exists()) {
                val allFiles: Array<File>? = mFolder.listFiles { _, name ->
                    name.endsWith(".jpg") ||
                            name.endsWith(".jpeg") ||
                            name.endsWith(".png") ||
                            name.endsWith(".pdf")
                }
                if (allFiles == null || allFiles.isEmpty()) {
                    progressBar.visibility = View.GONE
                    cl_no_data.visibility = View.VISIBLE
                    return@Runnable
                }

                for (file in allFiles.asList()) {
                    try {
                        val path = file.path
                        if (path.endsWith(".pdf")) {
                            val doc = DOC(file.name, file.extension, null, path)
                            docsList.add(doc)
                        } else {
                            val doc = DOC(file.name, file.extension, getThumbnail(path), path)
                            docsList.add(doc)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                requireActivity().runOnUiThread {
                    adapter.setDocsList(docsList)
                    mView!!.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                }
            }
        }).start()
    }

    private fun getThumbnail(path: String): Bitmap {
        return ThumbnailUtils.extractThumbnail(
            BitmapFactory.decodeFile(path),
            80,
            80
        );
    }


    private fun showImageInGalary(filePath: String) {
        try {
            ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "image")
                put(MediaStore.Images.Media.DISPLAY_NAME, "image")
                put(MediaStore.Images.Media.MIME_TYPE, "image/*")
                put("_data", filePath)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, filePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                requireContext().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    this
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.extras!!.getParcelable(ScanConstants.SCANNED_RESULT)
            val imageType: String = data.extras!!.get(ScanConstants.SELECTED_BITMAP_TYPE) as String
            val imageName: String = data.extras!!.get(ScanConstants.SELECTED_BITMAP_NAME) as String
            var bitmap: Bitmap? = null
            try {
                bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }
                saveReceivedImage(bitmap, imageName, imageType)
                if (uri != null) {
                    requireContext().contentResolver.delete(uri, null, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}