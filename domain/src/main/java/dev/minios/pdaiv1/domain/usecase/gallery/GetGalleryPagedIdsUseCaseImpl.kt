package dev.minios.pdaiv1.domain.usecase.gallery

import dev.minios.pdaiv1.domain.repository.GenerationResultRepository
import io.reactivex.rxjava3.core.Single

class GetGalleryPagedIdsUseCaseImpl(
    private val repository: GenerationResultRepository,
) : GetGalleryPagedIdsUseCase {

    override fun invoke(): Single<List<Long>> = repository.getAllIds()

    override fun withBlurHash(): Single<List<Pair<Long, String>>> = repository.getAllIdsWithBlurHash()
}
