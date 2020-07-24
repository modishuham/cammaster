package com.m.cammstrind.ui.pdfList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.m.cammstrind.R
import com.m.cammstrind.response.DOC
import com.m.cammstrind.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_doc_list.*
import java.io.File

class PdfListFragment: Fragment() {

    private var mView: View? = null
    private var pdfList: ArrayList<DOC> = ArrayList()
    private var adapter = PdfAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_pdf_list, container, false)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        val docType = arguments?.get("docType").toString()

        with(docType) {
            tv_list_name.text = this
        }

        if (pdfList.isEmpty()) {
            rv_doc_list.adapter = adapter
            getScannedDocsList()
        }
    }

    private fun getScannedDocsList() {
        val docsList: ArrayList<DOC> = ArrayList()
        Thread(Runnable {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
            val mFolder = File(mediaStorageDir)
            if (mFolder.exists()) {
                val allFiles: Array<File>? = mFolder.listFiles { _, name ->
                            name.endsWith(".pdf")
                }
                allFiles?.let {
                    for (file in allFiles.asList()) {
                        try {
                            val path = file.path
                            if (path.endsWith(".pdf")) {
                                val doc = DOC(
                                    file.name,
                                    file.extension,
                                    null,
                                    path
                                )
                                docsList.add(doc)
                            } else {
                                val doc = DOC(
                                    file.name, file.extension,
                                    BitmapUtils.getThumbnail(path), path
                                )
                                docsList.add(doc)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    requireActivity().runOnUiThread {
                        adapter.setDocsList(docsList)
                        if (pdfList.isEmpty()) {
                            requireActivity().findViewById<ConstraintLayout>(R.id.cl_no_data).visibility =
                                View.VISIBLE
                        } else {
                            requireActivity().findViewById<ProgressBar>(R.id.cl_no_data).visibility =
                                View.GONE
                        }
                        requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility =
                            View.GONE
                    }
                }
            }
        }).start()
    }
}