@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.minios.pdaiv1.presentation.screen.gallery.list

import android.graphics.Bitmap
import android.net.Uri
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.ThumbnailGenerator
import dev.minios.pdaiv1.domain.entity.MediaStoreInfo
import dev.minios.pdaiv1.domain.entity.Settings
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.feature.MediaFileManager
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
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.presentation.core.CoreViewModelTest
import dev.minios.pdaiv1.presentation.core.GalleryItemStateEvent
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.navigation.router.drawer.DrawerRouter
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.stub.stubDispatchersProvider
import dev.minios.pdaiv1.presentation.stub.stubSchedulersProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File

@Ignore("ToDo: Investigate why sometimes tests fail on remote worker due to race-conditions.")
class GalleryViewModelTest : CoreViewModelTest<GalleryViewModel>() {

    private val stubMediaStoreInfo = MediaStoreInfo(5598)
    private val stubFile = mockk<File>()
    private val stubBitmap = mockk<Bitmap>()
    private val stubUri = mockk<Uri>()
    private val stubGetMediaStoreInfoUseCase = mockk<GetMediaStoreInfoUseCase>()
    private val stubGetGenerationResultPagedUseCase = mockk<GetGenerationResultPagedUseCase>()
    private val stubGetGalleryPagedIdsUseCase = mockk<GetGalleryPagedIdsUseCase>()
    private val stubGetGalleryItemsUseCase = mockk<GetGalleryItemsUseCase>()
    private val stubGetGalleryItemsRawUseCase = mockk<GetGalleryItemsRawUseCase>()
    private val stubGetThumbnailInfoUseCase = mockk<GetThumbnailInfoUseCase>()
    private val stubBase64ToBitmapConverter = mockk<Base64ToBitmapConverter>()
    private val stubThumbnailGenerator = mockk<ThumbnailGenerator>()
    private val stubGalleryExporter = mockk<GalleryExporter>()
    private val stubMainRouter = mockk<MainRouter>()
    private val stubDrawerRouter = mockk<DrawerRouter>()
    private val stubDeleteAllGalleryUseCase = mockk<DeleteAllGalleryUseCase>()
    private val stubDeleteAllUnlikedUseCase = mockk<DeleteAllUnlikedUseCase>()
    private val stubDeleteGalleryItemsUseCase = mockk<DeleteGalleryItemsUseCase>()
    private val stubBackgroundWorkObserver = mockk<BackgroundWorkObserver>()
    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubMediaStoreGateway = mockk<MediaStoreGateway>()
    private val stubMediaFileManager = mockk<MediaFileManager>()
    private val stubGetAllGalleryUseCase = mockk<GetAllGalleryUseCase>()
    private val stubGalleryItemStateEvent = mockk<GalleryItemStateEvent>()
    private val stubLikeItemsUseCase = mockk<LikeItemsUseCase>()
    private val stubUnlikeItemsUseCase = mockk<UnlikeItemsUseCase>()
    private val stubHideItemsUseCase = mockk<HideItemsUseCase>()
    private val stubUnhideItemsUseCase = mockk<UnhideItemsUseCase>()

    override fun initializeViewModel() = GalleryViewModel(
        dispatchersProvider = stubDispatchersProvider,
        getMediaStoreInfoUseCase = stubGetMediaStoreInfoUseCase,
        backgroundWorkObserver = stubBackgroundWorkObserver,
        preferenceManager = stubPreferenceManager,
        deleteAllGalleryUseCase = stubDeleteAllGalleryUseCase,
        deleteAllUnlikedUseCase = stubDeleteAllUnlikedUseCase,
        deleteGalleryItemsUseCase = stubDeleteGalleryItemsUseCase,
        getGenerationResultPagedUseCase = stubGetGenerationResultPagedUseCase,
        getGalleryPagedIdsUseCase = stubGetGalleryPagedIdsUseCase,
        getGalleryItemsUseCase = stubGetGalleryItemsUseCase,
        getGalleryItemsRawUseCase = stubGetGalleryItemsRawUseCase,
        getThumbnailInfoUseCase = stubGetThumbnailInfoUseCase,
        base64ToBitmapConverter = stubBase64ToBitmapConverter,
        thumbnailGenerator = stubThumbnailGenerator,
        galleryExporter = stubGalleryExporter,
        schedulersProvider = stubSchedulersProvider,
        mainRouter = stubMainRouter,
        drawerRouter = stubDrawerRouter,
        mediaStoreGateway = stubMediaStoreGateway,
        mediaFileManager = stubMediaFileManager,
        getAllGalleryUseCase = stubGetAllGalleryUseCase,
        galleryItemStateEvent = stubGalleryItemStateEvent,
        likeItemsUseCase = stubLikeItemsUseCase,
        unlikeItemsUseCase = stubUnlikeItemsUseCase,
        hideItemsUseCase = stubHideItemsUseCase,
        unhideItemsUseCase = stubUnhideItemsUseCase,
    )

    @Before
    override fun initialize() {
        super.initialize()

        every {
            stubPreferenceManager.observe()
        } returns Flowable.just(Settings())

        every {
            stubBackgroundWorkObserver.observeResult()
        } returns Flowable.empty()

        every {
            stubBackgroundWorkObserver.observeNewImage()
        } returns Flowable.empty()

        every {
            stubBackgroundWorkObserver.observeGalleryChanged()
        } returns Flowable.empty()

        every {
            stubGetMediaStoreInfoUseCase()
        } returns Single.just(stubMediaStoreInfo)

        every {
            stubGetGalleryPagedIdsUseCase.withBlurHash()
        } returns Single.just(emptyList())

        every {
            stubGalleryItemStateEvent.observeHiddenChanges()
        } returns Flowable.empty()

        every {
            stubGalleryItemStateEvent.observeLikedChanges()
        } returns Flowable.empty()
    }

    @Test
    fun `initialized, expected mediaStoreInfo field in UI state equals stubMediaStoreInfo`() {
        runTest {
            val expected = stubMediaStoreInfo
            val actual = viewModel.state.value.mediaStoreInfo
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received DismissDialog intent, expected screenModal field in UI state is None`() {
        viewModel.processIntent(GalleryIntent.DismissDialog)
        runTest {
            val expected = Modal.None
            val actual = viewModel.state.value.screenModal
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received Export Request intent, expected screenModal field in UI state is ConfirmExport`() {
        viewModel.processIntent(GalleryIntent.Export.All.Request)
        runTest {
            val expected = Modal.ConfirmExport(true)
            val actual = viewModel.state.value.screenModal
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received Export Confirm intent, expected screenModal field in UI state is None, Share effect delivered to effect collector`() {
        every {
            stubGalleryExporter()
        } returns Single.just(stubFile)

        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel.processIntent(GalleryIntent.Export.All.Confirm)

        runTest {
            val expectedUiState = Modal.None
            val actualUiState = viewModel.state.value.screenModal
            Assert.assertEquals(expectedUiState, actualUiState)

            val expectedEffect = GalleryEffect.Share(stubFile)
            val actualEffect = viewModel.effect.firstOrNull()
            Assert.assertEquals(expectedEffect, actualEffect)
        }
        verify {
            stubGalleryExporter()
        }
    }

    @Test
    fun `given received OpenItem intent, expected selectedItemId in state equals item id`() {
        viewModel.processIntent(GalleryIntent.OpenItem(5598L, 0))

        runTest {
            val expected = 5598L
            val actual = viewModel.state.value.selectedItemId
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received OpenMediaStoreFolder intent, expected OpenUri effect delivered to effect collector`() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel.processIntent(GalleryIntent.OpenMediaStoreFolder(stubUri))
        runTest {
            val expected = GalleryEffect.OpenUri(stubUri)
            val actual = viewModel.effect.firstOrNull()
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received Delete AllUnliked Request intent, expected screenModal is DeleteUnlikedConfirm`() {
        viewModel.processIntent(GalleryIntent.Delete.AllUnliked.Request)
        runTest {
            val expected = Modal.DeleteUnlikedConfirm
            val actual = viewModel.state.value.screenModal
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received Delete AllUnliked Confirm intent, expected deleteAllUnlikedUseCase is called`() {
        every {
            stubDeleteAllUnlikedUseCase()
        } returns Completable.complete()

        viewModel.processIntent(GalleryIntent.Delete.AllUnliked.Confirm)

        verify {
            stubDeleteAllUnlikedUseCase()
        }
    }

    @Test
    fun `given received LikeSelection intent with selection, expected likeItemsUseCase is called`() {
        every {
            stubLikeItemsUseCase(any())
        } returns Completable.complete()

        every {
            stubGalleryItemStateEvent.emitLikedChange(any(), any())
        } returns Unit

        // Set up selection
        viewModel.processIntent(GalleryIntent.ChangeSelectionMode(true))
        viewModel.processIntent(GalleryIntent.ToggleItemSelection(1L))
        viewModel.processIntent(GalleryIntent.ToggleItemSelection(2L))

        viewModel.processIntent(GalleryIntent.LikeSelection)

        verify {
            stubLikeItemsUseCase(listOf(1L, 2L))
        }
    }

    @Test
    fun `given received HideSelection intent with selection, expected hideItemsUseCase is called`() {
        every {
            stubHideItemsUseCase(any())
        } returns Completable.complete()

        every {
            stubGalleryItemStateEvent.emitHiddenChange(any(), any())
        } returns Unit

        // Set up selection
        viewModel.processIntent(GalleryIntent.ChangeSelectionMode(true))
        viewModel.processIntent(GalleryIntent.ToggleItemSelection(1L))
        viewModel.processIntent(GalleryIntent.ToggleItemSelection(2L))

        viewModel.processIntent(GalleryIntent.HideSelection)

        verify {
            stubHideItemsUseCase(listOf(1L, 2L))
        }
    }
}
