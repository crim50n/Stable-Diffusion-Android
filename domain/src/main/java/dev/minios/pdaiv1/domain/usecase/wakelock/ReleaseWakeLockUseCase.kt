package dev.minios.pdaiv1.domain.usecase.wakelock

interface ReleaseWakeLockUseCase {
    operator fun invoke(): Result<Unit>
}
