package dev.minios.pdaiv1.presentation.screen.gallery.detail

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.presentation.extensions.mapToUi
import dev.minios.pdaiv1.presentation.model.Modal
import com.shifthackz.android.core.mvi.MviState
import dev.minios.pdaiv1.core.localization.R as LocalizationR
import dev.minios.pdaiv1.presentation.R as PresentationR

sealed interface GalleryDetailState : MviState {
    val tabs: List<Tab>
    val selectedTab: Tab
    val screenModal: Modal
    val galleryIds: List<Long>
    val currentIndex: Int
    val bitmapCache: Map<Long, Bitmap>
    val controlsVisible: Boolean
    val currentSource: ServerSource
    val animateToPage: Int?
    val showInfoBottomSheet: Boolean

    @Immutable
    data class Loading(
        override val tabs: List<Tab> = emptyList(),
        override val selectedTab: Tab = Tab.IMAGE,
        override val screenModal: Modal = Modal.None,
        override val galleryIds: List<Long> = emptyList(),
        override val currentIndex: Int = 0,
        override val bitmapCache: Map<Long, Bitmap> = emptyMap(),
        override val controlsVisible: Boolean = true,
        override val currentSource: ServerSource = ServerSource.AUTOMATIC1111,
        override val animateToPage: Int? = null,
        override val showInfoBottomSheet: Boolean = false,
    ) : GalleryDetailState

    @Immutable
    data class Content(
        override val tabs: List<Tab> = emptyList(),
        override val selectedTab: Tab = Tab.IMAGE,
        override val screenModal: Modal = Modal.None,
        override val galleryIds: List<Long> = emptyList(),
        override val currentIndex: Int = 0,
        override val bitmapCache: Map<Long, Bitmap> = emptyMap(),
        override val controlsVisible: Boolean = true,
        override val currentSource: ServerSource = ServerSource.AUTOMATIC1111,
        override val animateToPage: Int? = null,
        override val showInfoBottomSheet: Boolean = false,
        val showReportButton: Boolean = false,
        val generationType: AiGenerationResult.Type,
        val id: Long,
        val bitmap: Bitmap,
        val inputBitmap: Bitmap?,
        val createdAt: UiText,
        val type: UiText,
        val prompt: UiText,
        val negativePrompt: UiText,
        val size: UiText,
        val samplingSteps: UiText,
        val cfgScale: UiText,
        val restoreFaces: UiText,
        val sampler: UiText,
        val seed: UiText,
        val subSeed: UiText,
        val subSeedStrength: UiText,
        val denoisingStrength: UiText,
        val hidden: Boolean,
        val liked: Boolean = false,
        val isFalAi: Boolean = false,
        val falAiEndpointId: String = "",
        val modelName: UiText = "".asUiText(),
    ) : GalleryDetailState

    fun withTab(tab: Tab): GalleryDetailState = when (this) {
        is Content -> copy(selectedTab = tab)
        is Loading -> copy(selectedTab = tab)
    }

    fun withDialog(dialog: Modal) = when (this) {
        is Content -> copy(screenModal = dialog)
        is Loading -> copy(screenModal = dialog)
    }

    fun withHiddenState(value: Boolean) = when (this) {
        is Content -> copy(hidden = value)
        is Loading -> this
    }

    fun withLikedState(value: Boolean) = when (this) {
        is Content -> copy(liked = value)
        is Loading -> this
    }

    fun withControlsVisible(value: Boolean) = when (this) {
        is Content -> copy(controlsVisible = value)
        is Loading -> copy(controlsVisible = value)
    }

    fun withInfoBottomSheet(value: Boolean) = when (this) {
        is Content -> copy(showInfoBottomSheet = value)
        is Loading -> copy(showInfoBottomSheet = value)
    }

    fun withGalleryIds(ids: List<Long>, index: Int) = when (this) {
        is Content -> copy(galleryIds = ids, currentIndex = index)
        is Loading -> copy(galleryIds = ids, currentIndex = index)
    }

    fun withBitmapCache(id: Long, bitmap: Bitmap) = when (this) {
        is Content -> copy(bitmapCache = bitmapCache + (id to bitmap))
        is Loading -> copy(bitmapCache = bitmapCache + (id to bitmap))
    }

    fun getBitmapForPage(pageIndex: Int): Bitmap? {
        if (pageIndex !in galleryIds.indices) return null
        val id = galleryIds[pageIndex]
        return bitmapCache[id]
    }

    /**
     * Get thumbnail for blur placeholder (like Immich's loadingBuilder).
     * Returns any available bitmap for blur effect while loading.
     */
    fun getThumbnailForPage(pageIndex: Int): Bitmap? {
        // First try to get cached bitmap for this page
        val cached = getBitmapForPage(pageIndex)
        if (cached != null) return cached

        // Fallback: use current bitmap if available (for adjacent pages)
        return when (this) {
            is Content -> bitmap
            is Loading -> null
        }
    }

    enum class Tab(
        @StringRes val label: Int,
        @DrawableRes val iconRes: Int,
    ) {
        IMAGE(LocalizationR.string.gallery_tab_image, PresentationR.drawable.ic_image),
        ORIGINAL(LocalizationR.string.gallery_tab_original, PresentationR.drawable.ic_image),
        INFO(LocalizationR.string.gallery_tab_info, PresentationR.drawable.ic_text);

        companion object {
            fun consume(type: AiGenerationResult.Type): List<Tab> = when (type) {
                AiGenerationResult.Type.TEXT_TO_IMAGE -> listOf(
                    IMAGE, INFO,
                )

                AiGenerationResult.Type.IMAGE_TO_IMAGE -> entries
            }
        }
    }
}

fun Triple<AiGenerationResult, Base64ToBitmapConverter.Output, Base64ToBitmapConverter.Output?>.mapToUi(
    currentSource: ServerSource = ServerSource.AUTOMATIC1111,
): GalleryDetailState.Content =
    let { (ai, out, original) ->
        val isFalAi = ai.sampler.startsWith("fal.ai/")
        val falAiEndpointId = if (isFalAi) ai.sampler.removePrefix("fal.ai/") else ""
        GalleryDetailState.Content(
            tabs = GalleryDetailState.Tab.consume(ai.type),
            currentSource = currentSource,
            generationType = ai.type,
            id = ai.id,
            bitmap = out.bitmap,
            inputBitmap = original?.bitmap,
            createdAt = ai.createdAt.toString().asUiText(),
            type = if (isFalAi) "Fal AI".asUiText() else ai.type.key.asUiText(),
            prompt = ai.prompt.asUiText(),
            negativePrompt = ai.negativePrompt.asUiText(),
            size = "${ai.width} X ${ai.height}".asUiText(),
            samplingSteps = ai.samplingSteps.toString().asUiText(),
            cfgScale = ai.cfgScale.toString().asUiText(),
            restoreFaces = ai.restoreFaces.mapToUi(),
            sampler = if (isFalAi) falAiEndpointId.asUiText() else ai.sampler.asUiText(),
            seed = ai.seed.asUiText(),
            subSeed = ai.subSeed.asUiText(),
            subSeedStrength = ai.subSeedStrength.toString().asUiText(),
            denoisingStrength = ai.denoisingStrength.toString().asUiText(),
            hidden = ai.hidden,
            liked = ai.liked,
            isFalAi = isFalAi,
            falAiEndpointId = falAiEndpointId,
            modelName = ai.modelName.asUiText(),
        )
    }
