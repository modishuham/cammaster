package com.m.cammstrind.ui.pdfList

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
import com.m.cammstrind.utils.AppUtils
import com.m.cammstrind.utils.DialogUtils
import java.io.File
import kotlin.collections.ArrayList

class PdfAdapter : RecyclerView.Adapter<PdfAdapter.DocsViewHolder>() {

    private var fragment: PdfListFragment? = null
    private var pdfList: ArrayList<File> = ArrayList()
    private var activity: Activity? = null

    private var isMultiSelect: Boolean = false
    private var selectedFilesList: ArrayList<File> = ArrayList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DocsViewHolder {
        return DocsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_pdf, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return pdfList.size
    }

    override fun onBindViewHolder(holder: DocsViewHolder, position: Int) {
        holder.bind(pdfList[position])
    }

    fun setDocsList(pdfList: ArrayList<File>) {
        this.pdfList.clear()
        this.pdfList = pdfList
        notifyDataSetChanged()
    }

    fun setActivity(activity: Activity, fragment: PdfListFragment) {
        this.activity = activity
        this.fragment = fragment
    }

    fun resetMultiSelect() {
        isMultiSelect = false
        activity?.findViewById<ImageView>(R.id.iv_multi_share_pdf)?.visibility = View.GONE
        selectedFilesList.clear()
        notifyDataSetChanged()
    }

    fun isMultiSelectEnabled(): Boolean {
        return isMultiSelect
    }

    fun getMultiSelectedPdf(): ArrayList<File> {
        return selectedFilesList
    }

    inner class DocsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pdfDate = itemView.findViewById<TextView>(R.id.tv_pdf_date)
        private val pdfName = itemView.findViewById<TextView>(R.id.tv_pdf_name)
        private val btnShare = itemView.findViewById<ImageView>(R.id.btn_pdf_share)
        private val btnDelete = itemView.findViewById<ImageView>(R.id.btn_pdf_delete)
        private val ivPdf = itemView.findViewById<ImageView>(R.id.iv_item_pdf)
        private val pdfSize = itemView.findViewById<TextView>(R.id.tv_pdf_size)

        fun bind(pdf: File) {
            pdfName.text = AppUtils.removeFileExtension(pdf.name)
            pdfDate.text = AppUtils.getDateForDurationEvent(pdf.lastModified()).toString()
            pdfSize.text = AppUtils.getFileSize(pdf.length())

            if (selectedFilesList.contains(pdf)) {
                ivPdf.setImageDrawable(activity?.resources?.getDrawable(R.drawable.ic_select, null))
                itemView.alpha = 0.7f
                btnDelete.alpha = 0.0f
                btnShare.alpha = 0.0f
            } else {
                ivPdf.setImageDrawable(activity?.resources?.getDrawable(R.drawable.ic_pdf, null))
                itemView.alpha = 1.0f
                btnDelete.alpha = 1.0f
                btnShare.alpha = 1.0f
            }

            itemView.setOnLongClickListener {
                if (!isMultiSelect) {
                    isMultiSelect = true
                    activity?.findViewById<ImageView>(R.id.iv_multi_share_pdf)?.visibility =
                        View.VISIBLE
                    itemView.alpha = 0.7f
                    ivPdf.setImageDrawable(
                        activity?.resources?.getDrawable(
                            R.drawable.ic_select,
                            null
                        )
                    )
                    btnDelete.alpha = 0.0f
                    btnShare.alpha = 0.0f
                    selectedFilesList.add(pdf)
                } else {
                    resetMultiSelect()
                }

                return@setOnLongClickListener true
            }

            itemView.setOnClickListener {
                if (!isMultiSelect) {
                    val bundle = bundleOf(
                        "pdfPath" to pdf.path
                    )
                    AppAnalytics.trackPDFOpen(pdf.name)
                    it.findNavController()
                        .navigate(R.id.action_pdfListFragment_to_pdfDetailFragment, bundle)
                } else {
                    if (!selectedFilesList.contains(pdf)) {
                        itemView.alpha = 0.7f
                        ivPdf.setImageDrawable(
                            activity?.resources?.getDrawable(
                                R.drawable.ic_select,
                                null
                            )
                        )
                        btnDelete.alpha = 0.0f
                        btnShare.alpha = 0.0f
                        selectedFilesList.add(pdf)
                    } else {
                        ivPdf.setImageDrawable(
                            activity?.resources?.getDrawable(
                                R.drawable.ic_pdf,
                                null
                            )
                        )
                        btnDelete.alpha = 1.0f
                        btnShare.alpha = 1.0f
                        itemView.alpha = 1.0f
                        selectedFilesList.remove(pdf)
                    }
                }
            }

            btnShare.setOnClickListener {
                if (!isMultiSelect)
                    fragment?.sharePdf(pdf)
            }

            btnDelete.setOnClickListener {
                if (!isMultiSelect) {
                    DialogUtils.openAlertDialog(
                        fragment?.requireContext()!!,
                        "Are you sure want to delete ${pdf.name}?",
                        false,
                        View.OnClickListener {
                            AppUtils.deleteDoc(fragment?.requireContext()!!, pdf.name)
                            pdfList.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            notifyItemRangeChanged(adapterPosition, pdfList.size)
                            DialogUtils.dismissDialog()
                        })
                }
            }
        }

    }
}