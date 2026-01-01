package dev.minios.pdaiv1.work.di

import androidx.work.WorkManager
import androidx.work.WorkerFactory
import dev.minios.pdaiv1.domain.feature.work.BackgroundTaskManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.work.BackgroundTaskManagerImpl
import dev.minios.pdaiv1.work.BackgroundWorkObserverImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module

val backgroundWorkModule = module {
    factory {
        WorkManagerProvider { WorkManager.getInstance(androidApplication()) }
    }

    factoryOf(::SdaiWorkerFactory) binds arrayOf(SdaiWorkerFactory::class, WorkerFactory::class)
    singleOf(::BackgroundWorkObserverImpl) bind BackgroundWorkObserver::class
    factoryOf(::BackgroundTaskManagerImpl) bind BackgroundTaskManager::class
}
