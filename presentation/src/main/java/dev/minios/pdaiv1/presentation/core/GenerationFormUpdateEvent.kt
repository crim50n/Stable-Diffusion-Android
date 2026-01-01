package dev.minios.pdaiv1.presentation.core

import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class GenerationFormUpdateEvent {

    private val sRoute: PublishSubject<AiGenerationResult.Type> = PublishSubject.create()
    private val sFalAiRoute: PublishSubject<Unit> = PublishSubject.create()
    private val sTxt2Img: BehaviorSubject<Payload> = BehaviorSubject.createDefault(Payload.None)
    private val sImg2Img: BehaviorSubject<Payload> = BehaviorSubject.createDefault(Payload.None)
    private val sFalAi: BehaviorSubject<Payload> = BehaviorSubject.createDefault(Payload.None)

    fun update(
        generation: AiGenerationResult,
        route: AiGenerationResult.Type,
        inputImage: Boolean,
    ) {
        sRoute.onNext(route)
        when (route) {
            AiGenerationResult.Type.TEXT_TO_IMAGE -> sTxt2Img.onNext(Payload.T2IForm(generation))
            AiGenerationResult.Type.IMAGE_TO_IMAGE -> sImg2Img.onNext(Payload.I2IForm(generation, inputImage))
        }
    }

    fun clear() {
        sTxt2Img.onNext(Payload.None)
        sImg2Img.onNext(Payload.None)
        sFalAi.onNext(Payload.None)
    }

    fun observeRoute() = sRoute.toFlowable(BackpressureStrategy.LATEST)

    fun observeTxt2ImgForm() = sTxt2Img.toFlowable(BackpressureStrategy.LATEST)

    fun observeImg2ImgForm() = sImg2Img.toFlowable(BackpressureStrategy.LATEST)

    fun updateFalAi(generation: AiGenerationResult) {
        sFalAiRoute.onNext(Unit)
        sFalAi.onNext(Payload.FalAiForm(generation))
    }

    fun observeFalAiRoute() = sFalAiRoute.toFlowable(BackpressureStrategy.LATEST)

    fun observeFalAiForm() = sFalAi.toFlowable(BackpressureStrategy.LATEST)

    sealed interface Payload {
        data object None : Payload
        data class T2IForm(val ai: AiGenerationResult): Payload
        data class I2IForm(val ai: AiGenerationResult, val inputImage: Boolean): Payload
        data class FalAiForm(val ai: AiGenerationResult): Payload
    }
}
