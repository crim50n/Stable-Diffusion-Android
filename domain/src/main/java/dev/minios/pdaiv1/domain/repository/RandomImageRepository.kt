package dev.minios.pdaiv1.domain.repository

import android.graphics.Bitmap
import io.reactivex.rxjava3.core.Single

interface RandomImageRepository {
    fun fetchAndGet(): Single<Bitmap>
}
