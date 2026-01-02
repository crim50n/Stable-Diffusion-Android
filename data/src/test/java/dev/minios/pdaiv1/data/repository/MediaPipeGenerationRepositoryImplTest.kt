package dev.minios.pdaiv1.data.repository

import android.graphics.Bitmap
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.mediapipe.MediaPipe
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MediaPipeGenerationRepositoryImplTest {

    private val stubBitmap = mockk<Bitmap>()
    private val stubException = Throwable("Something went wrong.")
    private val stubMediaStoreGateway = mockk<MediaStoreGateway>()
    private val stubBase64ToBitmapConverter = mockk<Base64ToBitmapConverter>()
    private val stubBitmapToBase64Converter = mockk<BitmapToBase64Converter>()
    private val stubLocalDataSource = mockk<GenerationResultDataSource.Local>()
    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubMediaPipe = mockk<MediaPipe>()
    private val stubBackgroundWorkObserver = mockk<BackgroundWorkObserver>()
    private val stubMediaFileManager = mockk<MediaFileManager>()

    private val stubSchedulersProvider = object : SchedulersProvider {
        override val io: Scheduler = Schedulers.trampoline()
        override val ui: Scheduler = Schedulers.trampoline()
        override val computation: Scheduler = Schedulers.trampoline()
        override val singleThread: Executor = Executors.newSingleThreadExecutor()
    }

    private val repository = MediaPipeGenerationRepositoryImpl(
        mediaStoreGateway = stubMediaStoreGateway,
        base64ToBitmapConverter = stubBase64ToBitmapConverter,
        localDataSource = stubLocalDataSource,
        preferenceManager = stubPreferenceManager,
        backgroundWorkObserver = stubBackgroundWorkObserver,
        mediaFileManager = stubMediaFileManager,
        schedulersProvider = stubSchedulersProvider,
        mediaPipe = stubMediaPipe,
        bitmapToBase64Converter = stubBitmapToBase64Converter,
    )

    @Before
    fun initialize() {
        every {
            stubBackgroundWorkObserver.hasActiveTasks()
        } returns false

        every {
            stubPreferenceManager.autoSaveAiResults
        } returns false
    }

    @Test
    fun `given MediaPipe process fails, expected error value`() {
        val payload = mockk<dev.minios.pdaiv1.domain.entity.TextToImagePayload>(relaxed = true)

        every {
            stubMediaPipe.process(any())
        } returns Single.error(stubException)

        repository
            .generateFromText(payload)
            .test()
            .await()
            .assertError(stubException)
            .assertNoValues()
            .assertNotComplete()

        verify { stubMediaPipe.process(any()) }
    }

    @Test
    fun `given MediaPipe process success but bitmap conversion fails, expected error value`() {
        val payload = mockk<dev.minios.pdaiv1.domain.entity.TextToImagePayload>(relaxed = true)

        every {
            stubMediaPipe.process(any())
        } returns Single.just(stubBitmap)

        every {
            stubBitmapToBase64Converter(any())
        } returns Single.error(stubException)

        repository
            .generateFromText(payload)
            .test()
            .await()
            .assertError(stubException)
            .assertNoValues()
            .assertNotComplete()
    }
}
