package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.data.mappers.mapCloudToAiGenResult
import dev.minios.pdaiv1.data.mappers.mapToHuggingFaceRequest
import dev.minios.pdaiv1.domain.datasource.HuggingFaceGenerationDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.network.api.huggingface.HuggingFaceApi
import dev.minios.pdaiv1.network.api.huggingface.HuggingFaceInferenceApi
import io.reactivex.rxjava3.core.Single

internal class HuggingFaceGenerationRemoteDataSource(
    private val huggingFaceApi: HuggingFaceApi,
    private val huggingFaceInferenceApi: HuggingFaceInferenceApi,
    private val converter: BitmapToBase64Converter,
) : HuggingFaceGenerationDataSource.Remote {

    override fun validateApiKey(): Single<Boolean> = huggingFaceApi
        .validateBearerToken()
        .andThen(Single.just(true))
        .onErrorResumeNext { t ->
            errorLog(t)
            Single.just(false)
        }

    override fun textToImage(
        modelName: String,
        payload: TextToImagePayload,
    ): Single<AiGenerationResult> = huggingFaceInferenceApi
        .generate(modelName, payload.mapToHuggingFaceRequest())
        .map(BitmapToBase64Converter::Input)
        .flatMap(converter::invoke)
        .map(BitmapToBase64Converter.Output::base64ImageString)
        .map { base64 -> payload to base64 }
        .map(Pair<TextToImagePayload, String>::mapCloudToAiGenResult)

    override fun imageToImage(
        modelName: String,
        payload: ImageToImagePayload,
    ): Single<AiGenerationResult> = huggingFaceInferenceApi
        .generate(modelName, payload.mapToHuggingFaceRequest())
        .map(BitmapToBase64Converter::Input)
        .flatMap(converter::invoke)
        .map(BitmapToBase64Converter.Output::base64ImageString)
        .map { base64 -> payload to base64 }
        .map(Pair<ImageToImagePayload, String>::mapCloudToAiGenResult)

}
