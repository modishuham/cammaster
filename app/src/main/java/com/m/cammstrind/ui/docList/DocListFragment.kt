package com.m.cammstrind.ui.docList

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

class DocListFragment : Fragment() {

    private var mView: View? = null
    private var docList: ArrayList<DOC> = ArrayList()
    private var adapter = DocsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_doc_list, container, false)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (docList.isEmpty()) {
            requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
            rv_doc_list.adapter = adapter
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
                    name.endsWith(".jpg") ||
                            name.endsWith(".jpeg") ||
                            name.endsWith(".png")
                }
                allFiles?.let {
                    for (file in allFiles.asList()) {
                        try {
                            val doc = DOC(
                                file.name,
                                file.extension,
                                BitmapUtils.getThumbnail(file.path),
                                file.path
                            )
                            docList.add(doc)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    requireActivity().runOnUiThread {
                        adapter.setDocsList(docList)
                        if (docList.isEmpty()) {
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
            }
        }).start()
    }
}