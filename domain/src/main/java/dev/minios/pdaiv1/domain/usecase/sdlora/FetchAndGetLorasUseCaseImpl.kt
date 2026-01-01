package dev.minios.pdaiv1.domain.usecase.sdlora

import dev.minios.pdaiv1.domain.repository.LorasRepository

internal class FetchAndGetLorasUseCaseImpl(
    private val lorasRepository: LorasRepository,
) : FetchAndGetLorasUseCase {

    override fun invoke() = lorasRepository.fetchAndGetLoras()
}
