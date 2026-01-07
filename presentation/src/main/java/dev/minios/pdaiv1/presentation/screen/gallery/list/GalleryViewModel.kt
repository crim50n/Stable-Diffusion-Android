package dev.minios.pdaiv1.presentation.screen.gallery.list

import android.graphics.Bitmap
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.minios.pdaiv1.core.common.extensions.EmptyLambda
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.ThumbnailGenerator
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.entity.BackgroundWorkResult
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteAllGalleryUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteAllUnlikedUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteGalleryItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetAllGalleryUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryItemsRawUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryPagedIdsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetMediaStoreInfoUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetThumbnailInfoUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.LikeItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.UnlikeItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.HideItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.UnhideItemsUseCase
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import dev.minios.pdaiv1.presentation.core.GalleryItemStateEvent
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.navigation.router.drawer.DrawerRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.screen.drawer.DrawerIntent
import dev.minios.pdaiv1.presentation.utils.Constants
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.flow.Flow

class GalleryViewModel(
    dispatchersProvider: DispatchersProvider,
    getMediaStoreInfoUseCase: GetMediaStoreInfoUseCase,
    backgroundWorkObserver: BackgroundWorkObserver,
    private val preferenceManager: PreferenceManager,
    private val deleteAllGalleryUseCase: DeleteAllGalleryUseCase,
    private val deleteAllUnlikedUseCase: DeleteAllUnlikedUseCase,
    private val deleteGalleryItemsUseCase: DeleteGalleryItemsUseCase,
    private val getGenerationResultPagedUseCase: GetGenerationResultPagedUseCase,
    private val getGalleryPagedIdsUseCase: GetGalleryPagedIdsUseCase,
    private val getGalleryItemsUseCase: GetGalleryItemsUseCase,
    private val getGalleryItemsRawUseCase: GetGalleryItemsRawUseCase,
    private val getThumbnailInfoUseCase: GetThumbnailInfoUseCase,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val galleryExporter: GalleryExporter,
    private val schedulersProvider: SchedulersProvider,
    private val mainRouter: MainRouter,
    private val drawerRouter: DrawerRouter,
    private val mediaStoreGateway: MediaStoreGateway,
    private val mediaFileManager: MediaFileManager,
    private val getAllGalleryUseCase: GetAllGalleryUseCase,
    private val galleryItemStateEvent: GalleryItemStateEvent,
    private val likeItemsUseCase: LikeItemsUseCase,
    private val unlikeItemsUseCase: UnlikeItemsUseCase,
    private val hideItemsUseCase: HideItemsUseCase,
    private val unhideItemsUseCase: UnhideItemsUseCase,
) : MviRxViewModel<GalleryState, GalleryIntent, GalleryEffect>() {

    override val initialState = GalleryState()

    override val effectDispatcher = dispatchersProvider.immediate

    private val config = PagingConfig(
        pageSize = Constants.PAGINATION_PAYLOAD_SIZE,
        initialLoadSize = Constants.PAGINATION_PAYLOAD_SIZE * 3,
        prefetchDistance = Constants.PAGINATION_PAYLOAD_SIZE * 3,
        enablePlaceholders = false,
    )

    private val pager: Pager<Int, GalleryGridItemUi> = Pager(
        config = config,
        initialKey = GalleryPagingSource.FIRST_KEY,
        pagingSourceFactory = {
            GalleryPagingSource(
                getGenerationResultPagedUseCase,
                thumbnailGenerator,
                schedulersProvider,
            )
        }
    )

    val pagingFlow: Flow<PagingData<GalleryGridItemUi>> = pager.flow

    init {
        // Immich-style: Load all IDs at startup for instant scrolling
        loadAllIds()

        !preferenceManager
            .observe()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { settings ->
                updateState { it.copy(grid = settings.galleryGrid) }
            }

        !getMediaStoreInfoUseCase()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { info ->
                updateState { it.copy(mediaStoreInfo = info) }
            }

        !backgroundWorkObserver
            .observeResult()
            .ofType(BackgroundWorkResult.Success::class.java)
            .doOnNext { loadAllIds() } // Reload IDs when new image generated
            .map { GalleryEffect.Refresh }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::emitEffect)

        // Listen for new images during batch generation
        !backgroundWorkObserver
            .observeNewImage()
            .doOnNext { loadAllIds() } // Reload IDs when each new image is saved
            .map { GalleryEffect.Refresh }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::emitEffect)

        // Listen for gallery changes (deletions from detail view)
        !backgroundWorkObserver
            .observeGalleryChanged()
            .doOnNext { loadAllIds() } // Reload IDs when items deleted
            .map { GalleryEffect.Refresh }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::emitEffect)

        // Subscribe to real-time hidden state changes from detail view
        !galleryItemStateEvent.observeHiddenChanges()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { change ->
                updateState { state ->
                    state.copy(
                        hiddenIds = if (change.hidden) state.hiddenIds + change.itemId else state.hiddenIds - change.itemId
                    )
                }
            }

        // Subscribe to real-time liked state changes from detail view
        !galleryItemStateEvent.observeLikedChanges()
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { change ->
                updateState { state ->
                    state.copy(
                        likedIds = if (change.liked) state.likedIds + change.itemId else state.likedIds - change.itemId
                    )
                }
            }
    }

    private fun loadAllIds() {
        !getGalleryPagedIdsUseCase.withBlurHash()
            .subscribeOn(schedulersProvider.io)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { idsWithBlurHash ->
                val ids = idsWithBlurHash.map { it.first }
                val blurHashMap = idsWithBlurHash
                    .filter { it.second.isNotEmpty() }
                    .associate { it.first to it.second }
                updateState { state ->
                    state.copy(
                        allIds = ids,
                        blurHashCache = state.blurHashCache + blurHashMap,
                        isInitialLoading = false,
                    )
                }
            }
    }

    private fun loadThumbnails(ids: List<Long>) {
        // Filter out already loaded or loading IDs
        val idsToLoad = ids.filter { id ->
            !currentState.thumbnailCache.containsKey(id) &&
            !currentState.loadingIds.contains(id)
        }

        if (idsToLoad.isEmpty()) return

        // Mark as loading
        updateState { state ->
            state.copy(loadingIds = state.loadingIds + idsToLoad.toSet())
        }

        // Load thumbnails using raw data (without loading full images into memory)
        !getGalleryItemsRawUseCase(idsToLoad)
            .subscribeOn(schedulersProvider.io)
            .flatMapObservable { Observable.fromIterable(it) }
            .flatMap(
                { ai ->
                    // Use file-based loading if mediaPath is available, otherwise fall back to base64
                    val thumbnailSingle = if (ai.mediaPath.isNotEmpty()) {
                        // Convert relative path to absolute path
                        val absolutePath = mediaFileManager.getMediaFile(ai.mediaPath).absolutePath
                        thumbnailGenerator.generateFromFile(
                            id = ai.id.toString(),
                            filePath = absolutePath,
                        )
                    } else if (ai.image.isNotEmpty()) {
                        thumbnailGenerator.generate(
                            id = ai.id.toString(),
                            base64ImageString = ai.image,
                        )
                    } else {
                        // No image available, skip
                        io.reactivex.rxjava3.core.Single.never()
                    }
                    thumbnailSingle
                        .map { bitmap -> ThumbnailData(ai.id, ai.hidden, ai.liked, bitmap, ai.blurHash) }
                        .toObservable()
                        .subscribeOn(schedulersProvider.computation)
                },
                MAX_CONCURRENT_THUMBNAIL_LOADS,
            )
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog) { data: ThumbnailData ->
                updateState { state ->
                    state.copy(
                        thumbnailCache = state.thumbnailCache + (data.id to data.bitmap),
                        blurHashCache = if (data.blurHash.isNotEmpty()) state.blurHashCache + (data.id to data.blurHash) else state.blurHashCache,
                        loadingIds = state.loadingIds - data.id,
                        hiddenIds = if (data.hidden) state.hiddenIds + data.id else state.hiddenIds - data.id,
                        likedIds = if (data.liked) state.likedIds + data.id else state.likedIds - data.id,
                    )
                }
            }
    }

    private data class ThumbnailData(
        val id: Long,
        val hidden: Boolean,
        val liked: Boolean,
        val bitmap: Bitmap,
        val blurHash: String,
    )

    override fun processIntent(intent: GalleryIntent) {
        when (intent) {
            GalleryIntent.DismissDialog -> setActiveModal(Modal.None)

            GalleryIntent.Export.All.Request -> setActiveModal(Modal.ConfirmExport(true))

            GalleryIntent.Export.All.Confirm -> launchGalleryExport(true)

            GalleryIntent.Export.Selection.Request -> setActiveModal(Modal.ConfirmExport(false))

            GalleryIntent.Export.Selection.Confirm -> launchGalleryExport(false)

            is GalleryIntent.OpenItem -> {
                updateState {
                    it.copy(
                        selectedItemId = intent.id,
                        selectedItemIndex = intent.index,
                        scrollToItemIndex = intent.index
                    )
                }
            }

            GalleryIntent.CloseItem -> {
                // Hidden/liked states are synced in real-time via GalleryItemStateEvent
                updateState { it.copy(selectedItemId = null, selectedItemIndex = null) }
            }

            GalleryIntent.ClearScrollPosition -> {
                updateState { it.copy(scrollToItemIndex = null) }
            }

            is GalleryIntent.OpenMediaStoreFolder -> emitEffect(GalleryEffect.OpenUri(intent.uri))

            is GalleryIntent.Drawer -> when (intent.intent) {
                DrawerIntent.Close -> drawerRouter.closeDrawer()
                DrawerIntent.Open -> drawerRouter.openDrawer()
            }

            is GalleryIntent.ChangeSelectionMode -> updateState {
                it.copy(
                    selectionMode = intent.flag,
                    selection = if (!intent.flag) emptyList() else it.selection,
                )
            }

            is GalleryIntent.ToggleItemSelection -> updateState {
                val selectionIndex = it.selection.indexOf(intent.id)
                val newSelection = it.selection.toMutableList()
                if (selectionIndex != -1) {
                    newSelection.removeAt(selectionIndex)
                } else {
                    newSelection.add(intent.id)
                }
                it.copy(selection = newSelection)
            }

            GalleryIntent.Delete.Selection.Request -> setActiveModal(
                Modal.DeleteImageConfirm(isAll = false, isMultiple = true)
            )

            GalleryIntent.Delete.Selection.Confirm -> !deleteGalleryItemsUseCase
                .invoke(currentState.selection)
                .processDeletion()

            GalleryIntent.Delete.All.Request -> setActiveModal(
                Modal.DeleteImageConfirm(isAll = true, isMultiple = false)
            )

            GalleryIntent.Delete.All.Confirm -> !deleteAllGalleryUseCase()
                .processDeletion()

            GalleryIntent.Delete.AllUnliked.Request -> setActiveModal(
                Modal.DeleteUnlikedConfirm
            )

            GalleryIntent.Delete.AllUnliked.Confirm -> !deleteAllUnlikedUseCase()
                .processDeletion()

            GalleryIntent.LikeSelection -> {
                val selection = currentState.selection
                if (selection.isNotEmpty()) {
                    // Toggle logic: if ALL selected are liked -> unlike, otherwise -> like all
                    val allLiked = selection.all { it in currentState.likedIds }
                    val useCase = if (allLiked) unlikeItemsUseCase(selection) else likeItemsUseCase(selection)
                    val newLikedState = !allLiked
                    
                    !useCase
                        .subscribeOn(schedulersProvider.io)
                        .subscribeOnMainThread(schedulersProvider)
                        .subscribeBy(::errorLog) {
                            // Update liked state and emit events for real-time sync
                            updateState { state ->
                                val newLikedIds = if (newLikedState) {
                                    state.likedIds + selection.toSet()
                                } else {
                                    state.likedIds - selection.toSet()
                                }
                                state.copy(
                                    likedIds = newLikedIds,
                                    selectionMode = false,
                                    selection = emptyList(),
                                )
                            }
                            selection.forEach { id ->
                                galleryItemStateEvent.emitLikedChange(id, newLikedState)
                            }
                        }
                }
            }

            GalleryIntent.HideSelection -> {
                val selection = currentState.selection
                if (selection.isNotEmpty()) {
                    // Toggle logic: if ALL selected are hidden -> unhide, otherwise -> hide all
                    val allHidden = selection.all { it in currentState.hiddenIds }
                    val useCase = if (allHidden) unhideItemsUseCase(selection) else hideItemsUseCase(selection)
                    val newHiddenState = !allHidden
                    
                    !useCase
                        .subscribeOn(schedulersProvider.io)
                        .subscribeOnMainThread(schedulersProvider)
                        .subscribeBy(::errorLog) {
                            // Update hidden state and emit events for real-time sync
                            updateState { state ->
                                val newHiddenIds = if (newHiddenState) {
                                    state.hiddenIds + selection.toSet()
                                } else {
                                    state.hiddenIds - selection.toSet()
                                }
                                state.copy(
                                    hiddenIds = newHiddenIds,
                                    selectionMode = false,
                                    selection = emptyList(),
                                )
                            }
                            selection.forEach { id ->
                                galleryItemStateEvent.emitHiddenChange(id, newHiddenState)
                            }
                        }
                }
            }

            GalleryIntent.UnselectAll -> updateState {
                it.copy(selection = emptyList())
            }

            GalleryIntent.Dropdown.Toggle -> updateState {
                it.copy(dropdownMenuShow = !it.dropdownMenuShow)
            }

            GalleryIntent.Dropdown.Show -> updateState {
                it.copy(dropdownMenuShow = true)
            }

            GalleryIntent.Dropdown.Close -> updateState {
                it.copy(dropdownMenuShow = false)
            }

            GalleryIntent.SaveToGallery.All.Request -> setActiveModal(
                Modal.ConfirmSaveToGallery(saveAll = true)
            )

            GalleryIntent.SaveToGallery.All.Confirm -> saveAllToGallery()

            GalleryIntent.SaveToGallery.Selection.Request -> setActiveModal(
                Modal.ConfirmSaveToGallery(saveAll = false)
            )

            GalleryIntent.SaveToGallery.Selection.Confirm -> saveSelectionToGallery()

            GalleryIntent.GridZoom.ZoomIn -> {
                val newGrid = dev.minios.pdaiv1.domain.entity.Grid.zoomIn(currentState.grid)
                preferenceManager.galleryGrid = newGrid
            }

            GalleryIntent.GridZoom.ZoomOut -> {
                val newGrid = dev.minios.pdaiv1.domain.entity.Grid.zoomOut(currentState.grid)
                preferenceManager.galleryGrid = newGrid
            }

            is GalleryIntent.DragSelection.Start -> {
                updateState {
                    it.copy(
                        selectionMode = true,
                        selection = listOf(intent.itemId),
                    )
                }
            }

            is GalleryIntent.DragSelection.UpdateRange -> {
                updateState {
                    it.copy(selection = intent.itemIds.distinct())
                }
            }

            GalleryIntent.DragSelection.End -> {
                // Drag ended - selection is already updated
            }

            is GalleryIntent.LoadThumbnails -> loadThumbnails(intent.ids)
        }
    }

    private fun Completable.processDeletion() = this
        .doOnSubscribe { setActiveModal(Modal.None) }
        .doOnComplete { loadAllIds() } // Reload IDs after deletion
        .subscribeOnMainThread(schedulersProvider)
        .subscribeBy(::errorLog) {
            updateState {
                it.copy(
                    selectionMode = false,
                    selection = emptyList()
                )
            }
            emitEffect(GalleryEffect.Refresh)
        }

    private fun launchGalleryExport(exportAll: Boolean) = !galleryExporter
        .invoke(if (exportAll) null else currentState.selection)
        .doOnSubscribe { setActiveModal(Modal.ExportInProgress) }
        .subscribeOnMainThread(schedulersProvider)
        .subscribeBy(
            onError = { t ->
                setActiveModal(
                    Modal.Error(
                        (t.localizedMessage ?: "Something went wrong").asUiText()
                    )
                )
                errorLog(t)
            },
            onSuccess = { zipFile ->
                setActiveModal(Modal.None)
                emitEffect(GalleryEffect.Share(zipFile))
            }
        )

    private fun setActiveModal(dialog: Modal) = updateState {
        it.copy(screenModal = dialog, dropdownMenuShow = false)
    }

    private fun saveAllToGallery() {
        // Get all IDs first, then process in parallel
        !getGalleryPagedIdsUseCase()
            .doOnSubscribe { setActiveModal(Modal.ExportInProgress) }
            .flatMapObservable { allIds -> 
                io.reactivex.rxjava3.core.Observable.fromIterable(allIds)
            }
            .flatMap({ id: Long ->
                getGalleryItemsRawUseCase(listOf(id))
                    .flatMapCompletable { items ->
                        val item = items.firstOrNull()
                        if (item == null) {
                            Completable.complete()
                        } else {
                            Completable.fromAction { exportItemToGallery(item) }
                        }
                    }
                    .toObservable<Unit>()
            }, 4) // Process 4 items in parallel
            .subscribeOn(schedulersProvider.io)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    setActiveModal(
                        Modal.Error(
                            (t.localizedMessage ?: "Something went wrong").asUiText()
                        )
                    )
                    errorLog(t)
                },
                onComplete = {
                    setActiveModal(Modal.None)
                    emitEffect(GalleryEffect.AllImagesSavedToGallery)
                }
            )
    }

    private fun saveSelectionToGallery() {
        val selectedIds = currentState.selection
        !getGalleryItemsRawUseCase(selectedIds)
            .doOnSubscribe { setActiveModal(Modal.ExportInProgress) }
            .flatMapObservable { io.reactivex.rxjava3.core.Observable.fromIterable(it) }
            .flatMap({ item ->
                Completable.fromAction { exportItemToGallery(item) }
                    .toObservable<Unit>()
            }, 4) // Process 4 items in parallel
            .subscribeOn(schedulersProvider.io)
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    setActiveModal(
                        Modal.Error(
                            (t.localizedMessage ?: "Something went wrong").asUiText()
                        )
                    )
                    errorLog(t)
                },
                onComplete = {
                    setActiveModal(Modal.None)
                    updateState {
                        it.copy(
                            selectionMode = false,
                            selection = emptyList()
                        )
                    }
                    emitEffect(GalleryEffect.SelectionImagesSavedToGallery)
                }
            )
    }

    private fun exportItemToGallery(item: dev.minios.pdaiv1.domain.entity.AiGenerationResult) {
        // Export directly from file if available (fast path - no memory allocation for image data)
        if (item.mediaPath.isNotEmpty() && mediaFileManager.isFilePath(item.mediaPath)) {
            val file = mediaFileManager.getMediaFile(item.mediaPath)
            if (file.exists()) {
                mediaStoreGateway.exportFromFile(
                    fileName = "pdai_${item.id}_${System.currentTimeMillis()}",
                    sourceFile = file,
                )
                return
            }
        }
        // Fallback to base64 if no file path (legacy data)
        if (item.image.isNotEmpty()) {
            val output = base64ToBitmapConverter(Base64ToBitmapConverter.Input(item.image)).blockingGet()
            val stream = java.io.ByteArrayOutputStream()
            output.bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            mediaStoreGateway.exportToFile(
                fileName = "pdai_${item.id}_${System.currentTimeMillis()}",
                content = stream.toByteArray(),
            )
        }
    }

    companion object {
        private const val MAX_CONCURRENT_THUMBNAIL_LOADS = 32
    }
}
