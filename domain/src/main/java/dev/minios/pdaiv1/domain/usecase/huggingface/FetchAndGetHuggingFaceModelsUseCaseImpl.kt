package dev.minios.pdaiv1.domain.usecase.huggingface

import dev.minios.pdaiv1.domain.entity.HuggingFaceModel
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.repository.HuggingFaceModelsRepository
import io.reactivex.rxjava3.core.Single

internal class FetchAndGetHuggingFaceModelsUseCaseImpl(
    private val preferenceManager: PreferenceManager,
    private val huggingFaceModelsRepository: HuggingFaceModelsRepository,
) : FetchAndGetHuggingFaceModelsUseCase {

    override fun invoke(): Single<List<HuggingFaceModel>> {
        if (preferenceManager.source != ServerSource.HUGGING_FACE) {
            return Single.just(emptyList())
        }
        return huggingFaceModelsRepository.fetchAndGetHuggingFaceModels()
    }
}
