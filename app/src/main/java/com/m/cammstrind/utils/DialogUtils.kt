package com.m.cammstrind.utils

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.TextView
import com.m.cammstrind.R

object DialogUtils {

    private var dialog: Dialog? = null

    fun openAlertDialog(
        context: Context,
        message: String,
        singleButton: Boolean,
        clickListener: View.OnClickListener
    ) {
        dialog = Dialog(context)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(false)
        dialog!!.setContentView(R.layout.dialog_alert)
        dialog!!.findViewById<TextView>(R.id.dialog_description).text = message
        if (singleButton)
            dialog!!.findViewById<TextView>(R.id.dialog_cancel).visibility = View.GONE
        dialog!!.findViewById<TextView>(R.id.dialog_cancel).setOnClickListener {
            dialog!!.dismiss()
        }
        dialog!!.findViewById<TextView>(R.id.dialog_okay).setOnClickListener(clickListener)
        dialog!!.show()
    }

    fun openConvertToPdfDialog(
        context: Context,
        clickListener: View.OnClickListener
    ) {
        dialog = Dialog(context)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(R.layout.dialog_convert_to_pdf)
        dialog!!.findViewById<TextView>(R.id.btn_save_pdf).setOnClickListener(clickListener)
        dialog!!.show()
    }

    fun getPdfName(): String {
        return dialog!!.findViewById<TextView>(R.id.et_file_name).text.toString()
    }

    fun dismissDialog() {
        dialog?.dismiss()
    }

}