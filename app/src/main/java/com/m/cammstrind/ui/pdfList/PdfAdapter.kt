package com.m.cammstrind.ui.pdfList

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.pdfviewer.PDFView
import com.m.cammstrind.R
import com.m.cammstrind.utils.AppUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
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

        fun bind(pdf: File) {
            pdfName.text = pdf.name
            pdfDate.text = getDateForDurationEvent(pdf.lastModified()).toString()

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
                    PDFView.with(activity).setfilepath(pdf.path).start()
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
                    AppUtils.deleteDoc(fragment?.requireContext()!!, pdf.name)
                    pdfList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    notifyItemRangeChanged(adapterPosition, pdfList.size)
                }
            }
        }

        private fun getDateForDurationEvent(timeStamp: Long): String? {
            return try {
                val sdf =
                    SimpleDateFormat("dd/MM/yyyy hh.mm aa")
                val netDate = Date(timeStamp)
                sdf.format(netDate)
            } catch (ex: Exception) {
                ""
            }
        }

    }
}