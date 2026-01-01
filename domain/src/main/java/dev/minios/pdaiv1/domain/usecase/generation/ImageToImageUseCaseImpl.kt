package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.HordeGenerationRepository
import dev.minios.pdaiv1.domain.repository.HuggingFaceGenerationRepository
import dev.minios.pdaiv1.domain.repository.QnnGenerationRepository
import dev.minios.pdaiv1.domain.repository.StabilityAiGenerationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionGenerationRepository
import dev.minios.pdaiv1.domain.repository.SwarmUiGenerationRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

internal class ImageToImageUseCaseImpl(
    private val stableDiffusionGenerationRepository: StableDiffusionGenerationRepository,
    private val swarmUiGenerationRepository: SwarmUiGenerationRepository,
    private val hordeGenerationRepository: HordeGenerationRepository,
    private val huggingFaceGenerationRepository: HuggingFaceGenerationRepository,
    private val stabilityAiGenerationRepository: StabilityAiGenerationRepository,
    private val qnnGenerationRepository: QnnGenerationRepository,
    private val preferenceManager: PreferenceManager,
) : ImageToImageUseCase {

    override fun invoke(payload: ImageToImagePayload) = Observable
        .range(1, payload.batchCount)
        .concatMapSingle { generate(payload) }
        .toList()

    private fun generate(payload: ImageToImagePayload) = when (preferenceManager.source) {
        ServerSource.AUTOMATIC1111 -> stableDiffusionGenerationRepository.generateFromImage(payload)
        ServerSource.SWARM_UI -> swarmUiGenerationRepository.generateFromImage(payload)
        ServerSource.HORDE -> hordeGenerationRepository.generateFromImage(payload)
        ServerSource.HUGGING_FACE -> huggingFaceGenerationRepository.generateFromImage(payload)
        ServerSource.STABILITY_AI -> stabilityAiGenerationRepository.generateFromImage(payload)
        ServerSource.LOCAL_QUALCOMM_QNN -> qnnGenerationRepository.generateFromImage(payload)
        else -> Single.error(IllegalStateException("Img2Img not yet supported on ${preferenceManager.source}!"))
    }
}
