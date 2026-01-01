package com.shifthackz.aisdv1.domain.entity

import android.graphics.Bitmap

data class LocalDiffusionStatus(
    val current: Int,
    val total: Int,
    val previewBitmap: Bitmap? = null,
)
