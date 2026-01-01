package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.FalAiGenerationRepository
import dev.minios.pdaiv1.domain.repository.HordeGenerationRepository
import dev.minios.pdaiv1.domain.repository.HuggingFaceGenerationRepository
import dev.minios.pdaiv1.domain.repository.LocalDiffusionGenerationRepository
import dev.minios.pdaiv1.domain.repository.MediaPipeGenerationRepository
import dev.minios.pdaiv1.domain.repository.OpenAiGenerationRepository
import dev.minios.pdaiv1.domain.repository.QnnGenerationRepository
import dev.minios.pdaiv1.domain.repository.StabilityAiGenerationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionGenerationRepository
import dev.minios.pdaiv1.domain.repository.SwarmUiGenerationRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal class TextToImageUseCaseImpl(
    private val stableDiffusionGenerationRepository: StableDiffusionGenerationRepository,
    private val hordeGenerationRepository: HordeGenerationRepository,
    private val huggingFaceGenerationRepository: HuggingFaceGenerationRepository,
    private val openAiGenerationRepository: OpenAiGenerationRepository,
    private val stabilityAiGenerationRepository: StabilityAiGenerationRepository,
    private val falAiGenerationRepository: FalAiGenerationRepository,
    private val swarmUiGenerationRepository: SwarmUiGenerationRepository,
    private val localDiffusionGenerationRepository: LocalDiffusionGenerationRepository,
    private val mediaPipeGenerationRepository: MediaPipeGenerationRepository,
    private val qnnGenerationRepository: QnnGenerationRepository,
    private val preferenceManager: PreferenceManager,
) : TextToImageUseCase {

    override operator fun invoke(
        payload: TextToImagePayload,
    ): Single<List<AiGenerationResult>> = Observable
        .range(1, payload.batchCount)
        .concatMapSingle { generate(payload) }
        .toList()

    private fun generate(payload: TextToImagePayload) = when (preferenceManager.source) {
        ServerSource.HORDE -> hordeGenerationRepository.generateFromText(payload)
        ServerSource.LOCAL_MICROSOFT_ONNX -> localDiffusionGenerationRepository.generateFromText(payload)
        ServerSource.HUGGING_FACE -> huggingFaceGenerationRepository.generateFromText(payload)
        ServerSource.AUTOMATIC1111 -> stableDiffusionGenerationRepository.generateFromText(payload)
        ServerSource.OPEN_AI -> openAiGenerationRepository.generateFromText(payload)
        ServerSource.STABILITY_AI -> stabilityAiGenerationRepository.generateFromText(payload)
        ServerSource.FAL_AI -> falAiGenerationRepository.generateFromText(payload)
        ServerSource.SWARM_UI -> swarmUiGenerationRepository.generateFromText(payload)
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> mediaPipeGenerationRepository.generateFromText(payload)
        ServerSource.LOCAL_QUALCOMM_QNN -> qnnGenerationRepository.generateFromText(payload)
    }
}
