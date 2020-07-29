package com.m.cammstrind.ui.docDetail

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.m.cammstrind.R
import com.m.cammstrind.ui.docList.DocDeleteHandler
import com.m.cammstrind.utils.AppUtils
import com.m.cammstrind.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_doc_detail.*
import java.io.File


class DocDetailFragment : Fragment() {

    private var docPath: String = ""
    private var docName: String = ""
    private var docSize: String? = ""
    private var docTime: String? = ""
    private var docPosition: Int? = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doc_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DocDeleteHandler.deletedDocPosition = -1
        docName = arguments?.getString("docName").toString()
        tv_doc_name_detail.text = docName
        val image =
            arguments?.getParcelable<Bitmap>("docImage")
        docPath = arguments?.getString("docPath").toString()
        docPosition = arguments?.getInt("docPosition")
        //iv_doc_detail.setImageBitmap(image)
        docSize = arguments?.getString("docSize")
        docTime = arguments?.getString("docTime")

        docSize?.let {
            var size = ((it.toLong() / 1024) / 1024).toFloat()
            if (size < 1) {
                size = (it.toLong() / 1024).toFloat()
                tv_doc_size.text = "$size KB"
            } else {
                tv_doc_size.text = "$size MB"
            }
        }

        docTime?.let {
            tv_doc_time.text = AppUtils.getDateForDurationEvent(it.toLong())
        }

        btn_share.setOnClickListener {
            shareDoc()
        }

        btn_delete.setOnClickListener {
            DialogUtils.openAlertDialog(requireContext(),
                "Are you sure want to delete this?",
                false,
                View.OnClickListener {
                    deleteDoc()
                    DialogUtils.dismissDialog()
                    DocDeleteHandler.deletedDocPosition = docPosition!!
                    findNavController().popBackStack()
                }
            )
        }

        val mediaStorageDir: String =
            "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
        val mFolder = File(mediaStorageDir, docName)
        if (mFolder.exists()) {
            iv_doc_detail.setImageBitmap(BitmapFactory.decodeFile(mFolder.absolutePath))
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

    private fun deleteDoc() {
        val mediaStorageDir: String =
            "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
        val mFolder = File(mediaStorageDir, docName)
        if (mFolder.exists()) {
            mFolder.delete()
        }
    }
}