package dev.minios.pdaiv1.data.core

import android.graphics.Bitmap
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.MediaStoreInfo
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import java.io.ByteArrayOutputStream

internal abstract class CoreMediaStoreRepository(
    private val preferenceManager: PreferenceManager,
    private val mediaStoreGateway: MediaStoreGateway,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
) {

    protected fun exportToMediaStore(result: AiGenerationResult): Completable {
        if (!preferenceManager.saveToMediaStore) return Completable.complete()
        // Skip export if there's no image data (e.g., FalAi uses mediaPath directly)
        if (result.image.isEmpty()) return Completable.complete()
        return export(result)
    }

    protected fun getInfo(): Single<MediaStoreInfo> = Single.create { emitter ->
        emitter.onSuccess(mediaStoreGateway.getInfo())
    }

    private fun export(result: AiGenerationResult) = result.image
        .let(Base64ToBitmapConverter::Input)
        .let(base64ToBitmapConverter::invoke)
        .map(Base64ToBitmapConverter.Output::bitmap)
        .flatMapCompletable(::processBitmap)

    private fun processBitmap(bmp: Bitmap) = Completable
        .fromAction {
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            mediaStoreGateway.exportToFile(
                fileName = "pdai_${System.currentTimeMillis()}",
                content = stream.toByteArray(),
            )
        }
        .onErrorComplete { t ->
            errorLog(t)
            true
        }
}
