package dev.minios.pdaiv1.domain.usecase.gallery

import io.reactivex.rxjava3.core.Completable

interface DeleteAllUnlikedUseCase {
    operator fun invoke(): Completable
}
