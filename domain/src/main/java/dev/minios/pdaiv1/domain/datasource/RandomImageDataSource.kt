package dev.minios.pdaiv1.domain.datasource

import android.graphics.Bitmap
import io.reactivex.rxjava3.core.Single

sealed interface RandomImageDataSource {

    interface Remote : RandomImageDataSource {
        fun fetch(): Single<Bitmap>
    }
}
