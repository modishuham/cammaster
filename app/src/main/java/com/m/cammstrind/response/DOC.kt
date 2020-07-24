package com.m.cammstrind.response

import android.graphics.Bitmap

data class DOC(
    val docName: String,
    val docType: String,
    val docImage: Bitmap?,
    val docPath: String
)