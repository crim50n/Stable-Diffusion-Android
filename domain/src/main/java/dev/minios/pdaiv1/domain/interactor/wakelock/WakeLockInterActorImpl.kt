package dev.minios.pdaiv1.domain.interactor.wakelock

import dev.minios.pdaiv1.domain.usecase.wakelock.AcquireWakelockUseCase
import dev.minios.pdaiv1.domain.usecase.wakelock.ReleaseWakeLockUseCase

internal data class WakeLockInterActorImpl(
    override val acquireWakelockUseCase: AcquireWakelockUseCase,
    override val releaseWakeLockUseCase: ReleaseWakeLockUseCase
) : WakeLockInterActor
