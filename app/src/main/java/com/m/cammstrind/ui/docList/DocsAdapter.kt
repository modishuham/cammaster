package com.m.cammstrind.ui.docList

import android.app.Activity
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
    private var fragment: DocListFragment? = null
    private var activity: Activity? = null

    private var isMultiSelect: Boolean = false
    private var selectedFilesList: ArrayList<DOC> = ArrayList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocsViewHolder {
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

    fun setActivity(activity: Activity, fragment: DocListFragment) {
        this.activity = activity
        this.fragment = fragment
    }

    fun resetMultiSelect() {
        isMultiSelect = false
        activity?.findViewById<ImageView>(R.id.iv_multi_share_doc)?.visibility = View.GONE
        selectedFilesList.clear()
        notifyDataSetChanged()
    }

    fun isMultiSelectEnabled(): Boolean {
        return isMultiSelect
    }

    fun getMultiSelectedDOC(): ArrayList<DOC> {
        return selectedFilesList
    }

    inner class DocsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val docImage = itemView.findViewById<ImageView>(R.id.iv_doc)
        private val docName = itemView.findViewById<TextView>(R.id.tv_doc_name)

        fun bind(doc: DOC) {
            docImage.setImageBitmap(doc.docImage)
            docName.text = doc.docName

            if (selectedFilesList.contains(doc)) {
                itemView.alpha = 0.3f
            } else {
                itemView.alpha = 1.0f
            }

            itemView.setOnLongClickListener {
                if (!isMultiSelect) {
                    isMultiSelect = true
                    activity?.findViewById<ImageView>(R.id.iv_multi_share_doc)?.visibility =
                        View.VISIBLE
                    itemView.alpha = 0.3f
                    selectedFilesList.add(doc)
                } else {
                    resetMultiSelect()
                }
                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                if (!isMultiSelect) {
                    val bundle = bundleOf(
                        "docName" to doc.docName,
                        "docImage" to doc.docImage,
                        "docPath" to doc.docPath,
                        "docPosition" to adapterPosition,
                        "docSize" to doc.docSize,
                        "docTime" to doc.docTime
                    )
                    it.findNavController()
                        .navigate(R.id.action_docListFragment_to_docDetailFragment, bundle)
                } else {
                    if (!selectedFilesList.contains(doc)) {
                        itemView.alpha = 0.3f
                        selectedFilesList.add(doc)
                    } else {
                        itemView.alpha = 1.0f
                        selectedFilesList.remove(doc)
                    }
                }
            }
        }

    }
}