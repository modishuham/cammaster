package com.m.cammstrind.ui.docDetail

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.m.cammstrind.R
import kotlinx.android.synthetic.main.fragment_doc_detail.*
import java.io.File


class DocDetailFragment : Fragment() {

    private var docPath:String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_doc_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_doc_name_detail.text = arguments?.getString("docName")
        val image =
            arguments?.getParcelable<Bitmap>("docImage")
        docPath = arguments?.getString("docPath").toString()
        iv_doc_detail.setImageBitmap(image)

        btn_share.setOnClickListener {
            shareDoc()
        }
    }

    private fun shareDoc() {
        val intent = Intent(Intent.ACTION_SEND)
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "com.m.cammstrind.FileProvider",
            File( docPath)
        )
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/*"
        startActivity(intent)
    }
}