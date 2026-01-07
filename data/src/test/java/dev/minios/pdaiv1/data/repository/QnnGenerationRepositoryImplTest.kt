package dev.minios.pdaiv1.data.repository

import android.graphics.Bitmap
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashEncoder
import dev.minios.pdaiv1.data.mocks.mockImageToImagePayload
import dev.minios.pdaiv1.data.mocks.mockTextToImagePayload
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.LocalDiffusionStatus
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.qnn.LocalQnn
import dev.minios.pdaiv1.domain.feature.qnn.QnnGenerationResult
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test

class QnnGenerationRepositoryImplTest {

    private val stubBitmap = mockk<Bitmap>()
    private val stubQnnGenerationResult = QnnGenerationResult(
        bitmap = stubBitmap,
        seed = 12345L,
        width = 512,
        height = 512,
    )
    private val stubException = Throwable("Something went wrong.")
    private val stubStatus = BehaviorSubject.create<LocalDiffusionStatus>()
    private val stubMediaStoreGateway = mockk<MediaStoreGateway>()
    private val stubBase64ToBitmapConverter = mockk<Base64ToBitmapConverter>()
    private val stubBitmapToBase64Converter = mockk<BitmapToBase64Converter>()
    private val stubLocalDataSource = mockk<GenerationResultDataSource.Local>()
    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubLocalQnn = mockk<LocalQnn>()
    private val stubBackgroundWorkObserver = mockk<BackgroundWorkObserver>()
    private val stubMediaFileManager = mockk<MediaFileManager>()
    private val stubSchedulersProvider = mockk<SchedulersProvider>()
    private val stubBlurHashEncoder = mockk<BlurHashEncoder>()

    private val repository = QnnGenerationRepositoryImpl(
        mediaStoreGateway = stubMediaStoreGateway,
        base64ToBitmapConverter = stubBase64ToBitmapConverter,
        localDataSource = stubLocalDataSource,
        backgroundWorkObserver = stubBackgroundWorkObserver,
        mediaFileManager = stubMediaFileManager,
        blurHashEncoder = stubBlurHashEncoder,
        preferenceManager = stubPreferenceManager,
        localQnn = stubLocalQnn,
        bitmapToBase64Converter = stubBitmapToBase64Converter,
        schedulersProvider = stubSchedulersProvider,
    )

    @Before
    fun initialize() {
        every {
            stubBackgroundWorkObserver.hasActiveTasks()
        } returns false

        every {
            stubLocalQnn.observeStatus()
        } returns stubStatus

        every {
            stubPreferenceManager.autoSaveAiResults
        } returns false

        every {
            stubSchedulersProvider.io
        } returns Schedulers.trampoline()
    }

    @Test
    fun `given attempt to observe status, local emits two values, expected same values with same order`() {
        val stubObserver = repository.observeStatus().test()

        stubStatus.onNext(LocalDiffusionStatus(1, 2))

        stubObserver
            .assertNoErrors()
            .assertValueAt(0, LocalDiffusionStatus(1, 2))

        stubStatus.onNext(LocalDiffusionStatus(2, 2))

        stubObserver
            .assertNoErrors()
            .assertValueAt(1, LocalDiffusionStatus(2, 2))
    }

    @Test
    fun `given attempt to observe status, local throws exception, expected error value`() {
        every {
            stubLocalQnn.observeStatus()
        } returns Observable.error(stubException)

        repository
            .observeStatus()
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to interrupt generation, local completes, expected complete value`() {
        every {
            stubLocalQnn.interrupt()
        } returns Completable.complete()

        repository
            .interruptGeneration()
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to interrupt generation, local throws exception, expected error value`() {
        every {
            stubLocalQnn.interrupt()
        } returns Completable.error(stubException)

        repository
            .interruptGeneration()
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to generate from text, local returns bitmap, expected ai generation result`() {
        val mockBase64Output = BitmapToBase64Converter.Output("base64image")

        every {
            stubLocalQnn.processTextToImage(any())
        } returns Single.just(stubQnnGenerationResult)

        every {
            stubBitmapToBase64Converter.invoke(any())
        } returns Single.just(mockBase64Output)

        every {
            stubLocalDataSource.insert(any<AiGenerationResult>())
        } returns Single.just(1L)

        repository
            .generateFromText(mockTextToImagePayload)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to generate from text, local throws exception, expected error value`() {
        every {
            stubLocalQnn.processTextToImage(any())
        } returns Single.error(stubException)

        repository
            .generateFromText(mockTextToImagePayload)
            .test()
            .assertError(stubException)
            .assertNoValues()
    }

    @Test
    fun `given attempt to generate from image, local returns bitmap, expected ai generation result`() {
        val mockBase64Output = BitmapToBase64Converter.Output("base64image")

        every {
            stubLocalQnn.processImageToImage(any())
        } returns Single.just(stubQnnGenerationResult)

        every {
            stubBitmapToBase64Converter.invoke(any())
        } returns Single.just(mockBase64Output)

        every {
            stubLocalDataSource.insert(any<AiGenerationResult>())
        } returns Single.just(1L)

        repository
            .generateFromImage(mockImageToImagePayload)
            .test()
            .assertNoErrors()
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to generate from image, local throws exception, expected error value`() {
        every {
            stubLocalQnn.processImageToImage(any())
        } returns Single.error(stubException)

        repository
            .generateFromImage(mockImageToImagePayload)
            .test()
            .assertError(stubException)
            .assertNoValues()
    }
}
