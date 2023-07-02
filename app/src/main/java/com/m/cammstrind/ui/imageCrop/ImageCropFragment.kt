package com.m.cammstrind.ui.imageCrop

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.m.cammstrind.R
import com.m.cammstrind.databinding.FragmentImageCropBinding
import com.m.cammstrind.openCv.OpenCvUtils
import com.m.cammstrind.utils.BitmapUtils
import com.scanlibrary.ScanActivity
import kotlinx.android.synthetic.main.fragment_image_crop.polygonView
import kotlinx.android.synthetic.main.fragment_image_crop.sourceImageView

class ImageCropFragment : Fragment(), View.OnClickListener {

    private lateinit var mBinding: FragmentImageCropBinding
    private var original: Bitmap? = null
    private var uri: Uri? = null
    private lateinit var mScanActivity: ScanActivity

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
        mScanActivity = ScanActivity()
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
                requireContext().contentResolver.delete(uri!!, null, null)
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
        val scaledBitmap = scaledBitmap(bitmap, mBinding.sourceFrame.width, mBinding.sourceFrame.height)
        mBinding.sourceImageView.setImageBitmap(scaledBitmap)
        val tempBitmap = (sourceImageView.drawable as BitmapDrawable).bitmap
        val pointFs = getEdgePoints(tempBitmap)
        mBinding.polygonView.points = pointFs
        mBinding.polygonView.visibility = View.VISIBLE
        val padding = resources.getDimension(com.scanlibrary.R.dimen.scanPadding).toInt()
        val layoutParams = FrameLayout.LayoutParams(
            tempBitmap.width + 2 * padding,
            tempBitmap.height + 2 * padding
        )
        layoutParams.gravity = Gravity.CENTER
        mBinding.polygonView.layoutParams = layoutParams
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.scanButton) {
            mBinding.sourceFrame.post {
                val points: Map<Int, PointF> = mBinding.polygonView.points
                if (isScanPointsValid(points)) {
                    val croppedBitmap = getScannedBitmap(original!!, points)
                    val uri = BitmapUtils.getUri(requireContext(), croppedBitmap!!)
                    val bundle = bundleOf("image" to uri.toString())
                    findNavController().navigate(
                        R.id.action_imageCropFragment_to_scannerFragment,
                        bundle
                    )
                } else {
                    Toast.makeText(requireContext(), "Not Valid", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap? {
        val m = Matrix()
        m.setRectToRect(
            RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()),
            RectF(0f, 0f, width.toFloat(), height.toFloat()),
            Matrix.ScaleToFit.CENTER
        )
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun getScannedBitmap(original: Bitmap, points: Map<Int, PointF>): Bitmap? {
        //int width = original.getWidth();
        //int height = original.getHeight();
        val xRatio = original.width.toFloat() / sourceImageView.width
        val yRatio = original.height.toFloat() / sourceImageView.height
        val x1 = points[0]!!.x * xRatio
        val x2 = points[1]!!.x * xRatio
        val x3 = points[2]!!.x * xRatio
        val x4 = points[3]!!.x * xRatio
        val y1 = points[0]!!.y * yRatio
        val y2 = points[1]!!.y * yRatio
        val y3 = points[2]!!.y * yRatio
        val y4 = points[3]!!.y * yRatio
        Log.d("", "POints($x1,$y1)($x2,$y2)($x3,$y3)($x4,$y4)")
        return mScanActivity.getScannedBitmap(
            original,
            x1,
            y1,
            x2,
            y2,
            x3,
            y3,
            x4,
            y4
        )
    }

    private fun getEdgePoints(tempBitmap: Bitmap): Map<Int?, PointF?>? {
        val pointFs = getContourEdgePoints(tempBitmap)
        return orderedValidEdgePoints(tempBitmap, pointFs)
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap): List<PointF> {
        val points = mScanActivity.getPoints(tempBitmap)
        val x1 = points[0]
        val x2 = points[1]
        val x3 = points[2]
        val x4 = points[3]
        val y1 = points[4]
        val y2 = points[5]
        val y3 = points[6]
        val y4 = points[7]
        val pointFs: MutableList<PointF> = ArrayList()
        pointFs.add(PointF(x1, y1))
        pointFs.add(PointF(x2, y2))
        pointFs.add(PointF(x3, y3))
        pointFs.add(PointF(x4, y4))
        return pointFs
    }

    private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int?, PointF?> {
        val outlinePoints: MutableMap<Int?, PointF?> = HashMap()
        outlinePoints[0] = PointF(0f, 0f)
        outlinePoints[1] = PointF(tempBitmap.width.toFloat(), 0f)
        outlinePoints[2] = PointF(0f, tempBitmap.height.toFloat())
        outlinePoints[3] = PointF(tempBitmap.width.toFloat(), tempBitmap.height.toFloat())
        return outlinePoints
    }

    private fun orderedValidEdgePoints(
        tempBitmap: Bitmap,
        pointFs: List<PointF>
    ): Map<Int?, PointF?>? {
        var orderedPoints: Map<Int?, PointF?>? = polygonView.getOrderedPoints(pointFs)
        if (!polygonView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }
        return orderedPoints
    }

    private fun isScanPointsValid(points: Map<Int, PointF>): Boolean {
        return points.size == 4
    }

}