package com.m.cammstrind.ui.scanner

import android.graphics.Bitmap
import com.m.cammstrind.openCv.Scan
import org.opencv.android.Utils
import org.opencv.core.Mat

object EffectsUtils {

    suspend fun applyMagicColor(originalBitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(originalBitmap, mat)
        val scanner = Scan(mat, 51, 66, 160)
        val scannedImg = scanner.scanImage(Scan.ScanMode.RMODE)
        val final =
            Bitmap.createBitmap(scannedImg.cols(), scannedImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(scannedImg, final)
        return final
    }

    suspend fun applyMagicColor2(originalBitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(originalBitmap, mat)
        val scanner = Scan(mat, 70, 40, 220)
        val scannedImg = scanner.scanImage(Scan.ScanMode.RMODE)
        val final =
            Bitmap.createBitmap(scannedImg.cols(), scannedImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(scannedImg, final)
        return final
    }

    suspend fun applyGreyEffect(originalBitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(originalBitmap, mat)
        val scanner = Scan(mat, 51, 66, 160)
        val scannedImg = scanner.scanImage(Scan.ScanMode.SMODE)
        val final =
            Bitmap.createBitmap(scannedImg.cols(), scannedImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(scannedImg, final)
        return final
    }

    suspend fun applyPerfectEffect(originalBitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(originalBitmap, mat)
        val scanner = Scan(mat, 51, 66, 160)
        val scannedImg = scanner.scanImage(Scan.ScanMode.GCMODE)
        val final =
            Bitmap.createBitmap(scannedImg.cols(), scannedImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(scannedImg, final)
        return final
    }

    suspend fun applyPerfect2Effect(originalBitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(originalBitmap, mat)
        val scanner = Scan(mat, 100, 0, 0)
        val scannedImg = scanner.scanImage(Scan.ScanMode.GCMODE)
        val final =
            Bitmap.createBitmap(scannedImg.cols(), scannedImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(scannedImg, final)
        return final
    }

    suspend fun applyBWEffect(originalBitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(applyPerfectEffect(originalBitmap), mat)
        val scanner = Scan(mat, 51, 66, 160)
        val scannedImg = scanner.scanImage(Scan.ScanMode.SMODE)
        val final =
            Bitmap.createBitmap(scannedImg.cols(), scannedImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(scannedImg, final)
        return final
    }

    suspend fun applyBW2Effect(originalBitmap: Bitmap): Bitmap {
        val mat = Mat()
        Utils.bitmapToMat(applyPerfect2Effect(originalBitmap), mat)
        val scanner = Scan(mat, 51, 66, 250)
        val scannedImg = scanner.scanImage(Scan.ScanMode.SMODE)
        val final =
            Bitmap.createBitmap(scannedImg.cols(), scannedImg.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(scannedImg, final)
        return final
    }
}