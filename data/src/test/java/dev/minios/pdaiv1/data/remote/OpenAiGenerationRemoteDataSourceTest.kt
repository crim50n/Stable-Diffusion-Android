package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.data.mocks.mockBadOpenAiResponse
import dev.minios.pdaiv1.data.mocks.mockSuccessOpenAiResponse
import dev.minios.pdaiv1.data.mocks.mockTextToImagePayload
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.network.api.openai.OpenAiApi
import dev.minios.pdaiv1.network.response.OpenAiResponse
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class OpenAiGenerationRemoteDataSourceTest {

    private val stubApiException = Throwable("Internal server error.")
    private val stubApi = mockk<OpenAiApi>()

    private val remoteDataSource = OpenAiGenerationRemoteDataSource(stubApi)

    @Test
    fun `given attempt to validate bearer token, api returns success response, expected true`() {
        every {
            stubApi.validateBearerToken()
        } returns Completable.complete()

        remoteDataSource
            .validateApiKey()
            .test()
            .assertNoErrors()
            .assertValue(true)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to validate bearer token, api returns error response, expected false`() {
        every {
            stubApi.validateBearerToken()
        } returns Completable.error(Throwable("Bad api key."))

        remoteDataSource
            .validateApiKey()
            .test()
            .assertNoErrors()
            .assertValue(false)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to generate txt2img, api returns result, expected valid ai generation result value`() {
        every {
            stubApi.generateImage(any())
        } returns Single.just(mockSuccessOpenAiResponse)

        remoteDataSource
            .textToImage(mockTextToImagePayload)
            .test()
            .assertNoErrors()
            .assertValue { it is AiGenerationResult }
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to generate txt2img, api returns empty result, expected error value`() {
        every {
            stubApi.generateImage(any())
        } returns Single.just(mockBadOpenAiResponse)

        remoteDataSource
            .textToImage(mockTextToImagePayload)
            .test()
            .assertError { t ->
                t is IllegalStateException && t.message == "Got null data object from API."
            }
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to generate txt2img, api request fails, expected error value`() {
        every {
            stubApi.generateImage(any())
        } returns Single.error(stubApiException)

        remoteDataSource
            .textToImage(mockTextToImagePayload)
            .test()
            .assertError(stubApiException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }
}
