package dev.minios.pdaiv1.domain.usecase.sdsampler

import dev.minios.pdaiv1.domain.entity.StableDiffusionSampler
import dev.minios.pdaiv1.domain.repository.StableDiffusionSamplersRepository
import io.reactivex.rxjava3.core.Single

internal class GetStableDiffusionSamplersUseCaseImpl(
    private val repository: StableDiffusionSamplersRepository,
) : GetStableDiffusionSamplersUseCase {

    override operator fun invoke(): Single<List<StableDiffusionSampler>> = repository
        .getSamplers()
}
