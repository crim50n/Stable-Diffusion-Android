package dev.minios.pdaiv1.domain.usecase.sdsampler

import dev.minios.pdaiv1.domain.entity.StableDiffusionSampler
import io.reactivex.rxjava3.core.Single

interface GetStableDiffusionSamplersUseCase {
    operator fun invoke(): Single<List<StableDiffusionSampler>>
}
