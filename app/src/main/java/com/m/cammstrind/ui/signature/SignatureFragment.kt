package com.m.cammstrind.ui.signature

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.gcacace.signaturepad.views.SignaturePad
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import kotlinx.android.synthetic.main.fragment_signature.*
import java.io.File
import java.io.FileOutputStream


class SignatureFragment : Fragment() {

    private var transparentBackground: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signature, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppAnalytics.trackScreenLaunch("Signature")
        val mSignaturePad = view.findViewById(R.id.signature_pad) as SignaturePad
        mSignaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                //Event triggered when the pad is touched
            }

            override fun onSigned() {
                //Event triggered when the pad is signed
            }

            override fun onClear() {
                //Event triggered when the pad is cleared
            }
        })

        btn_save_signature.setOnClickListener {
            try {
                val bitmap: Bitmap? = if (transparentBackground) {
                    mSignaturePad.transparentSignatureBitmap
                } else {
                    mSignaturePad.signatureBitmap
                }
                if (bitmap == null)
                    return@setOnClickListener
                val outStream: FileOutputStream
                val mediaStorageDir: String =
                    "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
                val mFolder = File(mediaStorageDir)
                if (!mFolder.exists()) {
                    mFolder.mkdir()
                }
                val outFile = File(mFolder, "Sign${System.currentTimeMillis()}.png")
                outStream = FileOutputStream(outFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
                outStream.close()
                Toast.makeText(
                    requireContext(),
                    "Signature Saved Successfully.",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            } catch (ex: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        }

        tv_clear.setOnClickListener {
            mSignaturePad.clear()
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val checkedRadioButton = group.findViewById<RadioButton>(checkedId)
            if (checkedRadioButton.text.equals("Blue")) {
                mSignaturePad.setPenColor(Color.BLUE)
            } else {
                mSignaturePad.setPenColor(Color.BLACK)
            }
        }

        cb_transparent_bg.setOnCheckedChangeListener { _, isChecked ->
            transparentBackground = isChecked
        }

        sb_pen_pointer.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                mSignaturePad.setMaxWidth((p1+3).toFloat())
                mSignaturePad.setMinWidth((p1+1).toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

    }

}