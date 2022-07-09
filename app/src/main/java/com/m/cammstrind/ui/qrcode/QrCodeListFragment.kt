package com.m.cammstrind.ui.qrcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.base.BaseFragment
import com.m.cammstrind.response.DOC
import com.m.cammstrind.ui.qrcode.adapter.QrCodesAdapter
import com.m.cammstrind.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_pdf_list.*
import kotlinx.android.synthetic.main.fragment_qr_code_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class QrCodeListFragment : BaseFragment() {

    private var mView: View? = null
    private var qrList: ArrayList<DOC> = ArrayList()
    private var adapter = QrCodesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_qr_code_list, container, false)
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppAnalytics.trackScreenLaunch("QRcode List")
        if (qrList.isEmpty()) {
            requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
            rv_qr_list.adapter = adapter
            rv_qr_list.setHasFixedSize(true)
            adapter.setActivity(requireActivity(), this)
            getQRCodeList()
        }
    }

    private fun getQRCodeList() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/QRCodes"
            val mFolder = File(mediaStorageDir)
            if (mFolder.exists()) {
                val allFiles: Array<File>? = mFolder.listFiles { _, name ->
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
                            qrList.add(doc)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    adapter.setQrList(qrList)
                    if (qrList.isEmpty()) {
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

}