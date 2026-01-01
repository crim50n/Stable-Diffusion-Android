package dev.minios.pdaiv1.domain.usecase.generation

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.mocks.mockAiGenerationResult
import dev.minios.pdaiv1.domain.mocks.mockFalAiEndpoint
import dev.minios.pdaiv1.domain.mocks.mockFalAiPayload
import dev.minios.pdaiv1.domain.repository.FalAiEndpointRepository
import dev.minios.pdaiv1.domain.repository.FalAiGenerationRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class FalAiGenerationUseCaseImplTest {

    private val stubException = Throwable("Something went wrong.")
    private val stubFalAiEndpointRepository = mock<FalAiEndpointRepository>()
    private val stubFalAiGenerationRepository = mock<FalAiGenerationRepository>()
    private val stubSaveGenerationResultUseCase = mock<SaveGenerationResultUseCase>()

    private val useCase = FalAiGenerationUseCaseImpl(
        falAiEndpointRepository = stubFalAiEndpointRepository,
        falAiGenerationRepository = stubFalAiGenerationRepository,
        saveGenerationResultUseCase = stubSaveGenerationResultUseCase,
    )

    @Test
    fun `given valid payload and endpoint found, generation succeeds, expected valid results list`() {
        val expectedResults = listOf(mockAiGenerationResult)

        whenever(stubFalAiEndpointRepository.getAll())
            .thenReturn(Single.just(listOf(mockFalAiEndpoint)))

        whenever(stubFalAiGenerationRepository.generateDynamic(any(), any()))
            .thenReturn(Single.just(expectedResults))

        whenever(stubSaveGenerationResultUseCase.invoke(any()))
            .thenReturn(Completable.complete())

        useCase(mockFalAiPayload)
            .test()
            .assertNoErrors()
            .assertValue(expectedResults)
            .await()
            .assertComplete()
    }

    @Test
    fun `given valid payload but endpoint not found, expected error value`() {
        val payloadWithWrongEndpoint = mockFalAiPayload.copy(endpointId = "non-existent")

        whenever(stubFalAiEndpointRepository.getAll())
            .thenReturn(Single.just(listOf(mockFalAiEndpoint)))

        useCase(payloadWithWrongEndpoint)
            .test()
            .assertError { it.message?.contains("Endpoint not found") == true }
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given valid payload, endpoint repository fails, expected error value`() {
        whenever(stubFalAiEndpointRepository.getAll())
            .thenReturn(Single.error(stubException))

        useCase(mockFalAiPayload)
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given valid payload and endpoint found, generation fails, expected error value`() {
        whenever(stubFalAiEndpointRepository.getAll())
            .thenReturn(Single.just(listOf(mockFalAiEndpoint)))

        whenever(stubFalAiGenerationRepository.generateDynamic(any(), any()))
            .thenReturn(Single.error(stubException))

        useCase(mockFalAiPayload)
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given valid payload and endpoint found, generation succeeds but save fails, expected error value`() {
        val expectedResults = listOf(mockAiGenerationResult)

        whenever(stubFalAiEndpointRepository.getAll())
            .thenReturn(Single.just(listOf(mockFalAiEndpoint)))

        whenever(stubFalAiGenerationRepository.generateDynamic(any(), any()))
            .thenReturn(Single.just(expectedResults))

        whenever(stubSaveGenerationResultUseCase.invoke(any()))
            .thenReturn(Completable.error(stubException))

        useCase(mockFalAiPayload)
            .test()
            .assertError(stubException)
            .assertNoValues()
            .await()
            .assertNotComplete()
    }
}
