package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.HordeGenerationRepository
import dev.minios.pdaiv1.domain.repository.LocalDiffusionGenerationRepository
import dev.minios.pdaiv1.domain.repository.QnnGenerationRepository
import dev.minios.pdaiv1.domain.repository.StableDiffusionGenerationRepository
import io.reactivex.rxjava3.core.Completable

internal class InterruptGenerationUseCaseImpl(
    private val stableDiffusionGenerationRepository: StableDiffusionGenerationRepository,
    private val hordeGenerationRepository: HordeGenerationRepository,
    private val localDiffusionGenerationRepository: LocalDiffusionGenerationRepository,
    private val qnnGenerationRepository: QnnGenerationRepository,
    private val preferenceManager: PreferenceManager,
) : InterruptGenerationUseCase {

    override fun invoke() = when (preferenceManager.source) {
        ServerSource.AUTOMATIC1111 -> stableDiffusionGenerationRepository.interruptGeneration()
        ServerSource.HORDE -> hordeGenerationRepository.interruptGeneration()
        ServerSource.LOCAL_MICROSOFT_ONNX -> localDiffusionGenerationRepository.interruptGeneration()
        ServerSource.LOCAL_QUALCOMM_QNN -> qnnGenerationRepository.interruptGeneration()
        else -> Completable.complete()
    }
}
