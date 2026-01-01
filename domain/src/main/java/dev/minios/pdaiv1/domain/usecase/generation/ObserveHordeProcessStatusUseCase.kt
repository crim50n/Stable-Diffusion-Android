package dev.minios.pdaiv1.domain.usecase.generation

import dev.minios.pdaiv1.domain.entity.HordeProcessStatus
import io.reactivex.rxjava3.core.Flowable

interface ObserveHordeProcessStatusUseCase {
    operator fun invoke(): Flowable<HordeProcessStatus>
}
