package dev.minios.pdaiv1.presentation.screen.gallery.detail

import android.graphics.Bitmap
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.presentation.utils.FileSavableExporter
import io.reactivex.rxjava3.core.Single
import java.io.File

class GalleryDetailBitmapExporter(
    override val fileProviderDescriptor: FileProviderDescriptor,
) : FileSavableExporter.BmpToFile {

    operator fun invoke(bitmap: Bitmap): Single<File> = saveBitmapToFile(
        System.currentTimeMillis().toString(),
        bitmap
    )
}
