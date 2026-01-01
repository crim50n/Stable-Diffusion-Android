package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.FalAiPayload
import dev.minios.pdaiv1.domain.repository.FalAiEndpointRepository
import dev.minios.pdaiv1.domain.repository.FalAiGenerationRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

class FalAiGenerationUseCaseImpl(
    private val falAiEndpointRepository: FalAiEndpointRepository,
    private val falAiGenerationRepository: FalAiGenerationRepository,
    private val saveGenerationResultUseCase: SaveGenerationResultUseCase,
) : FalAiGenerationUseCase {

    override fun invoke(payload: FalAiPayload): Single<List<AiGenerationResult>> {
        return falAiEndpointRepository.getAll()
            .flatMap { endpoints ->
                val endpoint = endpoints.find { it.id == payload.endpointId || it.endpointId == payload.endpointId }
                    ?: return@flatMap Single.error(Throwable("Endpoint not found: ${payload.endpointId}"))

                falAiGenerationRepository.generateDynamic(endpoint, payload.parameters)
                    .flatMap { results ->
                        val saveCompletables = results.map { saveGenerationResultUseCase(it) }
                        Completable.merge(saveCompletables)
                            .andThen(Single.just(results))
                    }
            }
    }
}
