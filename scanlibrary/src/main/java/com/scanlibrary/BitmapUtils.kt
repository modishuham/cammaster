package com.scanlibrary

import android.content.ContentValues
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

object BitmapUtils {

    var currentPickedBitmap: Bitmap? = null

    @Throws(IOException::class)
    fun getBitmap(context: Context, selectedImg: Uri): Bitmap? {
        var inSampleSize = 1
        val bitmap: Bitmap
        try {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    selectedImg
                )
            } else {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        context.contentResolver,
                        selectedImg
                    )
                )
            }
            when {
                bitmap.byteCount > 50000000 -> {
                    inSampleSize = 4
                }
                bitmap.byteCount > 30000000 -> {
                    inSampleSize = 3
                }
                bitmap.byteCount > 20000000 -> {
                    inSampleSize = 2
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize
        val fileDescriptor: AssetFileDescriptor =
            context.contentResolver.openAssetFileDescriptor(selectedImg, "r")!!
        val original = BitmapFactory.decodeFileDescriptor(
            fileDescriptor.fileDescriptor, null, options
        )
        return getUnRotatedBitmap(fileDescriptor, original)
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    private fun getUnRotatedBitmap(
        fileDescriptor: AssetFileDescriptor?,
        original: Bitmap
    ): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val ei = ExifInterface(
                    fileDescriptor!!.fileDescriptor
                )
                val orientation = ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
                )
                val rotatedBitmap: Bitmap = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(original, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(original, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(original, 270f)
                    ExifInterface.ORIENTATION_NORMAL -> original
                    else -> original
                }
                rotatedBitmap
            } else {
                original
            }
        } catch (e: Exception) {
            e.printStackTrace()
            original
        }
    }

    /*
    * This method used for latest android >= 10
    * */
    fun getUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val relativeLocation = Environment.DIRECTORY_PICTURES
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        try {
            uri?.let { uri ->
                val stream = resolver.openOutputStream(uri)

                stream?.let { stream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)) {
                        throw IOException("Failed to save bitmap.")
                    }
                } ?: throw IOException("Failed to get output stream.")

            } ?: throw IOException("Failed to create new MediaStore record")

        } catch (e: IOException) {
            if (uri != null) {
                resolver.delete(uri, null, null)
            }
            throw IOException(e)
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
        }
        return uri!!
    }


}