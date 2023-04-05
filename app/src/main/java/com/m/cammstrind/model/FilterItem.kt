package com.m.cammstrind.model

import android.graphics.Bitmap

data class FilterItem(
    var filterName: String,
    var bitmap: Bitmap,
    var isSelected: Boolean = false
)