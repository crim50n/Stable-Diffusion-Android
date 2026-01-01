package dev.minios.pdaiv1.data.repository

import dev.minios.pdaiv1.data.mocks.mockAiGenerationResult
import dev.minios.pdaiv1.data.mocks.mockAiGenerationResults
import dev.minios.pdaiv1.data.mocks.mockTextToImagePayload
import dev.minios.pdaiv1.domain.datasource.FalAiGenerationDataSource
import dev.minios.pdaiv1.domain.entity.FalAiEndpoint
import dev.minios.pdaiv1.domain.entity.FalAiEndpointCategory
import dev.minios.pdaiv1.domain.entity.FalAiEndpointSchema
import dev.minios.pdaiv1.domain.repository.FalAiEndpointRepository
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class FalAiGenerationRepositoryImplTest {

    private val stubException = Throwable("Something went wrong.")
    private val stubRemoteDataSource = mockk<FalAiGenerationDataSource.Remote>()
    private val stubEndpointRepository = mockk<FalAiEndpointRepository>()

    private val stubEndpoint = FalAiEndpoint(
        id = "fal-ai/flux/schnell",
        endpointId = "fal-ai/flux/schnell",
        title = "FLUX.1 [schnell]",
        description = "Fast generation",
        category = FalAiEndpointCategory.TEXT_TO_IMAGE,
        group = "FLUX",
        thumbnailUrl = "",
        playgroundUrl = "",
        documentationUrl = "",
        isCustom = false,
        schema = FalAiEndpointSchema(
            baseUrl = "https://queue.fal.run",
            submissionPath = "/fal-ai/flux/schnell",
            inputProperties = emptyList(),
            requiredProperties = emptyList(),
            propertyOrder = emptyList(),
        ),
    )

    private val repository = FalAiGenerationRepositoryImpl(
        remoteDataSource = stubRemoteDataSource,
        endpointRepository = stubEndpointRepository,
    )

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
    fun `given attempt to generate from text, endpoint selected and remote returns data, expected valid result`() {
        every {
            stubEndpointRepository.getSelected()
        } returns Single.just(stubEndpoint)

        every {
            stubRemoteDataSource.textToImage(any(), any())
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
    fun `given attempt to generate from text, endpoint selection fails, expected error value`() {
        every {
            stubEndpointRepository.getSelected()
        } returns Single.error(stubException)

        repository
            .generateFromText(mockTextToImagePayload)
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to generate from text, remote generation fails, expected error value`() {
        every {
            stubEndpointRepository.getSelected()
        } returns Single.just(stubEndpoint)

        every {
            stubRemoteDataSource.textToImage(any(), any())
        } returns Single.error(stubException)

        repository
            .generateFromText(mockTextToImagePayload)
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given attempt to generate dynamic, remote returns data, expected valid results list`() {
        val stubParameters = mapOf<String, Any?>("prompt" to "test")

        every {
            stubRemoteDataSource.generateDynamic(any(), any())
        } returns Single.just(mockAiGenerationResults)

        repository
            .generateDynamic(stubEndpoint, stubParameters)
            .test()
            .assertNoErrors()
            .assertValue(mockAiGenerationResults)
            .await()
            .assertComplete()
    }

    @Test
    fun `given attempt to generate dynamic, remote throws exception, expected error value`() {
        val stubParameters = mapOf<String, Any?>("prompt" to "test")

        every {
            stubRemoteDataSource.generateDynamic(any(), any())
        } returns Single.error(stubException)

        repository
            .generateDynamic(stubEndpoint, stubParameters)
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }
}
