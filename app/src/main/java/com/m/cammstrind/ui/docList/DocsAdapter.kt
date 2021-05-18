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
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.response.DOC
import com.m.cammstrind.utils.AppUtils
import com.m.cammstrind.utils.DialogUtils

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
        private val docDate = itemView.findViewById<TextView>(R.id.tv_doc_date)
        private val docSize = itemView.findViewById<TextView>(R.id.tv_doc_size)
        private val btnDeleteDoc = itemView.findViewById<ImageView>(R.id.btn_doc_delete)
        private val btnShareDoc = itemView.findViewById<ImageView>(R.id.btn_doc_share)

        fun bind(doc: DOC) {
            docImage.setImageBitmap(doc.docImage)
            docName.text = AppUtils.removeFileExtension(doc.docName)
            docSize.text = AppUtils.getFileSize(doc.docSize.toLong())
            docDate.text = AppUtils.getDateForDurationEvent(doc.docTime.toLong())

            if (selectedFilesList.contains(doc)) {
                itemView.alpha = 0.3f
                btnShareDoc.alpha = 0f
                btnDeleteDoc.alpha = 0f
            } else {
                itemView.alpha = 1.0f
                btnShareDoc.alpha = 1.0f
                btnDeleteDoc.alpha = 1.0f
            }

            itemView.setOnLongClickListener {
                if (!isMultiSelect) {
                    isMultiSelect = true
                    activity?.findViewById<ImageView>(R.id.iv_multi_share_doc)?.visibility =
                        View.VISIBLE
                    itemView.alpha = 0.3f
                    selectedFilesList.add(doc)
                    btnShareDoc.alpha = 0f
                    btnDeleteDoc.alpha = 0f
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
                    AppAnalytics.trackDocOpen(doc.docName)
                    it.findNavController()
                        .navigate(R.id.action_docListFragment_to_docDetailFragment, bundle)
                } else {
                    if (!selectedFilesList.contains(doc)) {
                        itemView.alpha = 0.3f
                        btnShareDoc.alpha = 0f
                        btnDeleteDoc.alpha = 0f
                        selectedFilesList.add(doc)
                    } else {
                        itemView.alpha = 1.0f
                        btnShareDoc.alpha = 1.0f
                        btnDeleteDoc.alpha = 1.0f
                        selectedFilesList.remove(doc)
                    }
                }
            }

            btnDeleteDoc.setOnClickListener {
                if (!isMultiSelect) {
                    DialogUtils.openAlertDialog(
                        fragment?.requireContext()!!,
                        "Are you sure want to delete ${doc.docName}?",
                        false
                    ) {
                        AppUtils.deleteDoc(fragment?.requireContext()!!, doc.docName)
                        docsList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        notifyItemRangeChanged(adapterPosition, docsList.size)
                        DialogUtils.dismissDialog()
                    }
                }
            }

            btnShareDoc.setOnClickListener {
                if (!isMultiSelect) {
                    fragment?.shareDoc(doc)
                }
            }
        }

    }
}