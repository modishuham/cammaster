package com.m.cammstrind.ui.pdfList

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
import com.m.cammstrind.utils.DialogUtils
import kotlinx.android.synthetic.main.fragment_pdf_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

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
            AppAnalytics.trackScreenLaunch("PdfList")
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adRequest = AdRequest.Builder().build()
        addView_pdfList.loadAd(adRequest)

        if (pdfList.isEmpty()) {
            requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
            rv_pdf_list.adapter = adapter
            rv_pdf_list.setHasFixedSize(true)
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

        iv_multi_share_pdf.setOnClickListener { shareMultiplePdf() }
    }

    private fun getScannedDocsList() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
            val mFolder = File(mediaStorageDir)
            if (mFolder.exists()) {
                val allFiles: Array<File>? = mFolder.listFiles { _, name ->
                    name.endsWith(".pdf")
                }
                allFiles?.let {
                    Arrays.sort(it)
                    try {
                        pdfList.addAll(allFiles.asList())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                withContext(Dispatchers.Main) {
                    adapter.setDocsList(pdfList)
                    rv_pdf_list.scheduleLayoutAnimation()
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

    fun sharePdf(pdf: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "com.scanlibrary.provider1",
                File(pdf.path)
            )
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, "Share"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareMultiplePdf() {
        try {
            val uriList: ArrayList<Uri> = ArrayList()
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE)

            adapter.getMultiSelectedPdf().forEach {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.scanlibrary.provider1",
                    File(it.path)
                )
                uriList.add(uri)
            }
            intent.type = "application/pdf"
            intent.putExtra(Intent.EXTRA_STREAM, uriList)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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