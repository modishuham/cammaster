package com.m.cammstrind.ui.docDetail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.utils.AppUtils
import com.m.cammstrind.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_doc_detail.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class DocDetailFragment : Fragment() {

    private var docPath: String = ""
    private var docName: String = ""
    private var docSize: String? = ""
    private var docTime: String? = ""
    private var mBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doc_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppAnalytics.trackScreenLaunch("DocDetail")
        docName = arguments?.getString("docName").toString()
        docPath = arguments?.getString("docPath").toString()
        docSize = arguments?.getString("docSize")
        docTime = arguments?.getString("docTime")
        tv_doc_name_detail.text = docName
        docSize?.let {
            tv_doc_size.text = AppUtils.getFileSize(it.toLong())
        }
        docTime?.let {
            tv_doc_time.text = AppUtils.getDateForDurationEvent(it.toLong())
        }

        btn_share.setOnClickListener {
            shareDoc()
        }

        btn_convert_to_pdf.setOnClickListener {
            convertToPdf()
        }

        if (docName.startsWith("QR_CODE_")) {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/QRCodes"
            val mFolder = File(mediaStorageDir, docName)
            if (mFolder.exists()) {
                mBitmap = BitmapFactory.decodeFile(mFolder.absolutePath)
                mBitmap?.let {
                    iv_doc_detail.setImageBitmap(it)
                }
            }
        } else {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
            val mFolder = File(mediaStorageDir, docName)
            if (mFolder.exists()) {
                mBitmap = BitmapFactory.decodeFile(mFolder.absolutePath)
                mBitmap?.let {
                    iv_doc_detail.setImageBitmap(it)
                }
            }
        }
    }

    private fun shareDoc() {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "com.scanlibrary.provider1",
                File(docPath)
            )
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, "Share"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertToPdf() {
        try {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
            val mFolder = File(mediaStorageDir)
            if (!mFolder.exists()) {
                mFolder.mkdir()
            }
            val fileName = AppUtils.removeFileExtension(docName) + ".pdf"
            val outFile = File(mFolder, fileName)
            if (outFile.exists()) {
                DialogUtils.openConvertToPdfDialog(requireContext()) {
                    val newPdfName = DialogUtils.getPdfName().trim()
                    DialogUtils.dismissDialog()
                    if (newPdfName.isNotEmpty()) {
                        val outFile2 = File(mFolder, "$newPdfName.pdf")
                        if (outFile2.exists()) {
                            Toast.makeText(
                                requireContext(),
                                "File Name Already Exist.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            saveAsPDF("$newPdfName.pdf", mediaStorageDir)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "File Name Can't Empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                saveAsPDF(fileName, mediaStorageDir)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun saveAsPDF(
        fileName: String,
        mediaStorageDir: String
    ) {
        try {
            val stream = ByteArrayOutputStream()
            mBitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image = Image.getInstance(stream.toByteArray())

            val pageSize = Rectangle(image.width, image.height)
            val document = Document(pageSize)
            PdfWriter.getInstance(
                document, FileOutputStream(
                    File(
                        mediaStorageDir,
                        fileName
                    )
                )
            )

            image.alignment = Image.ALIGN_CENTER
            document.open()
            document.add(image)
            document.close()
            Toast.makeText(
                requireContext(),
                "Converted Successfully",
                Toast.LENGTH_SHORT
            ).show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}