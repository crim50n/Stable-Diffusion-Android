package dev.minios.pdaiv1.domain.feature.work

import dev.minios.pdaiv1.domain.entity.FalAiPayload
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload

interface BackgroundTaskManager {
    fun scheduleTextToImageTask(payload: TextToImagePayload)
    fun scheduleImageToImageTask(payload: ImageToImagePayload)
    fun scheduleFalAiTask(payload: FalAiPayload)
    fun retryLastTextToImageTask(): Result<Unit>
    fun retryLastImageToImageTask(): Result<Unit>
    fun retryLastFalAiTask(): Result<Unit>
    fun cancelAll(): Result<Unit>
}
