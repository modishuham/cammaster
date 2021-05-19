package com.m.cammstrind.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.response.DOC
import com.m.cammstrind.utils.AppUtils

class SearchAdapter(val context: Context) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private var filesList: ArrayList<DOC> = ArrayList()

    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.tv_file_name)
        private val fileImage: ImageView = itemView.findViewById(R.id.iv_item_search)

        fun bind(doc: DOC) {
            fileName.text = AppUtils.removeFileExtension(doc.docName)

            if (doc.docType == "pdf") {
                fileImage.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pdf))
            } else {
                fileImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_original
                    )
                )
            }

            itemView.setOnClickListener {
                if (doc.docType == "pdf") {
                    val bundle = bundleOf(
                        "pdfPath" to doc.docPath
                    )
                    AppAnalytics.trackPDFOpen(doc.docName)
                    it.findNavController()
                        .navigate(R.id.action_searchFragment_to_pdfDetailFragment, bundle)
                } else {
                    val bundle = bundleOf(
                        "docName" to doc.docName,
                        "docImage" to doc.docImage,
                        "docPath" to doc.docPath,
                        "docPosition" to adapterPosition,
                        "docSize" to doc.docSize,
                        "docTime" to doc.docTime
                    )
                    AppAnalytics.trackDocOpen(doc.docName)
                    it.findNavController()
                        .navigate(R.id.action_searchFragment_to_docDetailFragment, bundle)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(filesList[position])
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    fun setFiles(files: ArrayList<DOC>) {
        filesList.clear()
        filesList.addAll(files)
        notifyDataSetChanged()
    }
}