package com.m.cammstrind.ui.pdfList

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.pdfviewer.PDFView
import com.m.cammstrind.R
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class PdfAdapter : RecyclerView.Adapter<PdfAdapter.DocsViewHolder>() {

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

    fun setActivity(activity: Activity) {
        this.activity = activity
    }

    inner class DocsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pdfDate = itemView.findViewById<TextView>(R.id.tv_pdf_date)
        private val pdfName = itemView.findViewById<TextView>(R.id.tv_pdf_name)
        fun bind(pdf: File) {
            pdfName.text = pdf.name
            pdfDate.text = Date(pdf.lastModified()).toString()
            itemView.setOnClickListener {
                PDFView.with(activity).setfilepath(pdf.path).start()
            }
        }
    }
}