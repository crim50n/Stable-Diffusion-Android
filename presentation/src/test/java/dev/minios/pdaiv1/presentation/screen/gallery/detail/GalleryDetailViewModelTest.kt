@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.minios.pdaiv1.presentation.screen.gallery.detail

import android.graphics.Bitmap
import app.cash.turbine.test
import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.DeleteGalleryItemUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.GetGalleryPagedIdsUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.ToggleImageVisibilityUseCase
import dev.minios.pdaiv1.domain.usecase.gallery.ToggleLikeUseCase
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultUseCase
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.presentation.core.CoreViewModelTest
import dev.minios.pdaiv1.presentation.core.GalleryItemStateEvent
import dev.minios.pdaiv1.presentation.core.GenerationFormUpdateEvent
import dev.minios.pdaiv1.presentation.extensions.mapToUi
import dev.minios.pdaiv1.presentation.mocks.mockAiGenerationResult
import dev.minios.pdaiv1.presentation.model.Modal
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.stub.stubDispatchersProvider
import dev.minios.pdaiv1.presentation.stub.stubSchedulersProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File

@Ignore("ToDo: Investigate why sometimes tests fail on remote worker due to race-conditions.")
class GalleryDetailViewModelTest : CoreViewModelTest<GalleryDetailViewModel>() {

    private val stubBitmap = mockk<Bitmap>()
    private val stubFile = mockk<File>()
    private val stubGetGenerationResultUseCase = mockk<GetGenerationResultUseCase>()
    private val stubGetLastResultFromCacheUseCase = mockk<GetLastResultFromCacheUseCase>()
    private val stubDeleteGalleryItemUseCase = mockk<DeleteGalleryItemUseCase>()
    private val stubToggleImageVisibilityUseCase = mockk<ToggleImageVisibilityUseCase>()
    private val stubToggleLikeUseCase = mockk<ToggleLikeUseCase>()
    private val stubGalleryDetailBitmapExporter = mockk<GalleryDetailBitmapExporter>()
    private val stubBase64ToBitmapConverter = mockk<Base64ToBitmapConverter>()
    private val stubGenerationFormUpdateEvent = mockk<GenerationFormUpdateEvent>()
    private val stubGalleryItemStateEvent = mockk<GalleryItemStateEvent>()
    private val stubMainRouter = mockk<MainRouter>()
    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubGetGalleryPagedIdsUseCase = mockk<GetGalleryPagedIdsUseCase>()
    private val stubMediaStoreGateway = mockk<MediaStoreGateway>()
    private val stubBackgroundWorkObserver = mockk<BackgroundWorkObserver>()

    override fun initializeViewModel() = GalleryDetailViewModel(
        itemId = 5598L,
        dispatchersProvider = stubDispatchersProvider,
        buildInfoProvider = BuildInfoProvider.stub,
        getGenerationResultUseCase = stubGetGenerationResultUseCase,
        getLastResultFromCacheUseCase = stubGetLastResultFromCacheUseCase,
        deleteGalleryItemUseCase = stubDeleteGalleryItemUseCase,
        toggleImageVisibilityUseCase = stubToggleImageVisibilityUseCase,
        toggleLikeUseCase = stubToggleLikeUseCase,
        galleryDetailBitmapExporter = stubGalleryDetailBitmapExporter,
        base64ToBitmapConverter = stubBase64ToBitmapConverter,
        schedulersProvider = stubSchedulersProvider,
        generationFormUpdateEvent = stubGenerationFormUpdateEvent,
        galleryItemStateEvent = stubGalleryItemStateEvent,
        mainRouter = stubMainRouter,
        preferenceManager = stubPreferenceManager,
        getGalleryPagedIdsUseCase = stubGetGalleryPagedIdsUseCase,
        mediaStoreGateway = stubMediaStoreGateway,
        backgroundWorkObserver = stubBackgroundWorkObserver,
    )

    @Before
    override fun initialize() {
        super.initialize()

        every {
            stubGetLastResultFromCacheUseCase()
        } returns Single.just(mockAiGenerationResult)

        every {
            stubGetGenerationResultUseCase(any())
        } returns Single.just(mockAiGenerationResult)

        every {
            stubBase64ToBitmapConverter(any())
        } returns Single.just(Base64ToBitmapConverter.Output(stubBitmap))

        every {
            stubBackgroundWorkObserver.observeGalleryChanged()
        } returns Flowable.empty()
    }

    @Test
    fun `initialized, loaded ai generation result, expected UI state is Content`() {
        val expected = GalleryDetailState.Content(
            tabs = GalleryDetailState.Tab.consume(mockAiGenerationResult.type),
            generationType = mockAiGenerationResult.type,
            id = mockAiGenerationResult.id,
            bitmap = stubBitmap,
            inputBitmap = stubBitmap,
            createdAt = mockAiGenerationResult.createdAt.toString().asUiText(),
            type = mockAiGenerationResult.type.key.asUiText(),
            prompt = mockAiGenerationResult.prompt.asUiText(),
            negativePrompt = mockAiGenerationResult.negativePrompt.asUiText(),
            size = "512 X 512".asUiText(),
            samplingSteps = mockAiGenerationResult.samplingSteps.toString().asUiText(),
            cfgScale = mockAiGenerationResult.cfgScale.toString().asUiText(),
            restoreFaces = mockAiGenerationResult.restoreFaces.mapToUi(),
            sampler = mockAiGenerationResult.sampler.asUiText(),
            seed = mockAiGenerationResult.seed.asUiText(),
            subSeed = mockAiGenerationResult.subSeed.asUiText(),
            subSeedStrength = mockAiGenerationResult.subSeedStrength.toString().asUiText(),
            denoisingStrength = mockAiGenerationResult.denoisingStrength.toString().asUiText(),
            hidden = false,
        )
        val actual = viewModel.state.value
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `given received CopyToClipboard intent, expected ShareClipBoard effect delivered to effect collector`() {
        viewModel.processIntent(GalleryDetailIntent.CopyToClipboard("text"))
        runTest {
            advanceUntilIdle()
            val expected = GalleryDetailEffect.ShareClipBoard("text")
            val actual = viewModel.effect.firstOrNull()
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received Delete Request intent, expected modal field in UI state is DeleteImageConfirm`() {
        viewModel.processIntent(GalleryDetailIntent.Delete.Request)
        val expected = Modal.DeleteImageConfirm(isAll = false, isMultiple = false)
        val actual = (viewModel.state.value as? GalleryDetailState.Content)?.screenModal
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `given received Delete Confirm intent, expected screenModal field in UI state is None, deleteGalleryItemUseCase() method is called`() {
        every {
            stubDeleteGalleryItemUseCase(any())
        } returns Completable.complete()

        every {
            stubMainRouter.navigateBack()
        } returns Unit

        viewModel.processIntent(GalleryDetailIntent.Delete.Confirm)

        val expected = Modal.None
        val actual = (viewModel.state.value as? GalleryDetailState.Content)?.screenModal
        Assert.assertEquals(expected, actual)

        verify {
            stubDeleteGalleryItemUseCase(5598L)
        }
    }

    @Test
    fun `given received Export Image intent, expected galleryDetailBitmapExporter() method is called, ShareImageFile effect delivered to effect collector`() {
        every {
            stubGalleryDetailBitmapExporter(any())
        } returns Single.just(stubFile)

        viewModel.processIntent(GalleryDetailIntent.Export.Image)

        runTest {
            advanceUntilIdle()
            viewModel.effect.test {
                Assert.assertEquals(GalleryDetailEffect.ShareImageFile(stubFile), awaitItem())
            }
        }
        verify {
            stubGalleryDetailBitmapExporter(stubBitmap)
        }
    }

    @Test
    fun `given received Export Params intent, expected ShareGenerationParams effect delivered to effect collector`() {
        viewModel.processIntent(GalleryDetailIntent.Export.Params)
        runTest {
            advanceUntilIdle()
            val expected = GalleryDetailEffect.ShareGenerationParams(viewModel.state.value)
            val actual = viewModel.effect.firstOrNull()
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received NavigateBack intent, expected router navigateBack() method called`() {
        every {
            stubMainRouter.navigateBack()
        } returns Unit
        viewModel.processIntent(GalleryDetailIntent.NavigateBack)
        verify {
            stubMainRouter.navigateBack()
        }
    }

    @Test
    fun `given received SelectTab intent with IMAGE tab, expected expected selectedTab field in UI state is IMAGE`() {
        viewModel.processIntent(GalleryDetailIntent.SelectTab(GalleryDetailState.Tab.IMAGE))
        val expected = GalleryDetailState.Tab.IMAGE
        val actual = viewModel.state.value.selectedTab
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `given received SelectTab intent with INFO tab, expected expected selectedTab field in UI state is INFO`() {
        viewModel.processIntent(GalleryDetailIntent.SelectTab(GalleryDetailState.Tab.INFO))
        val expected = GalleryDetailState.Tab.INFO
        val actual = viewModel.state.value.selectedTab
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `given received SelectTab intent with ORIGINAL tab, expected expected selectedTab field in UI state is ORIGINAL`() {
        viewModel.processIntent(GalleryDetailIntent.SelectTab(GalleryDetailState.Tab.ORIGINAL))
        val expected = GalleryDetailState.Tab.ORIGINAL
        val actual = viewModel.state.value.selectedTab
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `given received SendTo Txt2Img intent, expected router navigateBack() and form event update() methods called`() {
        every {
            stubGenerationFormUpdateEvent.update(any(), any(), any())
        } returns Unit

        every {
            stubMainRouter.navigateBack()
        } returns Unit

        viewModel.processIntent(GalleryDetailIntent.SendTo.Txt2Img)

        verify {
            stubMainRouter.navigateBack()
        }
        verify {
            stubGenerationFormUpdateEvent.update(
                generation = mockAiGenerationResult,
                route = AiGenerationResult.Type.TEXT_TO_IMAGE,
                inputImage = false,
            )
        }
    }

    @Test
    fun `given received SendTo Img2Img intent, expected router navigateBack() and form event update() methods called`() {
        every {
            stubGenerationFormUpdateEvent.update(any(), any(), any())
        } returns Unit

        every {
            stubMainRouter.navigateBack()
        } returns Unit

        viewModel.processIntent(GalleryDetailIntent.SendTo.Img2Img)

        verify {
            stubMainRouter.navigateBack()
        }
        verify {
            stubGenerationFormUpdateEvent.update(
                generation = mockAiGenerationResult,
                route = AiGenerationResult.Type.IMAGE_TO_IMAGE,
                inputImage = false,
            )
        }
    }

    @Test
    fun `given received DismissDialog intent, expected screenModal field in UI state is None`() {
        viewModel.processIntent(GalleryDetailIntent.DismissDialog)
        val expected = Modal.None
        val actual = viewModel.state.value.screenModal
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `given received ToggleVisibility intent, expected hidden state updated and event emitted`() {
        every {
            stubToggleImageVisibilityUseCase(any())
        } returns Single.just(true)

        every {
            stubGalleryItemStateEvent.emitHiddenChange(any(), any())
        } returns Unit

        viewModel.processIntent(GalleryDetailIntent.ToggleVisibility)

        val expected = true
        val actual = (viewModel.state.value as? GalleryDetailState.Content)?.hidden
        Assert.assertEquals(expected, actual)

        verify {
            stubGalleryItemStateEvent.emitHiddenChange(5598L, true)
        }
    }

    @Test
    fun `given received ToggleLike intent, expected liked state updated and event emitted`() {
        every {
            stubToggleLikeUseCase(any())
        } returns Single.just(true)

        every {
            stubGalleryItemStateEvent.emitLikedChange(any(), any())
        } returns Unit

        viewModel.processIntent(GalleryDetailIntent.ToggleLike)

        val expected = true
        val actual = (viewModel.state.value as? GalleryDetailState.Content)?.liked
        Assert.assertEquals(expected, actual)

        verify {
            stubGalleryItemStateEvent.emitLikedChange(5598L, true)
        }
    }
}
