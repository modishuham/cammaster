package com.m.cammstrind.ui.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.base.BaseFragment
import com.m.cammstrind.response.DOC
import com.m.cammstrind.utils.AppUtils
import com.m.cammstrind.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.collections.ArrayList

class SearchFragment : BaseFragment() {

    private var mView: View? = null
    private var fileList = ArrayList<DOC>()
    private var adapter: SearchAdapter? = null
    private var searchResultHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_search, container, false)
            AppAnalytics.trackScreenLaunch("Search")
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adRequest = AdRequest.Builder().build()
        addView_search.loadAd(adRequest)
        if (fileList.isEmpty()) {
            adapter = SearchAdapter(requireContext())
            view.findViewById<RecyclerView>(R.id.rv_search_list).adapter = adapter
            view.findViewById<RecyclerView>(R.id.rv_search_list).setHasFixedSize(true)
            view.findViewById<EditText>(R.id.et_search)
                .addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        searchResultHandler.removeCallbacksAndMessages(null)
                        searchResultHandler.postDelayed({
                            if (text.toString().trim().isNotEmpty())
                                getSearchData(text.toString())
                        }, 1000)
                    }

                    override fun afterTextChanged(text: Editable?) {
                    }
                })
        }
    }

    private fun getSearchData(searchString: String) {
        fileList.clear()
        requireActivity().findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val mediaStorageDir: String =
                "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
            val mFolder = File(mediaStorageDir)
            if (mFolder.exists()) {
                val allFiles: Array<File>? = mFolder.listFiles { _, name ->
                    AppUtils.removeFileExtension(name).contains(searchString, true)
                }
                allFiles?.let {
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
                            fileList.add(doc)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    withContext(Dispatchers.Main) {
                        adapter?.setFiles(fileList)
                        if (fileList.isEmpty()) {
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

    override fun onDestroy() {
        super.onDestroy()
        searchResultHandler.removeCallbacksAndMessages(null)
    }
}