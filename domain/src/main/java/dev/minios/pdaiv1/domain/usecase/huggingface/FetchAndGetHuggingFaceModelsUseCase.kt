package dev.minios.pdaiv1.domain.usecase.huggingface

import dev.minios.pdaiv1.domain.entity.HuggingFaceModel
import io.reactivex.rxjava3.core.Single

interface FetchAndGetHuggingFaceModelsUseCase {
    operator fun invoke(): Single<List<HuggingFaceModel>>
}
