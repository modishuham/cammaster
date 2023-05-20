package com.m.cammstrind.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

object BitmapUtils {

    fun getThumbnail(path: String): Bitmap? {
        return try {
            ThumbnailUtils.extractThumbnail(
                BitmapFactory.decodeFile(path),
                100,
                100
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }

    fun getUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val bytes = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bytes)
            val path =
                MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
            if (path == null || path.isEmpty()) {
                getUriFromBitmap(context, bitmap)
            } else Uri.parse(path)
        } catch (ex: java.lang.Exception) {
            getUriFromBitmap(context, bitmap)
        }
    }

    fun getBitmap(context: Context, uri: Uri?): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (ex: java.lang.Exception) {
            null
        }
    }

    private fun getUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
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


    @Throws(IOException::class)
    fun getBitmapFromURIWithRotation(activity: Activity, selectedImg: Uri): Bitmap? {
        var inSampleSize = 1
        val bitmap: Bitmap
        try {
            val inputStream: InputStream? = activity.contentResolver.openInputStream(selectedImg)
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap.byteCount > 50000000) {
                inSampleSize = 4
            } else if (bitmap.byteCount > 30000000) {
                inSampleSize = 3
            } else if (bitmap.byteCount > 20000000) {
                inSampleSize = 2
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val options = BitmapFactory.Options()
        options.inSampleSize = inSampleSize
        val fileDescriptor: AssetFileDescriptor =
            activity.contentResolver.openAssetFileDescriptor(selectedImg, "r")!!
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
        } catch (e: Exception) {
            e.printStackTrace()
            original
        }
    }

}