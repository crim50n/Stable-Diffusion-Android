package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.entity.LocalAiModel
import io.reactivex.rxjava3.core.Single

interface GetLocalMediaPipeModelsUseCase {
    operator fun invoke(): Single<List<LocalAiModel>>
}
