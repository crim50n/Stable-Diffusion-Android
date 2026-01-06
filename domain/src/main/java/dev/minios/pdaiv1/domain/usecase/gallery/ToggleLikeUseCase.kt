package dev.minios.pdaiv1.domain.usecase.gallery

import io.reactivex.rxjava3.core.Single

interface ToggleLikeUseCase {
    operator fun invoke(id: Long): Single<Boolean>
}
