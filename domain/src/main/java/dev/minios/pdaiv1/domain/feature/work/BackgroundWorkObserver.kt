package dev.minios.pdaiv1.domain.feature.work

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.BackgroundWorkResult
import dev.minios.pdaiv1.domain.entity.BackgroundWorkStatus
import io.reactivex.rxjava3.core.Flowable

interface BackgroundWorkObserver {
    fun observeStatus(): Flowable<BackgroundWorkStatus>
    fun observeResult(): Flowable<BackgroundWorkResult>
    fun dismissResult()
    fun refreshStatus()
    fun postStatusMessage(title: String, subTitle: String)
    fun postSuccessSignal(result: List<AiGenerationResult>)
    fun postCancelSignal()
    fun postFailedSignal(t: Throwable)
    fun hasActiveTasks(): Boolean
}
