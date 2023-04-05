package com.m.cammstrind.ui.scanner

import android.app.Dialog
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfWriter
import com.m.cammstrind.R
import com.m.cammstrind.base.BaseFragment
import com.m.cammstrind.databinding.FragmentScannerBinding
import com.m.cammstrind.model.FilterItem
import com.m.cammstrind.ui.scanner.adapter.FilterAdapter
import com.m.cammstrind.utils.BitmapUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*

class ScannerFragment : BaseFragment(), FilterAdapter.FilterItemClickListener {

    private lateinit var mBinding: FragmentScannerBinding
    private var originalBitmap: Bitmap? = null
    private var transformedBitmap: Bitmap? = null
    private var rotationValue: Float = 0.0F

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentScannerBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getBitmap()
        originalBitmap?.let {
            mBinding.ivScannedImage.setImageBitmap(it)
            transformedBitmap = originalBitmap
            setUpFilters()
        }

        mBinding.rotate.setOnClickListener {
            try {
                if (rotationValue > 360) {
                    rotationValue = 0.0F
                }
                rotationValue += 90.0F
                transformedBitmap = getRotateBitmap(transformedBitmap!!, rotationValue)
                mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
            } catch (_: Exception) {
            }
        }

        mBinding.tvSave.setOnClickListener {
            val dialog = Dialog(requireActivity())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_save)
            val saveBtnImg: Button = dialog.findViewById<Button>(R.id.btn_save_img)
            val saveBtnPdf: Button = dialog.findViewById<Button>(R.id.btn_save_pdf)
            val docName: EditText = dialog.findViewById<EditText>(R.id.et_file_name)
            saveBtnImg.setOnClickListener { v1: View? ->
                if (docName.text.toString().trim { it <= ' ' }
                        .isEmpty()) {
                    docName.error = "Can't Empty"
                } else {
                    saveReceivedImage(
                        transformedBitmap,
                        docName.text.toString(),
                        "IMG"
                    )
                    dialog.dismiss()
                }
            }
            saveBtnPdf.setOnClickListener { v12: View? ->
                if (docName.text.toString().trim { it <= ' ' }
                        .isEmpty()) {
                    docName.error = "Can't Empty"
                } else {
                    saveReceivedImage(
                        transformedBitmap,
                        docName.text.toString(),
                        "PDF"
                    )
                    dialog.dismiss()
                }
            }
            dialog.show()

        }
    }

    private fun getBitmap() {
        arguments?.let {
            val uriString = it.getString("image", "")
            if (uriString.isNullOrEmpty())
                return
            val uri = Uri.parse(uriString)
            originalBitmap = BitmapUtils.getBitmap(requireContext(), uri)
            requireActivity().contentResolver.delete(uri, null, null)
        }
    }

    override fun onFilterItemClick(filterName: String) {
        when (filterName) {
            "Original" -> {
                transformedBitmap = originalBitmap
                mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
            }
            "Magic Color" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    transformedBitmap = EffectsUtils.applyMagicColor(originalBitmap!!)
                    withContext(Dispatchers.Main) {
                        mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
                    }
                }
            }
            "Magic Color 2" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    transformedBitmap = EffectsUtils.applyMagicColor2(originalBitmap!!)
                    withContext(Dispatchers.Main) {
                        mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
                    }
                }
            }
            "Perfect" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    transformedBitmap = EffectsUtils.applyPerfectEffect(originalBitmap!!)
                    withContext(Dispatchers.Main) {
                        mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
                    }
                }
            }
            "Perfect 2" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    transformedBitmap = EffectsUtils.applyPerfect2Effect(originalBitmap!!)
                    withContext(Dispatchers.Main) {
                        mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
                    }
                }
            }
            "Grey Scale" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    transformedBitmap = EffectsUtils.applyGreyEffect(originalBitmap!!)
                    withContext(Dispatchers.Main) {
                        mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
                    }
                }
            }
            "B&W" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    transformedBitmap = EffectsUtils.applyBWEffect(originalBitmap!!)
                    withContext(Dispatchers.Main) {
                        mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
                    }
                }
            }
            "B&W2" -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    transformedBitmap = EffectsUtils.applyBW2Effect(originalBitmap!!)
                    withContext(Dispatchers.Main) {
                        mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
                    }
                }
            }
        }
    }

    private fun setUpFilters() {
        val item1 = FilterItem("Original", originalBitmap!!)
        val item2 = FilterItem("Magic Color", originalBitmap!!)
        val item3 = FilterItem("Perfect", originalBitmap!!, true)
        val item4 = FilterItem("Grey Scale", originalBitmap!!)
        val item5 = FilterItem("B&W", originalBitmap!!)
        val item6 = FilterItem("Magic Color 2", originalBitmap!!)
        val item7 = FilterItem("Perfect 2", originalBitmap!!)
        val item8 = FilterItem("B&W2", originalBitmap!!)

        val filterList = ArrayList<FilterItem>()
        filterList.add(item1)
        filterList.add(item3)
        filterList.add(item7)
        filterList.add(item2)
        filterList.add(item6)
        filterList.add(item5)
        filterList.add(item8)
        filterList.add(item4)

        val adapter = FilterAdapter()
        adapter.setFilterItemClickListener(this)
        mBinding.rvFilters.adapter = adapter
        adapter.setFilters(filterList)

        lifecycleScope.launch(Dispatchers.IO) {
            transformedBitmap = EffectsUtils.applyPerfectEffect(originalBitmap!!)
            withContext(Dispatchers.Main) {
                mBinding.ivScannedImage.setImageBitmap(transformedBitmap)
            }
        }
    }

    fun resizeImage(image: Bitmap): Bitmap {

        val width = image.width
        val height = image.height

        val scaleWidth = width / 10
        val scaleHeight = height / 10

        if (image.byteCount <= 1000000)
            return image

        return Bitmap.createScaledBitmap(image, scaleWidth, scaleHeight, false)
    }

    private fun getRotateBitmap(
        bitmap: Bitmap,
        rotationValue: Float
    ): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(rotationValue)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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

}