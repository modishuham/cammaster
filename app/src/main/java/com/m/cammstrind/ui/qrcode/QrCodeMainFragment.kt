package com.m.cammstrind.ui.qrcode

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.zxing.integration.android.IntentIntegrator
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.base.BaseFragment
import com.m.cammstrind.databinding.FragmentQrCodeMainBinding
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_qr_code_main.*

class QrCodeMainFragment : BaseFragment() {

    private var mBinding: FragmentQrCodeMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (mBinding == null) {
            mBinding = FragmentQrCodeMainBinding.inflate(inflater, container, false)
        }
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adRequest = AdRequest.Builder().build()
        addView_qrcode.loadAd(adRequest)
        AppAnalytics.trackScreenLaunch("QRcode Main")
        mBinding?.clScanCode?.setOnClickListener {
            val intentIntegrator = IntentIntegrator.forSupportFragment(this)
            intentIntegrator.setPrompt("Scan a barcode or QR Code")
            intentIntegrator.setOrientationLocked(false)
            intentIntegrator.initiateScan()
        }
        mBinding?.clGenerateCode?.setOnClickListener {
            findNavController().navigate(R.id.action_qrCodeMainFragment_to_qrCodeGenerateFragment)
        }
        mBinding?.clMyQrCodes?.setOnClickListener {
            findNavController().navigate(R.id.action_qrCodeMainFragment_to_qrCodeListFragment)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (intentResult != null) {
            if (intentResult.contents == null) {
                Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                if (!intentResult.contents.isNullOrBlank()) {
                    val fragment = QrResultDialogFragment()
                    val bundle = Bundle()
                    bundle.putString("Qr", intentResult.contents.toString())
                    fragment.arguments = bundle
                    fragment.show(childFragmentManager, "")
                } else {
                    Toast.makeText(requireContext(), "No result found.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT).show()
        }
    }
}