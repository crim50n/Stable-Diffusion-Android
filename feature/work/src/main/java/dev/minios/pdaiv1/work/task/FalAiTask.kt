package dev.minios.pdaiv1.work.task

import android.content.Context
import androidx.work.WorkerParameters
import dev.minios.pdaiv1.core.common.appbuild.ActivityIntentProvider
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.core.common.log.debugLog
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.notification.PushNotificationManager
import dev.minios.pdaiv1.domain.feature.work.BackgroundWorkObserver
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.generation.FalAiGenerationUseCase
import dev.minios.pdaiv1.domain.usecase.generation.InterruptGenerationUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import dev.minios.pdaiv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import dev.minios.pdaiv1.work.Constants
import dev.minios.pdaiv1.work.Constants.NOTIFICATION_FAL_AI_FOREGROUND
import dev.minios.pdaiv1.work.Constants.NOTIFICATION_FAL_AI_GENERIC
import dev.minios.pdaiv1.work.core.CoreGenerationWorker
import dev.minios.pdaiv1.work.mappers.toFalAiPayload
import io.reactivex.rxjava3.core.Single
import java.io.File

internal class FalAiTask(
    context: Context,
    workerParameters: WorkerParameters,
    pushNotificationManager: PushNotificationManager,
    activityIntentProvider: ActivityIntentProvider,
    observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    interruptGenerationUseCase: InterruptGenerationUseCase,
    private val preferenceManager: PreferenceManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val falAiGenerationUseCase: FalAiGenerationUseCase,
    private val fileProviderDescriptor: FileProviderDescriptor,
) : CoreGenerationWorker(
    context = context,
    workerParameters = workerParameters,
    pushNotificationManager = pushNotificationManager,
    activityIntentProvider = activityIntentProvider,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
    observeHordeProcessStatusUseCase = observeHordeProcessStatusUseCase,
    observeLocalDiffusionProcessStatusUseCase = observeLocalDiffusionProcessStatusUseCase,
    interruptGenerationUseCase = interruptGenerationUseCase,
) {

    override val notificationId: Int = NOTIFICATION_FAL_AI_FOREGROUND

    override val genericNotificationId: Int = NOTIFICATION_FAL_AI_GENERIC

    override fun createWork(): Single<Result> {
        if (preferenceManager.backgroundProcessCount > 0) {
            handleProcess()
            handleError(Throwable("Background process count > 0"))
            compositeDisposable.clear()
            preferenceManager.backgroundProcessCount = 0
            debugLog("Background process count > 0! Skipping task.")
            return Single.just(Result.failure())
        }

        preferenceManager.backgroundProcessCount++
        handleStart()
        backgroundWorkObserver.refreshStatus()
        backgroundWorkObserver.dismissResult()
        debugLog("Starting FalAiTask!")

        return try {
            val file = File(fileProviderDescriptor.workCacheDirPath, Constants.FILE_FAL_AI)
            if (!file.exists()) {
                preferenceManager.backgroundProcessCount--
                val t = Throwable("File is null.")
                handleError(t)
                compositeDisposable.clear()
                errorLog(t, "Payload file does not exist.")
                return Single.just(Result.failure())
            }

            val bytes = file.readBytes()
            val payload = bytes.toFalAiPayload()

            if (payload == null) {
                preferenceManager.backgroundProcessCount--
                val t = Throwable("Payload is null.")
                handleError(t)
                compositeDisposable.clear()
                errorLog(t, "Payload was failed to read/parse.")
                return Single.just(Result.failure())
            }

            falAiGenerationUseCase(payload)
                .doOnSubscribe { handleProcess() }
                .map { result ->
                    preferenceManager.backgroundProcessCount--
                    handleSuccess(result)
                    debugLog("Fal AI generation finished successfully!")
                    Result.success()
                }
                .onErrorReturn { t ->
                    preferenceManager.backgroundProcessCount--
                    handleError(t)
                    errorLog(t, "Caught exception from FalAiGenerationUseCase!")
                    Result.failure()
                }
                .doFinally { compositeDisposable.clear() }
        } catch (e: Exception) {
            preferenceManager.backgroundProcessCount--
            handleError(e)
            compositeDisposable.clear()
            errorLog(e, "Caught exception from FalAiTask worker!")
            Single.just(Result.failure())
        }
    }
}
