package com.m.cammstrind.ui.qrcode.adapter

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
import com.m.cammstrind.ui.qrcode.QrCodeListFragment
import com.m.cammstrind.utils.AppUtils
import com.m.cammstrind.utils.DialogUtils

class QrCodesAdapter : RecyclerView.Adapter<QrCodesAdapter.QrCodeViewHolder>() {

    private var qrList: ArrayList<DOC> = ArrayList()
    private var fragment: QrCodeListFragment? = null
    private var activity: Activity? = null

    inner class QrCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val qrName: TextView = itemView.findViewById(R.id.tv_qr_code_name)
        private val btnDeleteQR: ImageView = itemView.findViewById(R.id.btn_qr_delete)
        fun bind(doc: DOC) {
            qrName.text = doc.docName
            itemView.setOnClickListener {
                val bundle = bundleOf(
                    "docName" to doc.docName,
                    "docImage" to doc.docImage,
                    "docPath" to doc.docPath,
                    "docPosition" to adapterPosition,
                    "docSize" to doc.docSize,
                    "docTime" to doc.docTime
                )
                it.findNavController()
                    .navigate(R.id.action_qrCodeListFragment_to_docDetailFragment, bundle)
            }

            btnDeleteQR.setOnClickListener {
                DialogUtils.openAlertDialog(
                    fragment?.requireContext()!!,
                    "Are you sure want to delete ${doc.docName}?",
                    false
                ) {
                    AppUtils.deleteQrCode(fragment?.requireContext()!!, doc.docName)
                    qrList.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    notifyItemRangeChanged(adapterPosition, qrList.size)
                    DialogUtils.dismissDialog()
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QrCodeViewHolder {
        return QrCodeViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_qr_code, parent, false)
        )
    }

    override fun onBindViewHolder(holder: QrCodeViewHolder, position: Int) {
        holder.bind(qrList[position])
    }

    override fun getItemCount(): Int {
        return qrList.size
    }

    fun setQrList(qrList: ArrayList<DOC>) {
        this.qrList.clear()
        this.qrList = qrList
        notifyDataSetChanged()
    }

    fun setActivity(activity: Activity, fragment: QrCodeListFragment) {
        this.activity = activity
        this.fragment = fragment
    }
}