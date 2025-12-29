package com.shifthackz.aisdv1.presentation.screen.gallery.detail

import android.graphics.Bitmap
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.log.errorLog
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersProvider
import com.shifthackz.aisdv1.core.common.schedulers.subscribeOnMainThread
import com.shifthackz.aisdv1.core.imageprocessing.Base64ToBitmapConverter
import com.shifthackz.aisdv1.core.imageprocessing.Base64ToBitmapConverter.Input
import com.shifthackz.aisdv1.core.viewmodel.MviRxViewModel
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetGalleryPagedIdsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageVisibilityUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCase
import com.shifthackz.aisdv1.presentation.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.model.Modal
import com.shifthackz.aisdv1.presentation.navigation.router.main.MainRouter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GalleryDetailViewModel(
    private val itemId: Long,
    dispatchersProvider: DispatchersProvider,
    private val buildInfoProvider: BuildInfoProvider,
    private val preferenceManager: PreferenceManager,
    private val getGenerationResultUseCase: GetGenerationResultUseCase,
    private val getLastResultFromCacheUseCase: GetLastResultFromCacheUseCase,
    private val getGalleryPagedIdsUseCase: GetGalleryPagedIdsUseCase,
    private val deleteGalleryItemUseCase: DeleteGalleryItemUseCase,
    private val toggleImageVisibilityUseCase: ToggleImageVisibilityUseCase,
    private val galleryDetailBitmapExporter: GalleryDetailBitmapExporter,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
    private val schedulersProvider: SchedulersProvider,
    private val generationFormUpdateEvent: GenerationFormUpdateEvent,
    private val mainRouter: MainRouter,
    private val mediaStoreGateway: MediaStoreGateway,
) : MviRxViewModel<GalleryDetailState, GalleryDetailIntent, GalleryDetailEffect>() {

    override val initialState = GalleryDetailState.Loading(currentSource = preferenceManager.source)

    override val effectDispatcher = dispatchersProvider.immediate

    private var currentItemId: Long = itemId

    init {
        // Load all gallery IDs first, then load the current item
        !getGalleryPagedIdsUseCase()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { ids ->
                val index = ids.indexOf(itemId).coerceAtLeast(0)
                updateState { it.withGalleryIds(ids, index) }
                loadItem(itemId)
            }
    }

    private fun loadItem(id: Long) {
        currentItemId = id
        !getGenerationResult(id)
            .subscribeOnMainThread(schedulersProvider)
            .postProcess()
            .subscribeBy(::errorLog) { ai ->
                updateState { state ->
                    val newIndex = state.galleryIds.indexOf(id).coerceAtLeast(0)
                    ai.mapToUi(preferenceManager.source)
                        .copy(
                            showReportButton = buildInfoProvider.type != BuildType.FOSS,
                            galleryIds = state.galleryIds,
                            currentIndex = newIndex,
                            bitmapCache = state.bitmapCache + (id to ai.second.bitmap),
                            controlsVisible = state.controlsVisible,
                        )
                        .withTab(state.selectedTab)
                }
                // Preload adjacent images
                preloadAdjacentImages(id)
            }
    }

    private fun preloadAdjacentImages(currentId: Long) {
        val ids = currentState.galleryIds
        val currentIndex = ids.indexOf(currentId)
        if (currentIndex < 0) return

        // Preload previous and next images
        listOf(currentIndex - 1, currentIndex + 1)
            .filter { it in ids.indices }
            .map { ids[it] }
            .filter { it !in currentState.bitmapCache }
            .forEach { adjacentId ->
                preloadBitmap(adjacentId)
            }
    }

    private fun preloadBitmap(id: Long) {
        !getGenerationResult(id)
            .subscribeOn(schedulersProvider.io)
            .flatMap { ai ->
                base64ToBitmapConverter(Input(ai.image)).map { bmp -> id to bmp.bitmap }
            }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { /* Ignore preload errors */ },
                onSuccess = { (loadedId, bitmap) ->
                    updateState { state ->
                        state.withBitmapCache(loadedId, bitmap)
                    }
                }
            )
    }

    override fun processIntent(intent: GalleryDetailIntent) {
        when (intent) {
            is GalleryDetailIntent.CopyToClipboard -> {
                emitEffect(GalleryDetailEffect.ShareClipBoard(intent.content.toString()))
            }

            GalleryDetailIntent.Delete.Request -> setActiveModal(
                Modal.DeleteImageConfirm(false, isMultiple = false)
            )

            GalleryDetailIntent.Delete.Confirm -> {
                setActiveModal(Modal.None)
                delete()
            }

            GalleryDetailIntent.Export.Image -> share()

            GalleryDetailIntent.Export.Params -> {
                emitEffect(GalleryDetailEffect.ShareGenerationParams(currentState))
            }

            GalleryDetailIntent.NavigateBack -> mainRouter.navigateBack()

            is GalleryDetailIntent.SelectTab -> updateState {
                it.withTab(intent.tab)
            }

            GalleryDetailIntent.SendTo.Txt2Img -> sendPromptToGenerationScreen(
                AiGenerationResult.Type.TEXT_TO_IMAGE,
            )

            GalleryDetailIntent.SendTo.Img2Img -> sendPromptToGenerationScreen(
                AiGenerationResult.Type.IMAGE_TO_IMAGE,
            )

            GalleryDetailIntent.SendTo.FalAi -> sendPromptToFalAi()

            GalleryDetailIntent.DismissDialog -> setActiveModal(Modal.None)

            GalleryDetailIntent.Report -> (currentState as? GalleryDetailState.Content)
                ?.id
                ?.let(mainRouter::navigateToReportImage)

            GalleryDetailIntent.ToggleVisibility -> toggleVisibility()

            GalleryDetailIntent.ToggleControlsVisibility -> updateState {
                it.withControlsVisible(!it.controlsVisible)
            }

            GalleryDetailIntent.SaveToGallery -> saveToGallery()

            is GalleryDetailIntent.PageChanged -> {
                val ids = currentState.galleryIds
                if (intent.index in ids.indices) {
                    val newId = ids[intent.index]
                    if (newId != currentItemId) {
                        loadItem(newId)
                    }
                }
            }
        }
    }

    private fun saveToGallery() {
        val state = currentState as? GalleryDetailState.Content ?: return
        val bitmap = state.bitmap
        !Completable.fromAction {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            mediaStoreGateway.exportToFile(
                fileName = "sdai_${System.currentTimeMillis()}",
                content = stream.toByteArray(),
            )
        }
            .subscribeOn(schedulersProvider.io)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) {
                emitEffect(GalleryDetailEffect.ImageSavedToGallery)
            }
    }

    private fun share() {
        val state = currentState as? GalleryDetailState.Content ?: return
        val bitmap = if (
            state.generationType == AiGenerationResult.Type.IMAGE_TO_IMAGE
            && state.inputBitmap != null
            && state.selectedTab == GalleryDetailState.Tab.ORIGINAL
        ) {
            state.inputBitmap
        } else {
            state.bitmap
        }
        !galleryDetailBitmapExporter(bitmap)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { file ->
                emitEffect(GalleryDetailEffect.ShareImageFile(file))
            }
    }

    private fun delete() {
        val state = currentState as? GalleryDetailState.Content ?: return
        val deletedId = state.id
        val ids = state.galleryIds
        val currentIndex = ids.indexOf(deletedId)

        val newIds = ids.filter { it != deletedId }

        if (newIds.isEmpty()) {
            // No more images after deletion, just delete and go back
            !deleteGalleryItemUseCase(deletedId)
                .subscribeOnMainThread(schedulersProvider)
                .subscribeBy(::errorLog) {
                    mainRouter.navigateBack()
                }
        } else {
            // Determine target page for swipe animation (next or previous)
            val targetPage = if (currentIndex < ids.size - 1) {
                currentIndex + 1
            } else {
                currentIndex - 1
            }

            // Trigger swipe animation to next/previous page
            updateState {
                (it as? GalleryDetailState.Content)?.copy(animateToPage = targetPage) ?: it
            }

            // After animation completes, delete and update state
            !deleteGalleryItemUseCase(deletedId)
                .delay(350, TimeUnit.MILLISECONDS)
                .subscribeOnMainThread(schedulersProvider)
                .subscribeBy(::errorLog) {
                    val nextIndex = currentIndex.coerceAtMost(newIds.size - 1)
                    val nextId = newIds[nextIndex]

                    updateState {
                        (it as? GalleryDetailState.Content)?.copy(
                            galleryIds = newIds,
                            currentIndex = nextIndex,
                            animateToPage = null
                        ) ?: it.withGalleryIds(newIds, nextIndex)
                    }
                    loadItem(nextId)
                }
        }
    }

    private fun setActiveModal(dialog: Modal) = updateState {
        it.withDialog(dialog)
    }

    private fun Single<AiGenerationResult>.postProcess() = this
        .flatMap { ai ->
            base64ToBitmapConverter(Input(ai.image)).map { bmp -> ai to bmp }
        }
        .flatMap { (ai, bmp) ->
            when (ai.type) {
                AiGenerationResult.Type.TEXT_TO_IMAGE -> Single.just(Triple(ai, bmp, null))
                AiGenerationResult.Type.IMAGE_TO_IMAGE ->
                    base64ToBitmapConverter(Input(ai.inputImage)).map { bmp2 ->
                        Triple(ai, bmp, bmp2)
                    }
            }
        }

    private fun sendPromptToGenerationScreen(screenType: AiGenerationResult.Type) {
        val state = (currentState as? GalleryDetailState.Content) ?: return
        !getGenerationResult(currentItemId)
            .subscribeOnMainThread(schedulersProvider)
            .doFinally { mainRouter.navigateBack() }
            .subscribeBy(::errorLog) { ai ->
                generationFormUpdateEvent.update(
                    ai,
                    screenType,
                    state.selectedTab == GalleryDetailState.Tab.ORIGINAL,
                )
            }

    }

    private fun sendPromptToFalAi() {
        !getGenerationResult(currentItemId)
            .subscribeOnMainThread(schedulersProvider)
            .doFinally { mainRouter.navigateBack() }
            .subscribeBy(::errorLog) { ai ->
                generationFormUpdateEvent.updateFalAi(ai)
            }
    }

    private fun toggleVisibility() = !toggleImageVisibilityUseCase(currentItemId)
        .subscribeOnMainThread(schedulersProvider)
        .subscribeBy(::errorLog) { hidden ->
            updateState { it.withHiddenState(hidden) }
        }

    private fun getGenerationResult(id: Long): Single<AiGenerationResult> {
        if (id <= 0) return getLastResultFromCacheUseCase()
        return getGenerationResultUseCase(id)
    }
}
