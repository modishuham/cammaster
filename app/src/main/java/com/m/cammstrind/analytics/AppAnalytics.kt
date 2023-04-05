package com.m.cammstrind.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.m.cammstrind.BuildConfig
import com.m.cammstrind.CamMasterApp

object AppAnalytics {

    private val analytics: FirebaseAnalytics =
        FirebaseAnalytics.getInstance(CamMasterApp.applicationContext())

    fun trackScreenLaunch(screenName: String) {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("screen_name", screenName)
            analytics.logEvent("SCREEN_LAUNCH", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun trackToggleNightMode(isOn: Boolean) {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("value", "" + isOn)
            analytics.logEvent("NIGHT_MODE", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun trackAppLock(isOn: Boolean) {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("value", "" + isOn)
            analytics.logEvent("APP_LOCK", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun trackDocOpen(docName: String) {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("doc_name", docName)
            analytics.logEvent("OPEN_DOC", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun trackPDFOpen(pdfName: String) {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("pdf_name", pdfName)
            analytics.logEvent("OPEN_PDF", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun trackCameraOpen() {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("camera_open", "true")
            analytics.logEvent("OPEN_CAMERA", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun trackFilesOpen() {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("files_open", "true")
            analytics.logEvent("OPEN_FILES", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun trackRateUsClick() {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("rate_us", "true")
            analytics.logEvent("RATE_US", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }

    fun trackShareClick() {
        if (BuildConfig.DEBUG)
            return
        try {
            val bundle = Bundle()
            bundle.putString("rate_us", "true")
            analytics.logEvent("RATE_US", bundle)
        } catch (ex: Exception) {
            ex.stackTrace
        }
    }
}