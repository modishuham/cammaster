package com.m.cammstrind.ui.pdfList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.m.cammstrind.R
import kotlinx.android.synthetic.main.fragment_doc_list.*
import java.io.File

class PdfListFragment : Fragment() {

    private var mView: View? = null
    private var pdfList: ArrayList<File> = ArrayList()
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

        if (pdfList.isEmpty()) {
            requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
            rv_doc_list.adapter = adapter
            adapter.setActivity(requireActivity())
            getScannedDocsList()
        }
    }

    private fun getScannedDocsList() {
        Thread(Runnable {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
            val mFolder = File(mediaStorageDir)
            if (mFolder.exists()) {
                val allFiles: Array<File>? = mFolder.listFiles { _, name ->
                    name.endsWith(".pdf")
                }
                allFiles?.let {
                    try {
                        pdfList.addAll(allFiles.asList())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                requireActivity().runOnUiThread {
                    adapter.setDocsList(pdfList)
                    if (pdfList.isEmpty()) {
                        requireActivity().findViewById<ConstraintLayout>(R.id.cl_no_data).visibility =
                            View.VISIBLE
                    } else {
                        requireActivity().findViewById<ConstraintLayout>(R.id.cl_no_data).visibility =
                            View.GONE
                    }
                    requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility =
                        View.GONE
                }
            }
        }).start()
    }
}