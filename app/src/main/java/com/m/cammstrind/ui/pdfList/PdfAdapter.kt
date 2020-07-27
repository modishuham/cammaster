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

    inner class DocsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pdfDate = itemView.findViewById<TextView>(R.id.tv_pdf_date)
        private val pdfName = itemView.findViewById<TextView>(R.id.tv_pdf_name)
        private val btnShare = itemView.findViewById<ImageView>(R.id.btn_pdf_share)
        private val btnDelete = itemView.findViewById<ImageView>(R.id.btn_pdf_delete)
        fun bind(pdf: File) {
            pdfName.text = pdf.name
            pdfDate.text = getDateForDurationEvent(pdf.lastModified()).toString()
            itemView.setOnClickListener {
                PDFView.with(activity).setfilepath(pdf.path).start()
            }
            btnShare.setOnClickListener {
                fragment?.sharePdf(pdf)
            }
            btnDelete.setOnClickListener {
                AppUtils.deleteDoc(fragment?.requireContext()!!, pdf.name)
                pdfList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                notifyItemRangeChanged(adapterPosition, pdfList.size)
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