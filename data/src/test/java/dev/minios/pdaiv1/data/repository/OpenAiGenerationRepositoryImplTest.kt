package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.data.mocks.mockAiGenerationResult
import dev.minios.pdaiv1.data.mocks.mockTextToImagePayload
import dev.minios.pdaiv1.domain.datasource.GenerationResultDataSource
import dev.minios.pdaiv1.domain.datasource.OpenAiGenerationDataSource
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.gateway.MediaStoreGateway
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.junit.Before
import org.junit.Test

class OpenAiGenerationRepositoryImplTest {

    private val stubException = Throwable("Something went wrong.")
    private val stubMediaStoreGateway = mockk<MediaStoreGateway>()
    private val stubBase64ToBitmapConverter = mockk<Base64ToBitmapConverter>()
    private val stubLocalDataSource = mockk<GenerationResultDataSource.Local>()
    private val stubPreferenceManager = mockk<PreferenceManager>()
    private val stubRemoteDataSource = mockk<OpenAiGenerationDataSource.Remote>()
    private val stubBackgroundWorkObserver = mockk<BackgroundWorkObserver>()
    private val stubMediaFileManager = mockk<MediaFileManager>()

    private val repository = OpenAiGenerationRepositoryImpl(
        mediaStoreGateway = stubMediaStoreGateway,
        base64ToBitmapConverter = stubBase64ToBitmapConverter,
        localDataSource = stubLocalDataSource,
        preferenceManager = stubPreferenceManager,
        backgroundWorkObserver = stubBackgroundWorkObserver,
        mediaFileManager = stubMediaFileManager,
        remoteDataSource = stubRemoteDataSource,
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
    fun `given attempt to validate api key, remote returns true, expected true value`() {
        every {
            stubRemoteDataSource.validateApiKey()
        } returns Single.just(true)

        repository
            .validateApiKey()
            .test()
            .assertNoErrors()
            .assertValue(true)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to validate api key, remote returns false, expected false value`() {
        every {
            stubRemoteDataSource.validateApiKey()
        } returns Single.just(false)

        repository
            .validateApiKey()
            .test()
            .assertNoErrors()
            .assertValue(false)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to validate api key, remote throws exception, expected error value`() {
        every {
            stubRemoteDataSource.validateApiKey()
        } returns Single.error(stubException)

        repository
            .validateApiKey()
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to generate from text, remote returns result, expected valid domain model value`() {
        every {
            stubRemoteDataSource.textToImage(any())
        } returns Single.just(mockAiGenerationResult)

        repository
            .generateFromText(mockTextToImagePayload)
            .test()
            .assertNoErrors()
            .assertValue(mockAiGenerationResult)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to generate from text, remote throws exception, expected error value`() {
        every {
            stubRemoteDataSource.textToImage(any())
        } returns Single.error(stubException)

        repository
            .generateFromText(mockTextToImagePayload)
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }
}
