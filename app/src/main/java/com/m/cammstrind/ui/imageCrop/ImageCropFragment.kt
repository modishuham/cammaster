package com.m.cammstrind.ui.imageCrop

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.m.cammstrind.openCv.OpenCvUtils
import com.m.cammstrind.R
import com.m.cammstrind.databinding.FragmentImageCropBinding
import com.m.cammstrind.utils.BitmapUtils

class ImageCropFragment : Fragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentImageCropBinding
    private var original: Bitmap? = null
    private var uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentImageCropBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val uriString = it.getString("image")
            uriString?.let { us ->
                uri = Uri.parse(us)
                init()
            }
        }
    }

    private fun init() {
        mBinding.scanButton.setOnClickListener(this)
        mBinding.sourceFrame.post {
            uri?.let {
                original = getBitmap(it)
                original?.let { bitmap ->
                    showImageWithCropPoints(bitmap)
                }

            }
        }
    }

    private fun getBitmap(uri: Uri): Bitmap? {
        var bitmap = BitmapUtils.getBitmap(requireContext(), uri)
        bitmap?.let {
            it.density = Bitmap.DENSITY_NONE
            if (it.width > it.height) {
                bitmap = OpenCvUtils.rotate(it, 90)
            }
        }
        return bitmap
    }

    private fun showImageWithCropPoints(bitmap: Bitmap) {
        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            bitmap.width,
            bitmap.height,
            false
        )
        mBinding.sourceImageView.setImageBitmap(scaledBitmap)
        //val pointFs = OpenCvUtils.getEdgePoints(scaledBitmap, polygonView!!)
        val pointFs = OpenCvUtils.getOutlinePointsWithoutEdgeDetection(scaledBitmap)
        mBinding.polygonView.points = pointFs
        mBinding.polygonView.visibility = View.VISIBLE
        val layoutParams =
            FrameLayout.LayoutParams(
                bitmap.width,
                bitmap.height
            )
        layoutParams.gravity = Gravity.CENTER
        mBinding.polygonView.layoutParams = layoutParams
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.scanButton) {
            mBinding.sourceFrame.post {
                val croppedBitmap = OpenCvUtils.cropReceiptByFourPoints(
                    original!!,
                    mBinding.polygonView.getListPoint(),
                    mBinding.sourceImageView.width,
                    mBinding.sourceImageView.height
                )
                val uri = BitmapUtils.getUri(requireContext(), croppedBitmap!!)
                val bundle = bundleOf("image" to uri.toString())
                findNavController().navigate(
                    R.id.action_imageCropFragment_to_scannerFragment,
                    bundle
                )
            }
        }
    }

}