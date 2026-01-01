package dev.minios.pdaiv1.work.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.minios.pdaiv1.core.common.appbuild.ActivityIntentProvider
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.core.notification.PushNotificationManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.generation.FalAiGenerationUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ImageToImageUseCase
import dev.minios.pdaiv1.domain.usecase.generation.InterruptGenerationUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.TextToImageUseCase
import dev.minios.pdaiv1.work.task.FalAiTask
import dev.minios.pdaiv1.work.task.ImageToImageTask
import dev.minios.pdaiv1.work.task.TextToImageTask

class SdaiWorkerFactory(
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val pushNotificationManager: PushNotificationManager,
    private val preferenceManager: PreferenceManager,
    private val textToImageUseCase: TextToImageUseCase,
    private val imageToImageUseCase: ImageToImageUseCase,
    private val falAiGenerationUseCase: FalAiGenerationUseCase,
    private val observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    private val observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    private val interruptGenerationUseCase: InterruptGenerationUseCase,
    private val fileProviderDescriptor: FileProviderDescriptor,
    private val activityIntentProvider: ActivityIntentProvider,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            TextToImageTask::class.java.name -> TextToImageTask(
                context = appContext,
                workerParameters = workerParameters,
                pushNotificationManager = pushNotificationManager,
                activityIntentProvider = activityIntentProvider,
                backgroundWorkObserver = backgroundWorkObserver,
                preferenceManager = preferenceManager,
                textToImageUseCase = textToImageUseCase,
                observeHordeProcessStatusUseCase = observeHordeProcessStatusUseCase,
                observeLocalDiffusionProcessStatusUseCase = observeLocalDiffusionProcessStatusUseCase,
                interruptGenerationUseCase = interruptGenerationUseCase,
                fileProviderDescriptor = fileProviderDescriptor,
            )

            ImageToImageTask::class.java.name -> ImageToImageTask(
                context = appContext,
                workerParameters = workerParameters,
                pushNotificationManager = pushNotificationManager,
                activityIntentProvider = activityIntentProvider,
                backgroundWorkObserver = backgroundWorkObserver,
                preferenceManager = preferenceManager,
                imageToImageUseCase = imageToImageUseCase,
                observeHordeProcessStatusUseCase = observeHordeProcessStatusUseCase,
                observeLocalDiffusionProcessStatusUseCase = observeLocalDiffusionProcessStatusUseCase,
                interruptGenerationUseCase = interruptGenerationUseCase,
                fileProviderDescriptor = fileProviderDescriptor,
            )

            FalAiTask::class.java.name -> FalAiTask(
                context = appContext,
                workerParameters = workerParameters,
                pushNotificationManager = pushNotificationManager,
                activityIntentProvider = activityIntentProvider,
                backgroundWorkObserver = backgroundWorkObserver,
                preferenceManager = preferenceManager,
                falAiGenerationUseCase = falAiGenerationUseCase,
                observeHordeProcessStatusUseCase = observeHordeProcessStatusUseCase,
                observeLocalDiffusionProcessStatusUseCase = observeLocalDiffusionProcessStatusUseCase,
                interruptGenerationUseCase = interruptGenerationUseCase,
                fileProviderDescriptor = fileProviderDescriptor,
            )

            else -> null
        }
    }
}
