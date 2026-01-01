package dev.minios.pdaiv1.presentation.modal.crop

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.mr0xf00.easycrop.CropError
import com.mr0xf00.easycrop.CropResult
import com.mr0xf00.easycrop.CropperStyle
import com.mr0xf00.easycrop.CropperStyleGuidelines
import com.mr0xf00.easycrop.RectCropShape
import com.mr0xf00.easycrop.crop
import com.mr0xf00.easycrop.rememberImageCropper
import com.mr0xf00.easycrop.ui.ImageCropperDialog
import dev.minios.pdaiv1.core.common.extensions.showToast

@Composable
fun CropImageModal(
    bitmap: Bitmap,
    onResult: (Bitmap) -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    val imageCropper = rememberImageCropper()
    val state = imageCropper.cropState
    state?.let {
        ImageCropperDialog(
            state = it,
            style = CropperStyle(
                backgroundColor = MaterialTheme.colorScheme.background,
                overlay = MaterialTheme.colorScheme.surface,
                guidelines = CropperStyleGuidelines(),
                shapes = listOf(RectCropShape),
            ),
        )
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        when (val result = imageCropper.crop(bmp = bitmap.asImageBitmap())) {
            is CropResult.Success -> result.bitmap.asAndroidBitmap().let(onResult::invoke)

            CropError.LoadingError -> {
                context.showToast("Loading error")
                onDismissRequest()
            }

            CropError.SavingError -> {
                context.showToast("Saving error")
                onDismissRequest()
            }

            CropResult.Cancelled -> onDismissRequest()
        }
    }
}
