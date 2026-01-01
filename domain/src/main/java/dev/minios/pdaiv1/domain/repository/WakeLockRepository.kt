package dev.minios.pdaiv1.domain.repository

import android.os.PowerManager

interface WakeLockRepository {
    val wakeLock: PowerManager.WakeLock
}
