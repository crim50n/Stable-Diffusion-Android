package dev.minios.pdaiv1.presentation.screen.gallery.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultUseCase
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.ByteArrayOutputStream

class ImageEditorViewModel(
    private val itemId: Long,
    dispatchersProvider: DispatchersProvider,
    private val getGenerationResultUseCase: GetGenerationResultUseCase,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
    private val mediaStoreGateway: MediaStoreGateway,
    private val schedulersProvider: SchedulersProvider,
    private val mainRouter: MainRouter,
) : MviRxViewModel<ImageEditorState, ImageEditorIntent, ImageEditorEffect>() {

    override val initialState = ImageEditorState()

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        loadImage()
    }

    private fun loadImage() {
        !getGenerationResultUseCase(itemId)
            .flatMap { item ->
                base64ToBitmapConverter(Base64ToBitmapConverter.Input(item.image))
                    .map { it.bitmap }
            }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { bitmap ->
                updateState {
                    it.copy(
                        originalBitmap = bitmap,
                        editedBitmap = bitmap,
                        isLoading = false
                    )
                }
            }
    }

    override fun processIntent(intent: ImageEditorIntent) {
        when (intent) {
            ImageEditorIntent.NavigateBack -> mainRouter.navigateBack()

            ImageEditorIntent.RotateLeft -> {
                updateState {
                    it.copy(rotation = (it.rotation - 90f) % 360f).withFiltersApplied()
                }
                applyTransformations()
            }

            ImageEditorIntent.RotateRight -> {
                updateState {
                    it.copy(rotation = (it.rotation + 90f) % 360f).withFiltersApplied()
                }
                applyTransformations()
            }

            ImageEditorIntent.FlipHorizontal -> {
                updateState {
                    it.copy(flipHorizontal = !it.flipHorizontal).withFiltersApplied()
                }
                applyTransformations()
            }

            ImageEditorIntent.FlipVertical -> {
                updateState {
                    it.copy(flipVertical = !it.flipVertical).withFiltersApplied()
                }
                applyTransformations()
            }

            is ImageEditorIntent.UpdateBrightness -> {
                updateState {
                    it.copy(brightness = intent.value).withFiltersApplied()
                }
                applyTransformations()
            }

            is ImageEditorIntent.UpdateContrast -> {
                updateState {
                    it.copy(contrast = intent.value).withFiltersApplied()
                }
                applyTransformations()
            }

            is ImageEditorIntent.UpdateSaturation -> {
                updateState {
                    it.copy(saturation = intent.value).withFiltersApplied()
                }
                applyTransformations()
            }

            ImageEditorIntent.ResetFilters -> updateState {
                it.copy(
                    rotation = 0f,
                    flipHorizontal = false,
                    flipVertical = false,
                    brightness = 0f,
                    contrast = 1f,
                    saturation = 1f,
                    editedBitmap = it.originalBitmap,
                    hasChanges = false
                )
            }

            ImageEditorIntent.Save -> saveImage()

            ImageEditorIntent.SaveAs -> saveImage()

            ImageEditorIntent.DismissDialog -> updateState {
                it.copy(screenModal = Modal.None)
            }

            is ImageEditorIntent.SelectTool -> updateState {
                it.copy(selectedTool = intent.tool)
            }
        }
    }

    private fun applyTransformations() {
        val state = currentState
        val original = state.originalBitmap ?: return

        !Single.fromCallable {
            applyAllEffects(
                bitmap = original,
                rotation = state.rotation,
                flipH = state.flipHorizontal,
                flipV = state.flipVertical,
                brightness = state.brightness,
                contrast = state.contrast,
                saturation = state.saturation
            )
        }
            .subscribeOn(schedulersProvider.io)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { result ->
                updateState { it.copy(editedBitmap = result) }
            }
    }

    private fun applyAllEffects(
        bitmap: Bitmap,
        rotation: Float,
        flipH: Boolean,
        flipV: Boolean,
        brightness: Float,
        contrast: Float,
        saturation: Float
    ): Bitmap {
        // Apply rotation and flip
        val matrix = Matrix().apply {
            postRotate(rotation)
            if (flipH) postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            if (flipV) postScale(1f, -1f, bitmap.width / 2f, bitmap.height / 2f)
        }

        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )

        // Apply color adjustments
        val colorMatrix = ColorMatrix()

        // Brightness: add to RGB values
        val brightnessMatrix = ColorMatrix(floatArrayOf(
            1f, 0f, 0f, 0f, brightness * 255,
            0f, 1f, 0f, 0f, brightness * 255,
            0f, 0f, 1f, 0f, brightness * 255,
            0f, 0f, 0f, 1f, 0f
        ))

        // Contrast: scale RGB values around middle
        val contrastMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, 128 * (1 - contrast),
            0f, contrast, 0f, 0f, 128 * (1 - contrast),
            0f, 0f, contrast, 0f, 128 * (1 - contrast),
            0f, 0f, 0f, 1f, 0f
        ))

        // Saturation
        val saturationMatrix = ColorMatrix()
        saturationMatrix.setSaturation(saturation)

        // Combine all matrices
        colorMatrix.postConcat(brightnessMatrix)
        colorMatrix.postConcat(contrastMatrix)
        colorMatrix.postConcat(saturationMatrix)

        val resultBitmap = Bitmap.createBitmap(
            rotatedBitmap.width, rotatedBitmap.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(resultBitmap)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }
        canvas.drawBitmap(rotatedBitmap, 0f, 0f, paint)

        if (rotatedBitmap != bitmap) {
            rotatedBitmap.recycle()
        }

        return resultBitmap
    }

    private fun saveImage() {
        val bitmap = currentState.editedBitmap ?: return
        updateState { it.copy(isSaving = true) }

        !Single.fromCallable {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
            .subscribeOn(schedulersProvider.io)
            .flatMapCompletable { bytes ->
                io.reactivex.rxjava3.core.Completable.fromAction {
                    mediaStoreGateway.exportToFile(
                        fileName = "pdai_edited_${System.currentTimeMillis()}",
                        content = bytes,
                    )
                }
            }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    errorLog(t)
                    updateState { it.copy(isSaving = false) }
                },
                onComplete = {
                    updateState { it.copy(isSaving = false, hasChanges = false) }
                    emitEffect(ImageEditorEffect.SavedSuccessfully)
                    mainRouter.navigateBack()
                }
            )
    }
}
