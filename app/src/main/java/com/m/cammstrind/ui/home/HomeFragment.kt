package com.m.cammstrind.ui.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.m.cammstrind.BuildConfig
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.ui.camera.CameraXActivity
import com.m.cammstrind.ui.settings.SettingsActivity
import com.m.cammstrind.utils.BitmapUtils
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.*

class HomeFragment : Fragment() {

    private var mView: View? = null
    private var isMenuOpen = false
    private var cameraRequestCode = 100
    private var filesRequestCode = 101

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_home, container, false)
            AppAnalytics.trackScreenLaunch("Home")
        }
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adRequest = AdRequest.Builder().build()
        addView_home.loadAd(adRequest)
        btnCamera.setOnClickListener { openCamera() }
        btnFiles.setOnClickListener { openFiles() }
        btn_browse_images.setOnClickListener {
            val bundle = bundleOf("docType" to resources.getString(R.string.scanned_images))
            findNavController().navigate(R.id.action_homeFragment_to_docListFragment, bundle)
        }
        btn_browse_pdf.setOnClickListener {
            val bundle = bundleOf("docType" to resources.getString(R.string.scanned_pdf))
            findNavController().navigate(R.id.action_homeFragment_to_pdfListFragment, bundle)
        }
        btn_ocr.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_ocrFragment)
        }
        tv_menu_about.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_aboutFragment)
        }
        iv_search.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        btn_signature.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_signatureFragment)
        }
        btn_scan_qr.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_qrCodeMainFragment)
        }
        btn_pdf_viewer.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_pdfViewerFragment)
        }
        tv_menu_rate_us.setOnClickListener {
            AppAnalytics.trackRateUsClick()
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                    )
                )
            }
        }
        tv_settings.setOnClickListener {
            requireActivity().startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        tv_menu_share_app.setOnClickListener {
            AppAnalytics.trackShareClick()
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hey download and share CamMaster as free document scanner and OCR app. Check out CamMaster at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
            )
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
        }
        iv_menu.setOnClickListener {
            if (isMenuOpen) {
                cl_menu.visibility = View.GONE
                isMenuOpen = false
            } else {
                cl_menu.visibility = View.VISIBLE
                isMenuOpen = true
            }
        }

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isMenuOpen) {
                    cl_menu.visibility = View.GONE
                    isMenuOpen = false
                } else {
                    requireActivity().finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun openCamera() {
        AppAnalytics.trackCameraOpen()
        startActivityForResult(
            Intent(requireContext(), CameraXActivity::class.java),
            cameraRequestCode
        )
    }

    private fun openFiles() {
        AppAnalytics.trackFilesOpen()
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, filesRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == cameraRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null || data.extras == null) {
                    Toast.makeText(requireContext(), "Something went wrong.", Toast.LENGTH_SHORT)
                        .show()
                    return
                }
                val uriString = data.extras!!.getString("selectedCameraBitmap")
                Toast.makeText(requireContext(), "" + uriString, Toast.LENGTH_SHORT).show()
                val bundle = bundleOf("image" to uriString)
                findNavController().navigate(R.id.action_homeFragment_to_imageCropFragment, bundle)
            }
        }

        if (requestCode == filesRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    if (data == null || data.data == null) {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val bitmap = BitmapUtils.getBitmapFromURIWithRotation(
                        requireActivity(),
                        data!!.data!!
                    )
                    val uriString = BitmapUtils.getUri(requireContext(), bitmap!!)
                    val bundle = bundleOf("image" to uriString.toString())
                    findNavController().navigate(
                        R.id.action_homeFragment_to_imageCropFragment,
                        bundle
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}