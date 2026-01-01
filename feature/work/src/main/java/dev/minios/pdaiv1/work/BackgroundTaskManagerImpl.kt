package dev.minios.pdaiv1.work

import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.domain.entity.FalAiPayload
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.work.BackgroundTaskManager
import dev.minios.pdaiv1.work.di.WorkManagerProvider
import dev.minios.pdaiv1.work.mappers.toByteArray
import dev.minios.pdaiv1.work.mappers.toByteArray as toFalAiByteArray
import dev.minios.pdaiv1.work.task.FalAiTask
import dev.minios.pdaiv1.work.task.ImageToImageTask
import dev.minios.pdaiv1.work.task.TextToImageTask
import org.koin.java.KoinJavaComponent.inject
import java.io.File

internal class BackgroundTaskManagerImpl : BackgroundTaskManager {

    override fun scheduleTextToImageTask(payload: TextToImagePayload) {
        runWork<TextToImageTask>(payload.toByteArray(), Constants.FILE_TEXT_TO_IMAGE)
    }

    override fun scheduleImageToImageTask(payload: ImageToImagePayload) {
        runWork<ImageToImageTask>(payload.toByteArray(), Constants.FILE_IMAGE_TO_IMAGE)
    }

    override fun scheduleFalAiTask(payload: FalAiPayload) {
        runWork<FalAiTask>(payload.toByteArray(), Constants.FILE_FAL_AI)
    }

    override fun retryLastTextToImageTask(): Result<Unit> {
        try {
            val bytes = readPayload(Constants.FILE_TEXT_TO_IMAGE)
                ?: return Result.failure(Throwable("Payload is null."))
            runWork<TextToImageTask>(bytes, Constants.FILE_TEXT_TO_IMAGE)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun retryLastImageToImageTask(): Result<Unit> {
        try {
            val bytes = readPayload(Constants.FILE_IMAGE_TO_IMAGE)
                ?: return Result.failure(Throwable("Payload is null."))
            runWork<ImageToImageTask>(bytes, Constants.FILE_IMAGE_TO_IMAGE)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun retryLastFalAiTask(): Result<Unit> {
        try {
            val bytes = readPayload(Constants.FILE_FAL_AI)
                ?: return Result.failure(Throwable("Payload is null."))
            runWork<FalAiTask>(bytes, Constants.FILE_FAL_AI)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun cancelAll(): Result<Unit> = runCatching {
        val workManager: WorkManagerProvider by inject(WorkManagerProvider::class.java)
        workManager().cancelAllWork()
    }

    private inline fun <reified W : ListenableWorker> runWork(bytes: ByteArray, fileName: String) {
        val workManager: WorkManagerProvider by inject(WorkManagerProvider::class.java)
        val workRequest = OneTimeWorkRequestBuilder<W>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(Constants.TAG_GENERATION)
            .build()

        writePayload(bytes, fileName)
        workManager().cancelUniqueWork(Constants.TAG_GENERATION)
        workManager().enqueueUniqueWork(
            Constants.TAG_GENERATION,
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
    }

    private fun readPayload(fileName: String): ByteArray? {
        val fileProviderDescriptor: FileProviderDescriptor by inject(FileProviderDescriptor::class.java)
        val cacheDirectory = File(fileProviderDescriptor.workCacheDirPath)
        if (!cacheDirectory.exists()) {
            return null
        }
        val outFile = File(cacheDirectory, fileName)
        return outFile.readBytes()
    }

    private fun writePayload(bytes: ByteArray, fileName: String) {
        val fileProviderDescriptor: FileProviderDescriptor by inject(FileProviderDescriptor::class.java)
        val cacheDirectory = File(fileProviderDescriptor.workCacheDirPath)
        if (!cacheDirectory.exists()) cacheDirectory.mkdirs()
        val outFile = File(cacheDirectory, fileName)
        if (!outFile.exists()) outFile.createNewFile()
        outFile.writeBytes(bytes)
    }
}
