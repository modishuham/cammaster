package com.m.cammstrind.ui.docList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.m.cammstrind.R
import com.m.cammstrind.response.DOC

class DocsAdapter : RecyclerView.Adapter<DocsAdapter.DocsViewHolder>() {

    private var docsList: ArrayList<DOC> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocsViewHolder {
        return DocsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_doc, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return docsList.size
    }

    override fun onBindViewHolder(holder: DocsViewHolder, position: Int) {
        holder.bind(docsList[position])
    }

    fun setDocsList(docsList: ArrayList<DOC>) {
        this.docsList.clear()
        this.docsList = docsList
        notifyDataSetChanged()
    }

    inner class DocsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val docImage = itemView.findViewById<ImageView>(R.id.iv_doc)
        private val docTypeImage = itemView.findViewById<ImageView>(R.id.iv_doc_type)
        private val docName = itemView.findViewById<TextView>(R.id.tv_doc_name)

        fun bind(doc: DOC) {
            if (doc.docType == "pdf") {
                docImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_pdf, null))
            } else {
                docImage.setImageBitmap(doc.docImage)
            }
            docName.text = doc.docName
            docTypeImage.setImageDrawable(itemView.resources.getDrawable(R.drawable.ic_pdf, null))

            itemView.setOnClickListener {
                val bundle = bundleOf(
                    "docName" to doc.docName,
                    "docImage" to doc.docImage,
                    "docPath" to doc.docPath
                )
                it.findNavController()
                    .navigate(R.id.action_docListFragment_to_docDetailFragment, bundle)
            }
        }

    }
}