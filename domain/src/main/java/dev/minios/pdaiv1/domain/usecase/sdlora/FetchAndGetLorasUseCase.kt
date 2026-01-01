package dev.minios.pdaiv1.domain.usecase.sdlora

import dev.minios.pdaiv1.domain.entity.LoRA
import io.reactivex.rxjava3.core.Single

interface FetchAndGetLorasUseCase {
    operator fun invoke(): Single<List<LoRA>>
}
