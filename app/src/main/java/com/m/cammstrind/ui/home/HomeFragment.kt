package com.m.cammstrind.ui.home

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import com.m.cammstrind.BuildConfig
import com.m.cammstrind.R
import com.m.cammstrind.analytics.AppAnalytics
import com.m.cammstrind.ui.settings.SettingsActivity
import com.scanlibrary.ScanActivity
import com.scanlibrary.ScanConstants
import kotlinx.android.synthetic.main.fragment_home.*
import java.io.*

class HomeFragment : Fragment() {

    private val requestCode = 99
    private var mView: View? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var isMenuOpen = false

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
        InterstitialAd.load(
            requireContext(),
            "ca-app-pub-3940256099942544/1033173712",
            AdRequest.Builder().build(), interstitialAdLoadCallback
        )
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
        val preference = ScanConstants.OPEN_CAMERA
        val intent = Intent(requireContext(), ScanActivity::class.java)
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference)
        startActivityForResult(intent, requestCode)

    }

    private fun openFiles() {
        AppAnalytics.trackFilesOpen()
        val preference = ScanConstants.OPEN_MEDIA
        val intent = Intent(requireContext(), ScanActivity::class.java)
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference)
        startActivityForResult(intent, requestCode)
    }

    private fun saveReceivedImage(
        bitmap: Bitmap?,
        imageName: String,
        imageType: String
    ) {
        try {
            val number = (1..10).random()
            if (imageType == "IMG") {
                val outStream: FileOutputStream
                val mediaStorageDir: String =
                    "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
                val mFolder = File(mediaStorageDir)
                if (!mFolder.exists()) {
                    mFolder.mkdir()
                }
                var outFile = File(mFolder, "$imageName.png")
                if (outFile.exists()) {
                    outFile = File(mFolder, "$imageName($number).png")
                }
                outStream = FileOutputStream(outFile)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
                outStream.close()
            } else {
                val mediaStorageDir: String =
                    "" + requireContext().getExternalFilesDir(null) + "/CamMaster"
                val mFolder = File(mediaStorageDir)
                if (!mFolder.exists()) {
                    mFolder.mkdir()
                }
                var fileName = "$imageName.pdf"
                val outFile = File(mFolder, fileName)
                if (outFile.exists()) {
                    fileName = "$imageName($number).pdf"
                }
                val stream = ByteArrayOutputStream()
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val image = Image.getInstance(stream.toByteArray())

                val pageSize = Rectangle(image.width, image.height)
                val document = Document(pageSize)
                PdfWriter.getInstance(
                    document, FileOutputStream(
                        File(
                            mediaStorageDir,
                            fileName
                        )
                    )
                )

                image.alignment = Image.ALIGN_CENTER
                document.open()
                document.add(image)
                document.close()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private val interstitialAdLoadCallback = object : InterstitialAdLoadCallback() {
        override fun onAdLoaded(add: InterstitialAd) {
            super.onAdLoaded(add)
            mInterstitialAd = add
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == this.requestCode && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.extras!!.getParcelable(ScanConstants.SCANNED_RESULT)
            val imageType: String = data.extras!!.get(ScanConstants.SELECTED_BITMAP_TYPE) as String
            val imageName: String = data.extras!!.get(ScanConstants.SELECTED_BITMAP_NAME) as String
            val bitmap: Bitmap?
            try {
                bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(requireContext().contentResolver, uri!!)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
                }
                if (mInterstitialAd != null) {
                    mInterstitialAd!!.show(requireActivity())
                }
                Thread {
                    saveReceivedImage(bitmap, imageName.trim(), imageType)
                }.start()
                if (uri != null) {
                    requireContext().contentResolver.delete(uri, null, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}