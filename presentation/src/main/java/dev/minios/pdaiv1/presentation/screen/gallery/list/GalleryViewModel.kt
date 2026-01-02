package dev.minios.pdaiv1.presentation.screen.gallery.list

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.minios.pdaiv1.core.common.extensions.EmptyLambda
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.entity.BackgroundWorkResult
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteAllGalleryUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteGalleryItemsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetAllGalleryUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetMediaStoreInfoUseCase
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.navigation.router.drawer.DrawerRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.screen.drawer.DrawerIntent
import dev.minios.pdaiv1.presentation.utils.Constants
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.flow.Flow

class GalleryViewModel(
    dispatchersProvider: DispatchersProvider,
    getMediaStoreInfoUseCase: GetMediaStoreInfoUseCase,
    backgroundWorkObserver: BackgroundWorkObserver,
    preferenceManager: PreferenceManager,
    private val deleteAllGalleryUseCase: DeleteAllGalleryUseCase,
    private val deleteGalleryItemsUseCase: DeleteGalleryItemsUseCase,
    private val getGenerationResultPagedUseCase: GetGenerationResultPagedUseCase,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
    private val galleryExporter: GalleryExporter,
    private val schedulersProvider: SchedulersProvider,
    private val mainRouter: MainRouter,
    private val drawerRouter: DrawerRouter,
    private val mediaStoreGateway: MediaStoreGateway,
    private val getAllGalleryUseCase: GetAllGalleryUseCase,
) : MviRxViewModel<GalleryState, GalleryIntent, GalleryEffect>() {

    override val initialState = GalleryState()

    override val effectDispatcher = dispatchersProvider.immediate

    private val config = PagingConfig(
        pageSize = Constants.PAGINATION_PAYLOAD_SIZE,
        initialLoadSize = Constants.PAGINATION_PAYLOAD_SIZE
    )

    private val pager: Pager<Int, GalleryGridItemUi> = Pager(
        config = config,
        initialKey = GalleryPagingSource.FIRST_KEY,
        pagingSourceFactory = {
            GalleryPagingSource(
                getGenerationResultPagedUseCase,
                base64ToBitmapConverter,
                schedulersProvider,
            )
        }
    )

    val pagingFlow: Flow<PagingData<GalleryGridItemUi>> = pager.flow

    init {
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
            .map { GalleryEffect.Refresh }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(::errorLog, EmptyLambda, ::emitEffect)
    }

    override fun processIntent(intent: GalleryIntent) {
        when (intent) {
            GalleryIntent.DismissDialog -> setActiveModal(Modal.None)

            GalleryIntent.Export.All.Request -> setActiveModal(Modal.ConfirmExport(true))

            GalleryIntent.Export.All.Confirm -> launchGalleryExport(true)

            GalleryIntent.Export.Selection.Request -> setActiveModal(Modal.ConfirmExport(false))

            GalleryIntent.Export.Selection.Confirm -> launchGalleryExport(false)

            is GalleryIntent.OpenItem -> {
                android.util.Log.d("GalleryScroll", "OpenItem: saving index ${intent.index}")
                updateState { it.copy(scrollToItemIndex = intent.index) }
                mainRouter.navigateToGalleryDetails(intent.item.id)
            }

            GalleryIntent.ClearScrollPosition -> {
                android.util.Log.d("GalleryScroll", "ClearScrollPosition called")
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
        }
    }

    private fun Completable.processDeletion() = this
        .doOnSubscribe { setActiveModal(Modal.None) }
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
        !getAllGalleryUseCase()
            .doOnSubscribe { setActiveModal(Modal.ExportInProgress) }
            .flatMapObservable { io.reactivex.rxjava3.core.Observable.fromIterable(it) }
            .flatMapSingle { item ->
                base64ToBitmapConverter(Base64ToBitmapConverter.Input(item.image))
                    .map { output -> item to output }
            }
            .flatMapCompletable { (item, output) ->
                Completable.fromAction {
                    val stream = java.io.ByteArrayOutputStream()
                    output.bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                    mediaStoreGateway.exportToFile(
                        fileName = "pdai_${item.id}_${System.currentTimeMillis()}",
                        content = stream.toByteArray(),
                    )
                }
            }
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
        !getAllGalleryUseCase()
            .doOnSubscribe { setActiveModal(Modal.ExportInProgress) }
            .flatMapObservable { io.reactivex.rxjava3.core.Observable.fromIterable(it) }
            .filter { item -> selectedIds.contains(item.id) }
            .flatMapSingle { item ->
                base64ToBitmapConverter(Base64ToBitmapConverter.Input(item.image))
                    .map { output -> item to output }
            }
            .flatMapCompletable { (item, output) ->
                Completable.fromAction {
                    val stream = java.io.ByteArrayOutputStream()
                    output.bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
                    mediaStoreGateway.exportToFile(
                        fileName = "pdai_${item.id}_${System.currentTimeMillis()}",
                        content = stream.toByteArray(),
                    )
                }
            }
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
}
