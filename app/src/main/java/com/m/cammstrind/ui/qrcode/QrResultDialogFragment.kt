package com.m.cammstrind.ui.qrcode

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.m.cammstrind.R

class QrResultDialogFragment : DialogFragment() {

    var result = ""

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arguments?.let {
            result = it.getString("Qr", "")
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_qr_code_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tv_qr_result).text = result
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.let {
                it.setLayout(width, height)
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            }
        }
    }
}