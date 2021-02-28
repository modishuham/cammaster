package com.m.cammstrind.ui.pdfDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.m.cammstrind.R
import com.m.cammstrind.base.BaseFragment
import kotlinx.android.synthetic.main.activity_authentication.*
import java.io.File

class PdfDetailFragment : BaseFragment() {

    private var mView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_pdf_detail, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pdfView.fromFile(File(arguments?.get("pdfPath").toString()))
            .defaultPage(0).load()
    }

}