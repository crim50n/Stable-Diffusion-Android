package dev.minios.pdaiv1.work.di

import androidx.work.WorkManager

fun interface WorkManagerProvider {
    operator fun invoke(): WorkManager
}
