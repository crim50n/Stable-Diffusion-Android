package dev.minios.pdaiv1.domain.interactor.wakelock

import dev.minios.pdaiv1.domain.usecase.wakelock.AcquireWakelockUseCase
import dev.minios.pdaiv1.domain.usecase.wakelock.ReleaseWakeLockUseCase

interface WakeLockInterActor {
    val acquireWakelockUseCase: AcquireWakelockUseCase
    val releaseWakeLockUseCase: ReleaseWakeLockUseCase
}
