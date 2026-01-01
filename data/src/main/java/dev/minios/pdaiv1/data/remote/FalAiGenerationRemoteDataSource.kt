package dev.minios.pdaiv1.data.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import dev.minios.pdaiv1.core.common.log.debugLog
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.domain.datasource.FalAiGenerationDataSource
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.FalAiEndpoint
import dev.minios.pdaiv1.domain.entity.FalAiEndpointCategory
import dev.minios.pdaiv1.domain.entity.MediaType
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.MediaFileManager
import dev.minios.pdaiv1.network.api.falai.FalAiApi
import dev.minios.pdaiv1.network.request.FalAiImageSize
import dev.minios.pdaiv1.network.request.FalAiTextToImageRequest
import dev.minios.pdaiv1.network.response.FalAiGenerationResponse
import dev.minios.pdaiv1.network.response.FalAiImage
import dev.minios.pdaiv1.network.response.FalAiQueueResponse
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.util.Date
import java.util.concurrent.TimeUnit

internal class FalAiGenerationRemoteDataSource(
    private val api: FalAiApi,
    private val httpClient: OkHttpClient,
    private val mediaFileManager: MediaFileManager,
) : FalAiGenerationDataSource.Remote {

    override fun validateApiKey(): Single<Boolean> = api
        .listModels(limit = 1)
        .map { true }
        .onErrorResumeNext { t ->
            errorLog(t)
            Single.just(false)
        }

    override fun textToImage(model: String, payload: TextToImagePayload): Single<AiGenerationResult> {
        val request = payload.toFalAiRequest()
        val url = "$BASE_URL$model"
        debugLog("FalAi textToImage: url=$url, request=$request")

        return api.submitToQueue(url, request)
            .doOnError { t -> errorLog("FalAi submitToQueue error: ${t.message}") }
            .flatMap { queueResponse -> handleQueueResponse(queueResponse, payload) }
    }

    private fun handleQueueResponse(
        response: FalAiQueueResponse,
        payload: TextToImagePayload,
    ): Single<AiGenerationResult> {
        // If response already contains images (sync mode or fast completion)
        response.images?.firstOrNull()?.let { image ->
            return downloadAndConvertToBase64(image.url)
                .map { base64 -> createResult(payload, base64) }
        }

        // Otherwise, poll for completion
        val statusUrl = response.statusUrl
            ?: return Single.error(IllegalStateException("No status URL returned from fal.ai"))
        val resultUrl = response.responseUrl
            ?: return Single.error(IllegalStateException("No response URL returned from fal.ai"))

        return pollForCompletion(statusUrl, resultUrl, payload)
    }

    private fun pollForCompletion(
        statusUrl: String,
        resultUrl: String,
        payload: TextToImagePayload,
    ): Single<AiGenerationResult> {
        debugLog("FalAi pollForCompletion: checking status at $statusUrl")
        return api.checkStatus(statusUrl)
            .doOnSuccess { status -> debugLog("FalAi status response: ${status.status}") }
            .doOnError { t -> errorLog("FalAi checkStatus error: ${t.message}") }
            .flatMap { status ->
                when (status.status) {
                    "COMPLETED" -> {
                        debugLog("FalAi generation completed, fetching result from $resultUrl")
                        fetchAndProcessResult(resultUrl, payload)
                    }
                    "FAILED" -> Single.error(IllegalStateException("fal.ai generation failed"))
                    "IN_QUEUE", "IN_PROGRESS" -> {
                        debugLog("FalAi status: ${status.status}, queue position: ${status.queuePosition}")
                        // Wait and retry
                        Single.timer(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
                            .flatMap { pollForCompletion(statusUrl, resultUrl, payload) }
                    }
                    else -> Single.error(IllegalStateException("Unknown status: ${status.status}"))
                }
            }
    }

    private fun fetchAndProcessResult(
        resultUrl: String,
        payload: TextToImagePayload,
    ): Single<AiGenerationResult> {
        debugLog("FalAi fetchResult: fetching from $resultUrl")
        return api.fetchResult(resultUrl)
            .doOnSuccess { result -> debugLog("FalAi fetchResult response: images=${result.images?.size}, video=${result.video != null}") }
            .doOnError { t -> errorLog("FalAi fetchResult error: ${t.message}") }
            .flatMap { result ->
                val imageUrl = result.images?.firstOrNull()?.url
                    ?: return@flatMap Single.error<AiGenerationResult>(
                        IllegalStateException("No images in fal.ai response")
                    )
                downloadAndConvertToBase64(imageUrl)
                    .map { base64 -> createResult(payload, base64) }
            }
    }

    private fun downloadAndSaveMedia(url: String, mediaType: MediaType): Single<String> {
        return Single.fromCallable {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to download media: ${response.code}")
            }

            val bytes = response.body?.bytes()
                ?: throw IllegalStateException("Empty response body")

            when (mediaType) {
                MediaType.IMAGE -> {
                    // Decode to bitmap and re-encode to ensure proper PNG format
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        ?: throw IllegalStateException("Failed to decode image")

                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    mediaFileManager.saveMedia(outputStream.toByteArray(), MediaType.IMAGE)
                }
                MediaType.VIDEO -> {
                    // Save video bytes directly
                    mediaFileManager.saveMedia(bytes, MediaType.VIDEO)
                }
            }
        }
    }

    @Deprecated("Use downloadAndSaveMedia instead", replaceWith = ReplaceWith("downloadAndSaveMedia(imageUrl, MediaType.IMAGE)"))
    private fun downloadAndConvertToBase64(imageUrl: String): Single<String> {
        return Single.fromCallable {
            val request = Request.Builder().url(imageUrl).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to download image: ${response.code}")
            }

            val bytes = response.body?.bytes()
                ?: throw IllegalStateException("Empty response body")

            // Decode to bitmap and re-encode to ensure proper format
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("Failed to decode image")

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
    }

    private fun createResult(payload: TextToImagePayload, base64: String): AiGenerationResult {
        return AiGenerationResult(
            id = 0L,
            image = base64,
            inputImage = "",
            createdAt = Date(),
            type = AiGenerationResult.Type.TEXT_TO_IMAGE,
            prompt = payload.prompt,
            negativePrompt = payload.negativePrompt,
            width = payload.width,
            height = payload.height,
            samplingSteps = payload.samplingSteps,
            cfgScale = payload.cfgScale,
            restoreFaces = false,
            sampler = payload.sampler,
            seed = payload.seed,
            subSeed = "",
            subSeedStrength = 0f,
            denoisingStrength = 0f,
            hidden = false,
            modelName = payload.modelName,
        )
    }

    private fun TextToImagePayload.toFalAiRequest(): FalAiTextToImageRequest {
        return FalAiTextToImageRequest(
            prompt = prompt,
            negativePrompt = negativePrompt.takeIf { it.isNotBlank() },
            imageSize = FalAiImageSize(width = width, height = height),
            numInferenceSteps = samplingSteps,
            guidanceScale = cfgScale,
            seed = seed.toLongOrNull(),
            numImages = 1,
            enableSafetyChecker = false,
            syncMode = false,
        )
    }

    override fun generateDynamic(
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
    ): Single<List<AiGenerationResult>> {
        val url = "$BASE_URL${endpoint.endpointId}"

        // Filter out null values and add sync_mode = false
        val requestBody = parameters
            .filterValues { it != null }
            .toMutableMap()
            .apply { put("sync_mode", false) }

        debugLog("FalAi generateDynamic: url=$url, params=$requestBody")

        return api.submitDynamicToQueue(url, requestBody)
            .doOnError { t -> errorLog("FalAi submitDynamicToQueue error: ${t.message}") }
            .flatMap { queueResponse -> handleDynamicQueueResponse(queueResponse, endpoint, parameters) }
    }

    private fun handleDynamicQueueResponse(
        response: FalAiQueueResponse,
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
    ): Single<List<AiGenerationResult>> {
        // If response already contains images (sync mode or fast completion)
        val images = response.images
        if (!images.isNullOrEmpty()) {
            return processImages(images, endpoint, parameters, response.seed, response.prompt)
        }

        // If response contains video (for video generation endpoints)
        response.video?.let { video ->
            return processVideo(video, endpoint, parameters, response.seed, response.prompt)
        }

        // Otherwise, poll for completion
        val statusUrl = response.statusUrl
            ?: return Single.error(IllegalStateException("No status URL returned from fal.ai"))
        val resultUrl = response.responseUrl
            ?: return Single.error(IllegalStateException("No response URL returned from fal.ai"))

        return pollForDynamicCompletion(statusUrl, resultUrl, endpoint, parameters)
    }

    private fun pollForDynamicCompletion(
        statusUrl: String,
        resultUrl: String,
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
    ): Single<List<AiGenerationResult>> {
        debugLog("FalAi pollForDynamicCompletion: checking status at $statusUrl")
        return api.checkStatus(statusUrl)
            .doOnSuccess { status -> debugLog("FalAi dynamic status response: ${status.status}") }
            .doOnError { t -> errorLog("FalAi dynamic checkStatus error: ${t.message}") }
            .flatMap { status ->
                when (status.status) {
                    "COMPLETED" -> {
                        debugLog("FalAi dynamic generation completed, fetching result from $resultUrl")
                        fetchAndProcessDynamicResult(resultUrl, endpoint, parameters)
                    }
                    "FAILED" -> Single.error(IllegalStateException("fal.ai generation failed"))
                    "IN_QUEUE", "IN_PROGRESS" -> {
                        debugLog("FalAi dynamic status: ${status.status}, queue position: ${status.queuePosition}")
                        Single.timer(POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
                            .flatMap { pollForDynamicCompletion(statusUrl, resultUrl, endpoint, parameters) }
                    }
                    else -> Single.error(IllegalStateException("Unknown status: ${status.status}"))
                }
            }
    }

    private fun fetchAndProcessDynamicResult(
        resultUrl: String,
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
    ): Single<List<AiGenerationResult>> {
        debugLog("FalAi fetchDynamicResult: fetching from $resultUrl")
        return api.fetchResult(resultUrl)
            .doOnSuccess { result -> debugLog("FalAi fetchDynamicResult response: images=${result.images?.size}, video=${result.video != null}") }
            .doOnError { t -> errorLog("FalAi fetchDynamicResult error: ${t.message}") }
            .flatMap { result ->
                // Check for images first
                val images = result.images
                if (!images.isNullOrEmpty()) {
                    return@flatMap processImages(images, endpoint, parameters, result.seed, result.prompt)
                }

                // Check for video
                result.video?.let { video ->
                    return@flatMap processVideo(video, endpoint, parameters, result.seed, result.prompt)
                }

                // No images or video found
                Single.error<List<AiGenerationResult>>(
                    IllegalStateException("No images or video in fal.ai response")
                )
            }
    }

    private fun processImages(
        images: List<FalAiImage>,
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
        responseSeed: String? = null,
        responsePrompt: String? = null,
    ): Single<List<AiGenerationResult>> {
        return io.reactivex.rxjava3.core.Observable.fromIterable(images)
            .flatMapSingle { image ->
                downloadAndConvertToBase64(image.url)
                    .map { base64 ->
                        createDynamicImageResult(
                            endpoint = endpoint,
                            parameters = parameters,
                            base64 = base64,
                            responseSeed = responseSeed,
                            responsePrompt = responsePrompt,
                            imageWidth = image.width,
                            imageHeight = image.height,
                        )
                    }
            }
            .toList()
    }

    private fun processVideo(
        video: FalAiImage,
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
        responseSeed: String? = null,
        responsePrompt: String? = null,
    ): Single<List<AiGenerationResult>> {
        return downloadAndSaveMedia(video.url, MediaType.VIDEO)
            .map { mediaPath ->
                createDynamicResult(
                    endpoint = endpoint,
                    parameters = parameters,
                    mediaPath = mediaPath,
                    mediaType = MediaType.VIDEO,
                    responseSeed = responseSeed,
                    responsePrompt = responsePrompt,
                    imageWidth = video.width,
                    imageHeight = video.height,
                )
            }
            .map { listOf(it) }
    }

    private fun createDynamicImageResult(
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
        base64: String,
        responseSeed: String? = null,
        responsePrompt: String? = null,
        imageWidth: Int? = null,
        imageHeight: Int? = null,
    ): AiGenerationResult {
        val prompt = responsePrompt ?: parameters["prompt"]?.toString() ?: ""
        val negativePrompt = parameters["negative_prompt"]?.toString() ?: ""
        val width = imageWidth ?: extractWidth(parameters)
        val height = imageHeight ?: extractHeight(parameters)
        val steps = (parameters["num_inference_steps"] as? Number)?.toInt() ?: 28
        val guidance = (parameters["guidance_scale"] as? Number)?.toFloat() ?: 3.5f
        val seed = responseSeed ?: parameters["seed"]?.toString() ?: ""

        val generationType = when (endpoint.category) {
            FalAiEndpointCategory.IMAGE_TO_IMAGE,
            FalAiEndpointCategory.INPAINTING -> AiGenerationResult.Type.IMAGE_TO_IMAGE
            else -> AiGenerationResult.Type.TEXT_TO_IMAGE
        }

        return AiGenerationResult(
            id = 0L,
            image = base64,
            inputImage = "",
            createdAt = Date(),
            type = generationType,
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width,
            height = height,
            samplingSteps = steps,
            cfgScale = guidance,
            restoreFaces = false,
            sampler = "fal.ai/${endpoint.endpointId}",
            seed = seed,
            subSeed = "",
            subSeedStrength = 0f,
            denoisingStrength = 0f,
            hidden = false,
            modelName = endpoint.endpointId,
        )
    }

    private fun createDynamicResult(
        endpoint: FalAiEndpoint,
        parameters: Map<String, Any?>,
        mediaPath: String,
        mediaType: MediaType,
        responseSeed: String? = null,
        responsePrompt: String? = null,
        imageWidth: Int? = null,
        imageHeight: Int? = null,
    ): AiGenerationResult {
        // Prefer response values over input parameters
        val prompt = responsePrompt ?: parameters["prompt"]?.toString() ?: ""
        val negativePrompt = parameters["negative_prompt"]?.toString() ?: ""

        // Prefer actual image dimensions from response, then parameters, then defaults
        val width = imageWidth ?: extractWidth(parameters)
        val height = imageHeight ?: extractHeight(parameters)

        val steps = (parameters["num_inference_steps"] as? Number)?.toInt() ?: 28
        val guidance = (parameters["guidance_scale"] as? Number)?.toFloat() ?: 3.5f

        // Use seed from response if available (server may generate random seed)
        val seed = responseSeed ?: parameters["seed"]?.toString() ?: ""

        val generationType = when (endpoint.category) {
            FalAiEndpointCategory.IMAGE_TO_IMAGE,
            FalAiEndpointCategory.INPAINTING -> AiGenerationResult.Type.IMAGE_TO_IMAGE
            else -> AiGenerationResult.Type.TEXT_TO_IMAGE
        }

        return AiGenerationResult(
            id = 0L,
            image = "",
            inputImage = "",
            createdAt = Date(),
            type = generationType,
            prompt = prompt,
            negativePrompt = negativePrompt,
            width = width,
            height = height,
            samplingSteps = steps,
            cfgScale = guidance,
            restoreFaces = false,
            sampler = "fal.ai/${endpoint.endpointId}",
            seed = seed,
            subSeed = "",
            subSeedStrength = 0f,
            denoisingStrength = 0f,
            hidden = false,
            mediaPath = mediaPath,
            inputMediaPath = "",
            mediaType = mediaType,
            modelName = endpoint.endpointId,
        )
    }

    private fun extractWidth(parameters: Map<String, Any?>): Int {
        // Handle image_size as object with width/height
        val imageSize = parameters["image_size"]
        return when (imageSize) {
            is Map<*, *> -> (imageSize["width"] as? Number)?.toInt() ?: 1024
            is String -> parseImageSizeDimension(imageSize, true)
            else -> 1024
        }
    }

    private fun extractHeight(parameters: Map<String, Any?>): Int {
        val imageSize = parameters["image_size"]
        return when (imageSize) {
            is Map<*, *> -> (imageSize["height"] as? Number)?.toInt() ?: 1024
            is String -> parseImageSizeDimension(imageSize, false)
            else -> 1024
        }
    }

    private fun parseImageSizeDimension(sizeStr: String, isWidth: Boolean): Int {
        // Handle formats like "1024x1024", "landscape_16_9", etc.
        return if (sizeStr.contains("x")) {
            val parts = sizeStr.split("x")
            (if (isWidth) parts.getOrNull(0) else parts.getOrNull(1))
                ?.toIntOrNull() ?: 1024
        } else {
            1024
        }
    }

    companion object {
        private const val BASE_URL = "https://queue.fal.run/"
        private const val DEFAULT_MODEL = "fal-ai/flux-lora"
        private const val POLL_INTERVAL_MS = 2000L
    }
}
