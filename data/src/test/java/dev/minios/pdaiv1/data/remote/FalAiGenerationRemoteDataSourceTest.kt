package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.network.api.falai.FalAiApi
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import org.junit.Test

class FalAiGenerationRemoteDataSourceTest {

    private val stubException = Throwable("API error")
    private val stubApi = mockk<FalAiApi>()
    private val stubHttpClient = mockk<OkHttpClient>()
    private val stubMediaFileManager = mockk<MediaFileManager>()

    private val remoteDataSource = FalAiGenerationRemoteDataSource(
        api = stubApi,
        httpClient = stubHttpClient,
        mediaFileManager = stubMediaFileManager,
    )

    @Test
    fun `given attempt to validate API key, api returns success, expected true value`() {
        val mockResponse = mockk<Any>()

        every {
            stubApi.listModels(limit = 1)
        } returns Single.just(mockResponse)

        remoteDataSource
            .validateApiKey()
            .test()
            .assertNoErrors()
            .assertValue(true)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to validate API key, api returns error, expected false value`() {
        every {
            stubApi.listModels(limit = 1)
        } returns Single.error(stubException)

        remoteDataSource
            .validateApiKey()
            .test()
            .assertNoErrors()
            .assertValue(false)
            .await()
            .assertComplete()
    }
}
