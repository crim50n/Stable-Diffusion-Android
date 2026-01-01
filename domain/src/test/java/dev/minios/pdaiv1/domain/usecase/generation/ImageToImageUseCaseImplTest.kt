package dev.minios.pdaiv1.domain.usecase.generation

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.mocks.mockAiGenerationResult
import dev.minios.pdaiv1.domain.mocks.mockImageToImagePayload
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.HordeGenerationRepository
import dev.minios.pdaiv1.domain.repository.HuggingFaceGenerationRepository
import dev.minios.pdaiv1.domain.repository.StabilityAiGenerationRepository
import dev.minios.pdaiv1.domain.repository.QnnGenerationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionGenerationRepository
import dev.minios.pdaiv1.domain.repository.SwarmUiGenerationRepository
import io.reactivex.rxjava3.core.Single
import org.junit.Test

class ImageToImageUseCaseImplTest {

    private val stubException = Throwable("Unable to generate image.")
    private val stubStableDiffusionGenerationRepository = mock<StableDiffusionGenerationRepository>()
    private val stubSwarmUiGenerationRepository = mock<SwarmUiGenerationRepository>()
    private val stubHordeGenerationRepository = mock<HordeGenerationRepository>()
    private val stubHuggingFaceGenerationRepository = mock<HuggingFaceGenerationRepository>()
    private val stubStabilityAiGenerationRepository = mock<StabilityAiGenerationRepository>()
    private val stubQnnGenerationRepository = mock<QnnGenerationRepository>()
    private val stubPreferenceManager = mock<PreferenceManager>()

    private val useCase = ImageToImageUseCaseImpl(
        stableDiffusionGenerationRepository = stubStableDiffusionGenerationRepository,
        swarmUiGenerationRepository = stubSwarmUiGenerationRepository,
        hordeGenerationRepository = stubHordeGenerationRepository,
        huggingFaceGenerationRepository = stubHuggingFaceGenerationRepository,
        stabilityAiGenerationRepository = stubStabilityAiGenerationRepository,
        qnnGenerationRepository = stubQnnGenerationRepository,
        preferenceManager = stubPreferenceManager,
    )

    @Test
    fun `given source is AUTOMATIC1111, batch count is 1, generated successfully, expected generations list with size 1`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubStableDiffusionGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.just(mockAiGenerationResult))

        val stubBatchCount = 1
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        val expectedResult = listOf(mockAiGenerationResult)

        useCase(stubPayload)
            .test()
            .assertNoErrors()
            .assertValue { generations ->
                generations.size == stubBatchCount && expectedResult == generations
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is AUTOMATIC1111, batch count is 10, generated successfully, expected generations list with size 10`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubStableDiffusionGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.just(mockAiGenerationResult))

        val stubBatchCount = 10
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        val expectedResult = (0 until 10).map { mockAiGenerationResult }

        useCase(stubPayload)
            .test()
            .assertNoErrors()
            .assertValue { generations ->
                generations.size == stubBatchCount && expectedResult == generations
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is AUTOMATIC1111, batch count is 1, generate failed, expected error`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.AUTOMATIC1111)

        whenever(stubStableDiffusionGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.error(stubException))

        val stubBatchCount = 1
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        useCase(stubPayload)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given source is HORDE, batch count is 1, generated successfully, expected generations list with size 1`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.HORDE)

        whenever(stubHordeGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.just(mockAiGenerationResult))

        val stubBatchCount = 1
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        val expectedResult = listOf(mockAiGenerationResult)

        useCase(stubPayload)
            .test()
            .assertNoErrors()
            .assertValue { generations ->
                generations.size == stubBatchCount && expectedResult == generations
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is HORDE, batch count is 10, generated successfully, expected generations list with size 10`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.HORDE)

        whenever(stubHordeGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.just(mockAiGenerationResult))

        val stubBatchCount = 10
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        val expectedResult = (0 until 10).map { mockAiGenerationResult }

        useCase(stubPayload)
            .test()
            .assertNoErrors()
            .assertValue { generations ->
                generations.size == stubBatchCount && expectedResult == generations
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is HORDE, batch count is 1, generate failed, expected error`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.HORDE)

        whenever(stubHordeGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.error(stubException))

        val stubBatchCount = 1
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        useCase(stubPayload)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given source is HUGGING_FACE, batch count is 1, generated successfully, expected generations list with size 1`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.HUGGING_FACE)

        whenever(stubHuggingFaceGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.just(mockAiGenerationResult))

        val stubBatchCount = 1
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        val expectedResult = listOf(mockAiGenerationResult)

        useCase(stubPayload)
            .test()
            .assertNoErrors()
            .assertValue { generations ->
                generations.size == stubBatchCount && expectedResult == generations
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is HUGGING_FACE, batch count is 10, generated successfully, expected generations list with size 10`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.HUGGING_FACE)

        whenever(stubHuggingFaceGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.just(mockAiGenerationResult))

        val stubBatchCount = 10
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        val expectedResult = (0 until 10).map { mockAiGenerationResult }

        useCase(stubPayload)
            .test()
            .assertNoErrors()
            .assertValue { generations ->
                generations.size == stubBatchCount && expectedResult == generations
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is HUGGING_FACE, batch count is 1, generate failed, expected error`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.HUGGING_FACE)

        whenever(stubHuggingFaceGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.error(stubException))

        val stubBatchCount = 1
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        useCase(stubPayload)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given source is STABILITY_AI, batch count is 1, generated successfully, expected generations list with size 1`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.STABILITY_AI)

        whenever(stubStabilityAiGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.just(mockAiGenerationResult))

        val stubBatchCount = 1
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        val expectedResult = listOf(mockAiGenerationResult)

        useCase(stubPayload)
            .test()
            .assertNoErrors()
            .assertValue { generations ->
                generations.size == stubBatchCount && expectedResult == generations
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is STABILITY_AI, batch count is 10, generated successfully, expected generations list with size 10`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.STABILITY_AI)

        whenever(stubStabilityAiGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.just(mockAiGenerationResult))

        val stubBatchCount = 10
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        val expectedResult = (0 until 10).map { mockAiGenerationResult }

        useCase(stubPayload)
            .test()
            .assertNoErrors()
            .assertValue { generations ->
                generations.size == stubBatchCount && expectedResult == generations
            }
            .await()
            .assertComplete()
    }

    @Test
    fun `given source is STABILITY_AI, batch count is 1, generate failed, expected error`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.STABILITY_AI)

        whenever(stubStabilityAiGenerationRepository.generateFromImage(any()))
            .thenReturn(Single.error(stubException))

        val stubBatchCount = 1
        val stubPayload = mockImageToImagePayload.copy(batchCount = stubBatchCount)

        useCase(stubPayload)
            .test()
            .assertError(stubException)
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given source is LOCAL, expected Img2Img not yet supported error`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.LOCAL_MICROSOFT_ONNX)

        useCase(mockImageToImagePayload)
            .test()
            .assertError {
                it is IllegalStateException
                        && it.message?.startsWith("Img2Img not yet supported") == true
            }
            .await()
            .assertNotComplete()
    }

    @Test
    fun `given source is OPEN_AI, expected Img2Img not yet supported error`() {
        whenever(stubPreferenceManager.source)
            .thenReturn(ServerSource.OPEN_AI)

        useCase(mockImageToImagePayload)
            .test()
            .assertError {
                it is IllegalStateException
                        && it.message?.startsWith("Img2Img not yet supported") == true
            }
            .await()
            .assertNotComplete()
    }
}
