package dev.minios.pdaiv1.data.remote

import dev.minios.pdaiv1.core.common.extensions.fixUrlSlashes
import dev.minios.pdaiv1.core.imageprocessing.Base64EncodingConverter
import dev.minios.pdaiv1.core.imageprocessing.BitmapToBase64Converter
import dev.minios.pdaiv1.data.mappers.mapCloudToAiGenResult
import dev.minios.pdaiv1.data.mappers.mapToSwarmUiRequest
import dev.minios.pdaiv1.data.provider.ServerUrlProvider
import dev.minios.pdaiv1.domain.datasource.SwarmUiGenerationDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.network.api.swarmui.SwarmUiApi
import dev.minios.pdaiv1.network.api.swarmui.SwarmUiApi.Companion.PATH_GENERATE
import dev.minios.pdaiv1.network.request.SwarmUiGenerationRequest
import io.reactivex.rxjava3.core.Single

class SwarmUiGenerationRemoteDataSource(
    private val serverUrlProvider: ServerUrlProvider,
    private val api: SwarmUiApi,
    private val bmpToBase64Converter: BitmapToBase64Converter,
    private val base64EncodingConverter: Base64EncodingConverter,
) : SwarmUiGenerationDataSource.Remote {

    override fun textToImage(
        sessionId: String,
        model: String,
        payload: TextToImagePayload
    ): Single<AiGenerationResult> =
        generate(
            payload = payload,
            request = payload.mapToSwarmUiRequest(sessionId, model),
        )
        .map(Pair<TextToImagePayload, String>::mapCloudToAiGenResult)

    override fun imageToImage(
        sessionId: String,
        model: String,
        payload: ImageToImagePayload,
    ): Single<AiGenerationResult> = payload
        .base64Image
        .let(Base64EncodingConverter::Input)
        .let(base64EncodingConverter::invoke)
        .map(Base64EncodingConverter.Output::base64)
        .map { base64 -> "data:image/png;base64,${base64}" }
        .map { base64Uri -> payload.copy(base64Image = base64Uri) }
        .flatMap { encodedPayload ->
            generate(
                payload = encodedPayload,
                request = encodedPayload.mapToSwarmUiRequest(sessionId, model),
            )
        }
        .map { (_, outBase64) -> payload to outBase64 }
        .map(Pair<ImageToImagePayload, String>::mapCloudToAiGenResult)

    private fun <T: Any> generate(
        payload: T,
        request: SwarmUiGenerationRequest,
    ): Single<Pair<T, String>> = serverUrlProvider(PATH_GENERATE)
        .flatMap { url -> api.generate(url, request) }
        .flatMap { response ->
            serverUrlProvider("").map { url -> response to url }
        }
        .flatMap { (response, url) ->
            response.images
                ?.firstOrNull()
                ?.let { endpoint -> Single.just("$url/$endpoint".fixUrlSlashes()) }
                ?: Single.error(IllegalStateException("Bad response"))
        }
        .flatMap(api::downloadImage)
        .map(BitmapToBase64Converter::Input)
        .flatMap(bmpToBase64Converter::invoke)
        .map(BitmapToBase64Converter.Output::base64ImageString)
        .map { base64 -> payload to base64 }
}
