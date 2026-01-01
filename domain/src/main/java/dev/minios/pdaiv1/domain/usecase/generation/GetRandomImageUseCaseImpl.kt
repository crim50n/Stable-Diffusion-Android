package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.repository.RandomImageRepository

class GetRandomImageUseCaseImpl(
    private val randomImageRepository: RandomImageRepository,
) : GetRandomImageUseCase {

    override fun invoke() = randomImageRepository.fetchAndGet()
}
