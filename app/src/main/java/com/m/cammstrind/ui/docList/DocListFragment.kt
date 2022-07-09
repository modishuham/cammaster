package com.m.cammstrind.ui.docList

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.response.DOC
import com.m.cammstrind.utils.BitmapUtils
import com.m.cammstrind.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_doc_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

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
            AppAnalytics.trackScreenLaunch("DocList")
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adRequest = AdRequest.Builder().build()
        addView_docList.loadAd(adRequest)

        if (docList.isEmpty()) {
            requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
            rv_doc_list.adapter = adapter
            rv_doc_list.setHasFixedSize(true)
            adapter.setActivity(requireActivity(), this)
            getScannedDocsList()
        }

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (adapter.isMultiSelectEnabled())
                    adapter.resetMultiSelect()
                else
                    findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        iv_multi_share_doc.setOnClickListener { shareMultipleDoc() }
    }

    private fun getScannedDocsList() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
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
                    Arrays.sort(it)
                    for (file in allFiles.asList()) {
                        try {
                            val doc = DOC(
                                file.name,
                                file.extension,
                                BitmapUtils.getThumbnail(file.path),
                                file.path,
                                file.length().toString(),
                                file.lastModified().toString()
                            )
                            docList.add(doc)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    withContext(Dispatchers.Main) {
                        adapter.setDocsList(docList)
                        rv_doc_list.scheduleLayoutAnimation()
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
            } else {
                withContext(Dispatchers.Main) {
                    requireActivity().findViewById<ConstraintLayout>(R.id.cl_no_data).visibility =
                        View.VISIBLE
                    requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility =
                        View.GONE
                }
            }
        }
    }

    fun shareDoc(doc: DOC) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "com.scanlibrary.provider1",
                File(doc.docPath)
            )
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, "Share"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareMultipleDoc() {
        try {
            val uriList: ArrayList<Uri> = ArrayList()
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)

            adapter.getMultiSelectedDOC().forEach {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.scanlibrary.provider1",
                    File(it.docPath)
                )
                uriList.add(uri)
            }
            intent.putExtra(Intent.EXTRA_STREAM, uriList)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, "Share"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DialogUtils.dismissDialog()
    }
}