package dev.minios.pdaiv1.domain.usecase.wakelock

import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.domain.repository.WakeLockRepository

internal class ReleaseWakeLockUseCaseImpl(
    private val wakeLockRepository: WakeLockRepository,
) : ReleaseWakeLockUseCase {

    override fun invoke() = runCatching {
        wakeLockRepository.wakeLock.release()
    }.onFailure { t ->
        errorLog(t)
    }
}
