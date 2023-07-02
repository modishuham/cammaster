package com.m.cammstrind.ui.pdfViewer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_pdf_detail.pdfView
import java.io.IOException

const val PDF_REQUEST_CODE = 1010

class PdfViewerFragment : BaseFragment() {

    private var mView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_pdf_viewer, container, false)
            AppAnalytics.trackScreenLaunch("PdfViewer")
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        startActivityForResult(intent, PDF_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_REQUEST_CODE) {
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
                    pdfView.fromUri(data!!.data).defaultPage(0).load()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

}