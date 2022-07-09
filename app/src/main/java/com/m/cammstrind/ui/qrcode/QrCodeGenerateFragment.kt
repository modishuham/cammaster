package com.m.cammstrind.ui.qrcode

import android.content.Context.WINDOW_SERVICE
import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.navigation.fragment.findNavController
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.base.BaseFragment
import com.m.cammstrind.databinding.FragmentGenerateQrCodeBinding
import java.io.File
import java.io.FileOutputStream


class QrCodeGenerateFragment : BaseFragment() {

    private var mBinding: FragmentGenerateQrCodeBinding? = null
    private var bitmap: Bitmap? = null
    private var qrgEncoder: QRGEncoder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (mBinding == null) {
            mBinding = FragmentGenerateQrCodeBinding.inflate(inflater, container, false)
        }
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppAnalytics.trackScreenLaunch("QRcode generate")
        mBinding?.btnGenerateQrCode?.setOnClickListener {
            if (TextUtils.isEmpty(mBinding?.etQrCode?.text.toString())) {
                Toast.makeText(
                    activity,
                    "Enter some text to generate QR Code",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                try {
                    val manager = context?.getSystemService(WINDOW_SERVICE) as WindowManager?
                    val display = manager!!.defaultDisplay
                    val point = Point()
                    display.getSize(point)
                    val width: Int = point.x
                    val height: Int = point.y
                    // generating dimension from width and height.
                    var dimen = if (width < height) width else height
                    dimen = dimen * 3 / 4
                    // setting this dimensions inside our qr code
                    // encoder to generate our qr code.
                    qrgEncoder =
                        QRGEncoder(
                            mBinding?.etQrCode?.text.toString(),
                            null,
                            QRGContents.Type.TEXT,
                            dimen
                        )
                    // getting our qrcode in the form of bitmap.
                    bitmap = qrgEncoder!!.encodeAsBitmap()
                    // the bitmap is set inside our image
                    bitmap?.let {
                        mBinding!!.ivQrCode.setImageBitmap(it)
                        mBinding!!.btnSaveQrCode.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                }
            }
        }

        mBinding?.btnSaveQrCode?.setOnClickListener {
            try {
                val outStream: FileOutputStream
                val mediaStorageDir: String =
                    "" + requireContext().getExternalFilesDir(null) + "/QRCodes"
                val mFolder = File(mediaStorageDir)
                if (!mFolder.exists()) {
                    mFolder.mkdir()
                }
                val outFile = File(mFolder, "QR_CODE_${mBinding!!.etQrCode.text.toString()}.png")
                outStream = FileOutputStream(outFile)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
                outStream.close()
                Toast.makeText(
                    requireContext(),
                    "QR Code Saved Successfully.",
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
    }


}