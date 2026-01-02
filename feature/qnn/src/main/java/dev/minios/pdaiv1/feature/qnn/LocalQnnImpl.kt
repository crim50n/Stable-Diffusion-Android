package dev.minios.pdaiv1.feature.qnn

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.domain.entity.ImageToImagePayload
import dev.minios.pdaiv1.domain.entity.LocalAiModel
import dev.minios.pdaiv1.domain.entity.LocalDiffusionStatus
import dev.minios.pdaiv1.domain.entity.TextToImagePayload
import dev.minios.pdaiv1.domain.feature.qnn.LocalQnn
import dev.minios.pdaiv1.domain.feature.qnn.QnnGenerationResult
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.feature.qnn.api.model.CompleteEvent
import dev.minios.pdaiv1.feature.qnn.api.model.GenerateRequest
import dev.minios.pdaiv1.feature.qnn.api.model.ProgressEvent
import dev.minios.pdaiv1.feature.qnn.service.QnnBackendService
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

/**
 * Implementation of LocalQnn using QNN backend HTTP server with SSE streaming.
 */
internal class LocalQnnImpl(
    private val context: Context,
    private val httpClient: OkHttpClient,
    private val gson: Gson,
    private val preferenceManager: PreferenceManager,
    private val fileProviderDescriptor: FileProviderDescriptor
) : LocalQnn {

    companion object {
        private const val TAG = "LocalQnnImpl"
        private const val BASE_URL = "http://127.0.0.1:8081"
        private const val SERVER_STARTUP_TIMEOUT_MS = 30000L
        private const val HEALTH_CHECK_INTERVAL_MS = 500L
    }

    private val statusSubject: PublishSubject<LocalDiffusionStatus> = PublishSubject.create()

    // Track current server state to restart if changed
    @Volatile
    private var currentServerWidth: Int = 0
    @Volatile
    private var currentServerHeight: Int = 0
    @Volatile
    private var currentServerModelId: String = ""

    override fun processTextToImage(payload: TextToImagePayload): Single<QnnGenerationResult> {
        // Check if Hires.Fix is enabled and we're on NPU
        val useHires = payload.qnnHires.enabled && !preferenceManager.localQnnRunOnCpu

        return if (useHires) {
            processTextToImageWithHires(payload)
        } else {
            processTextToImageDirect(payload)
        }
    }

    /**
     * Direct text-to-image generation without Hires.Fix.
     */
    private fun processTextToImageDirect(payload: TextToImagePayload): Single<QnnGenerationResult> {
        return ensureServerRunning(payload.width, payload.height)
            .andThen(Single.create<QnnGenerationResult> { emitter ->
                try {
                    val scheduler = mapSamplerToQnnScheduler(payload.sampler)
                    val request = GenerateRequest(
                        prompt = payload.prompt,
                        negativePrompt = payload.negativePrompt,
                        width = payload.width,
                        height = payload.height,
                        steps = payload.samplingSteps,
                        cfg = payload.cfgScale,
                        seed = parseSeed(payload.seed),
                        scheduler = scheduler,
                        useOpencl = preferenceManager.localQnnUseOpenCL,
                        showDiffusionProcess = preferenceManager.localQnnShowDiffusionProcess,
                        showDiffusionStride = 1
                    )

                    val result = executeGenerateRequest(request)
                    emitter.onSuccess(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Text-to-image error", e)
                    emitter.onError(e)
                }
            })
            .subscribeOn(Schedulers.io())
    }

    /**
     * Text-to-image with Hires.Fix (NPU only):
     * 1. Generate at base resolution (from payload width/height)
     * 2. Upscale to target resolution (with same aspect ratio)
     * 3. Run img2img refinement pass at target resolution
     *
     * Supported upscale paths:
     * - 512×512 → 768×768, 1024×1024
     * - 512×768 → 768×1024
     * - 768×512 → 1024×768
     * - 768×768 → 1024×1024
     */
    private fun processTextToImageWithHires(payload: TextToImagePayload): Single<QnnGenerationResult> {
        // Base resolution is from payload (user selected)
        val baseWidth = payload.width
        val baseHeight = payload.height
        val targetWidth = payload.qnnHires.targetWidth
        val targetHeight = payload.qnnHires.targetHeight
        val hiresSteps = if (payload.qnnHires.steps > 0) payload.qnnHires.steps else payload.samplingSteps
        val hiresDenoise = payload.qnnHires.denoisingStrength

        Log.i(TAG, "Hires.Fix: ${baseWidth}x${baseHeight} → ${targetWidth}x${targetHeight}, " +
                "steps=$hiresSteps, denoise=$hiresDenoise")

        // Step 1: Generate at base resolution
        return ensureServerRunning(baseWidth, baseHeight)
            .andThen(Single.create<QnnGenerationResult> { emitter ->
                try {
                    val scheduler = mapSamplerToQnnScheduler(payload.sampler)
                    val request = GenerateRequest(
                        prompt = payload.prompt,
                        negativePrompt = payload.negativePrompt,
                        width = baseWidth,
                        height = baseHeight,
                        steps = payload.samplingSteps,
                        cfg = payload.cfgScale,
                        seed = parseSeed(payload.seed),
                        scheduler = scheduler,
                        useOpencl = false, // NPU mode
                        showDiffusionProcess = preferenceManager.localQnnShowDiffusionProcess,
                        showDiffusionStride = 1
                    )

                    Log.d(TAG, "Hires Step 1: Generating base image at ${baseWidth}x${baseHeight}")
                    val baseResult = executeGenerateRequest(request)
                    emitter.onSuccess(baseResult)
                } catch (e: Exception) {
                    Log.e(TAG, "Hires Step 1 error", e)
                    emitter.onError(e)
                }
            })
            .subscribeOn(Schedulers.io())
            // Step 2: Upscale and refine
            .flatMap { baseResult ->
                Log.d(TAG, "Hires Step 2: Upscaling ${baseWidth}x${baseHeight} → ${targetWidth}x${targetHeight}")

                // Upscale bitmap to target resolution
                val upscaledBitmap = Bitmap.createScaledBitmap(
                    baseResult.bitmap, targetWidth, targetHeight, true
                )
                val upscaledBase64 = bitmapToBase64NoWrap(upscaledBitmap)

                // Restart server at target resolution
                ensureServerRunning(targetWidth, targetHeight)
                    .andThen(Single.create<QnnGenerationResult> { emitter ->
                        try {
                            val scheduler = mapSamplerToQnnScheduler(payload.sampler)
                            val request = GenerateRequest(
                                prompt = payload.prompt,
                                negativePrompt = payload.negativePrompt,
                                width = targetWidth,
                                height = targetHeight,
                                steps = hiresSteps,
                                cfg = payload.cfgScale,
                                seed = baseResult.seed, // Use same seed for consistency
                                scheduler = scheduler,
                                useOpencl = false, // NPU mode
                                showDiffusionProcess = preferenceManager.localQnnShowDiffusionProcess,
                                showDiffusionStride = 1,
                                image = upscaledBase64,
                                denoiseStrength = hiresDenoise
                            )

                            Log.d(TAG, "Hires Step 3: Refining at ${targetWidth}x${targetHeight}")
                            val refinedResult = executeGenerateRequest(request)
                            emitter.onSuccess(refinedResult)
                        } catch (e: Exception) {
                            Log.e(TAG, "Hires Step 3 error", e)
                            emitter.onError(e)
                        }
                    })
            }
    }

    override fun processImageToImage(payload: ImageToImagePayload): Single<QnnGenerationResult> {
        // Check if we need to use "Only masked" mode
        val hasMask = payload.base64MaskImage.isNotEmpty()
        val useOnlyMasked = hasMask && payload.inPaintFullRes

        return if (useOnlyMasked) {
            processImageToImageOnlyMasked(payload)
        } else {
            processImageToImageWholePicture(payload)
        }
    }

    /**
     * Process img2img with "Whole picture" mode - sends full image to QNN.
     */
    private fun processImageToImageWholePicture(payload: ImageToImagePayload): Single<QnnGenerationResult> {
        return ensureServerRunning(payload.width, payload.height)
            .andThen(Single.create<QnnGenerationResult> { emitter ->
                try {
                    val scheduler = mapSamplerToQnnScheduler(payload.sampler)
                    // Remove line breaks from base64 (Android Base64.DEFAULT adds them)
                    val cleanImage = payload.base64Image.replace("\n", "").replace("\r", "")
                    val cleanMask = payload.base64MaskImage.replace("\n", "").replace("\r", "")
                    val request = GenerateRequest(
                        prompt = payload.prompt,
                        negativePrompt = payload.negativePrompt,
                        width = payload.width,
                        height = payload.height,
                        steps = payload.samplingSteps,
                        cfg = payload.cfgScale,
                        seed = parseSeed(payload.seed),
                        scheduler = scheduler,
                        useOpencl = preferenceManager.localQnnUseOpenCL,
                        showDiffusionProcess = preferenceManager.localQnnShowDiffusionProcess,
                        showDiffusionStride = 1,
                        image = cleanImage,
                        mask = cleanMask.takeIf { it.isNotEmpty() },
                        denoiseStrength = payload.denoisingStrength
                    )

                    val result = executeGenerateRequest(request)
                    emitter.onSuccess(result)
                } catch (e: Exception) {
                    Log.e(TAG, "Image-to-image error", e)
                    emitter.onError(e)
                }
            })
            .subscribeOn(Schedulers.io())
    }

    /**
     * Process img2img with "Only masked" mode:
     * 1. Find bounding box of mask
     * 2. Crop masked area with padding
     * 3. Scale to target resolution
     * 4. Generate with QNN
     * 5. Scale result back
     * 6. Composite onto original image
     */
    private fun processImageToImageOnlyMasked(payload: ImageToImagePayload): Single<QnnGenerationResult> {
        return Single.fromCallable {
            // Decode original image and mask
            val originalBitmap = base64ToBitmap(payload.base64Image)
            val maskBitmap = base64ToBitmap(payload.base64MaskImage)

            // Find bounding box of the mask (white pixels)
            val maskBounds = findMaskBounds(maskBitmap, payload.inPaintFullResPadding)

            // Crop the region from original image
            val croppedImage = Bitmap.createBitmap(
                originalBitmap,
                maskBounds.left, maskBounds.top,
                maskBounds.width(), maskBounds.height()
            )

            // Crop corresponding mask region
            val croppedMask = Bitmap.createBitmap(
                maskBitmap,
                maskBounds.left, maskBounds.top,
                maskBounds.width(), maskBounds.height()
            )

            // Scale to target resolution
            val scaledImage = Bitmap.createScaledBitmap(croppedImage, payload.width, payload.height, true)
            val scaledMask = Bitmap.createScaledBitmap(croppedMask, payload.width, payload.height, true)

            InpaintOnlyMaskedData(
                originalBitmap = originalBitmap,
                maskBitmap = maskBitmap,
                scaledImage = scaledImage,
                scaledMask = scaledMask,
                maskBounds = maskBounds
            )
        }
        .subscribeOn(Schedulers.io())
        .flatMap { data ->
            // Run generation on the cropped/scaled region
            ensureServerRunning(payload.width, payload.height)
                .andThen(Single.create<QnnGenerationResult> { emitter ->
                    try {
                        val scheduler = mapSamplerToQnnScheduler(payload.sampler)
                        val cleanImage = bitmapToBase64NoWrap(data.scaledImage)
                        val cleanMask = bitmapToBase64NoWrap(data.scaledMask)

                        val request = GenerateRequest(
                            prompt = payload.prompt,
                            negativePrompt = payload.negativePrompt,
                            width = payload.width,
                            height = payload.height,
                            steps = payload.samplingSteps,
                            cfg = payload.cfgScale,
                            seed = parseSeed(payload.seed),
                            scheduler = scheduler,
                            useOpencl = preferenceManager.localQnnUseOpenCL,
                            showDiffusionProcess = preferenceManager.localQnnShowDiffusionProcess,
                            showDiffusionStride = 1,
                            image = cleanImage,
                            mask = cleanMask,
                            denoiseStrength = payload.denoisingStrength
                        )

                        val genResult = executeGenerateRequest(request)

                        // Scale generated result back to crop size
                        val scaledBack = Bitmap.createScaledBitmap(
                            genResult.bitmap,
                            data.maskBounds.width(),
                            data.maskBounds.height(),
                            true
                        )

                        // Composite onto original image
                        val compositedBitmap = compositeWithMask(
                            data.originalBitmap,
                            scaledBack,
                            data.maskBitmap,
                            data.maskBounds
                        )

                        emitter.onSuccess(QnnGenerationResult(
                            bitmap = compositedBitmap,
                            seed = genResult.seed,
                            width = data.originalBitmap.width,
                            height = data.originalBitmap.height,
                        ))
                    } catch (e: Exception) {
                        Log.e(TAG, "Image-to-image (only masked) error", e)
                        emitter.onError(e)
                    }
                })
        }
    }

    private data class InpaintOnlyMaskedData(
        val originalBitmap: Bitmap,
        val maskBitmap: Bitmap,
        val scaledImage: Bitmap,
        val scaledMask: Bitmap,
        val maskBounds: Rect
    )

    /**
     * Find the bounding box of white pixels in mask, with padding.
     */
    private fun findMaskBounds(mask: Bitmap, padding: Int): Rect {
        var minX = mask.width
        var minY = mask.height
        var maxX = 0
        var maxY = 0

        for (y in 0 until mask.height) {
            for (x in 0 until mask.width) {
                val pixel = mask.getPixel(x, y)
                // Check if pixel is "white" (masked area) - check red channel > 128
                if (Color.red(pixel) > 128) {
                    minX = min(minX, x)
                    minY = min(minY, y)
                    maxX = max(maxX, x)
                    maxY = max(maxY, y)
                }
            }
        }

        // If no mask found, use whole image
        if (minX > maxX || minY > maxY) {
            return Rect(0, 0, mask.width, mask.height)
        }

        // Add padding
        minX = max(0, minX - padding)
        minY = max(0, minY - padding)
        maxX = min(mask.width, maxX + padding)
        maxY = min(mask.height, maxY + padding)

        return Rect(minX, minY, maxX, maxY)
    }

    /**
     * Composite generated image onto original using mask.
     */
    private fun compositeWithMask(
        original: Bitmap,
        generated: Bitmap,
        mask: Bitmap,
        bounds: Rect
    ): Bitmap {
        val result = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        // Draw the generated image at the mask bounds
        canvas.drawBitmap(generated, null, bounds, null)

        return result
    }

    private fun base64ToBitmap(base64: String): Bitmap {
        val clean = base64.replace("\n", "").replace("\r", "")
        val bytes = Base64.decode(clean, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun bitmapToBase64NoWrap(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Ensure the QNN backend server is running with correct resolution and model.
     * If running with different resolution or model, restarts the server.
     */
    private fun ensureServerRunning(width: Int, height: Int): Completable {
        val requestedModelId = preferenceManager.localQnnModelId
        return isAvailable()
            .flatMapCompletable { isRunning ->
                val resolutionChanged = isRunning &&
                    (currentServerWidth != width || currentServerHeight != height)
                val modelChanged = isRunning && currentServerModelId != requestedModelId

                when {
                    !isRunning -> {
                        Log.d(TAG, "Server not running, starting with ${width}x${height}...")
                        startServerAndWait(width, height)
                    }
                    resolutionChanged -> {
                        Log.d(TAG, "Resolution changed from ${currentServerWidth}x${currentServerHeight} to ${width}x${height}, restarting server...")
                        stopService()
                            .andThen(Completable.timer(1000, java.util.concurrent.TimeUnit.MILLISECONDS))
                            .andThen(startServerAndWait(width, height))
                    }
                    modelChanged -> {
                        Log.d(TAG, "Model changed from $currentServerModelId to $requestedModelId, restarting server...")
                        stopService()
                            .andThen(Completable.timer(1000, java.util.concurrent.TimeUnit.MILLISECONDS))
                            .andThen(startServerAndWait(width, height))
                    }
                    else -> {
                        Log.d(TAG, "Server already running with ${width}x${height}")
                        Completable.complete()
                    }
                }
            }
    }

    private fun startServerAndWait(width: Int, height: Int): Completable {
        return Completable.create { emitter ->
            try {
                val modelPath = getModelPath()
                val modelId = preferenceManager.localQnnModelId
                val runOnCpu = preferenceManager.localQnnRunOnCpu
                Log.d(TAG, "Starting server with model: $modelPath, resolution: ${width}x${height}, runOnCpu: $runOnCpu")

                val intent = QnnBackendService.createStartIntent(context, modelPath, width, height, runOnCpu)
                context.startForegroundService(intent)

                // Wait for server to be ready
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < SERVER_STARTUP_TIMEOUT_MS) {
                    if (checkServerHealth()) {
                        Log.d(TAG, "Server is ready with resolution ${width}x${height}")
                        currentServerWidth = width
                        currentServerHeight = height
                        currentServerModelId = modelId
                        emitter.onComplete()
                        return@create
                    }
                    Thread.sleep(HEALTH_CHECK_INTERVAL_MS)
                }

                emitter.onError(RuntimeException("Server startup timeout"))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server", e)
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun getModelPath(): String {
        val modelId = preferenceManager.localQnnModelId
        return when {
            // Old single custom model (legacy support)
            modelId == LocalAiModel.CustomQnn.id -> {
                preferenceManager.localQnnCustomModelPath
            }
            // Scanned custom model - ID format is "CUSTOM_QNN:modelName"
            modelId.startsWith("CUSTOM_QNN:") -> {
                val modelName = modelId.removePrefix("CUSTOM_QNN:")
                File(preferenceManager.localQnnCustomModelPath, modelName).absolutePath
            }
            // Downloaded model path
            else -> {
                File(fileProviderDescriptor.localModelDirPath, modelId).absolutePath
            }
        }
    }

    private fun checkServerHealth(): Boolean {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/health")
                .get()
                .build()

            val response = httpClient.newBuilder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build()
                .newCall(request)
                .execute()

            val isOk = response.isSuccessful
            response.close()
            isOk
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Execute generation request using SSE streaming.
     * The native server returns Server-Sent Events with progress and final image.
     */
    private fun executeGenerateRequest(generateRequest: GenerateRequest): QnnGenerationResult {
        val jsonBody = gson.toJson(generateRequest)
        Log.d(TAG, "Generate request: $jsonBody")

        val request = Request.Builder()
            .url("$BASE_URL/generate")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .header("Accept", "text/event-stream")
            .build()

        // Use longer timeout for generation
        val client = httpClient.newBuilder()
            .readTimeout(10, TimeUnit.MINUTES)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "No error body"
            Log.e(TAG, "Generate request failed with code ${response.code}: $errorBody")
            throw RuntimeException("Generate request failed: ${response.code} - $errorBody")
        }

        val body = response.body ?: throw RuntimeException("Empty response body")
        val source = body.source()
        var result: QnnGenerationResult? = null
        var eventType: String? = null

        // Read SSE events line by line, blocking until data is available
        try {
            while (true) {
                // readUtf8Line() blocks until a line is available or stream ends
                val line = source.readUtf8Line() ?: break

                when {
                    line.startsWith("event: ") -> {
                        eventType = line.removePrefix("event: ").trim()
                    }
                    line.startsWith("data: ") -> {
                        val eventData = line.removePrefix("data: ").trim()

                        when (eventType) {
                            "progress" -> {
                                try {
                                    val progress = gson.fromJson(eventData, ProgressEvent::class.java)
                                    val previewBitmap = progress.image?.takeIf { it.isNotEmpty() }?.let { base64 ->
                                        try {
                                            // Preview images are typically at generation resolution
                                            decodeBase64Image(base64, generateRequest.width, generateRequest.height)
                                        } catch (e: Exception) {
                                            Log.w(TAG, "Failed to decode preview", e)
                                            null
                                        }
                                    }
                                    statusSubject.onNext(
                                        LocalDiffusionStatus(
                                            current = progress.step,
                                            total = progress.totalSteps,
                                            previewBitmap = previewBitmap
                                        )
                                    )
                                    Log.d(TAG, "Progress: ${progress.step}/${progress.totalSteps}" +
                                            if (previewBitmap != null) " (with preview)" else "")
                                } catch (e: Exception) {
                                    Log.w(TAG, "Failed to parse progress", e)
                                }
                            }
                            "complete" -> {
                                try {
                                    val complete = gson.fromJson(eventData, CompleteEvent::class.java)
                                    val bitmap = decodeBase64Image(complete.image, complete.width, complete.height)
                                    result = QnnGenerationResult(
                                        bitmap = bitmap,
                                        seed = complete.seed,
                                        width = complete.width,
                                        height = complete.height,
                                    )
                                    Log.i(TAG, "Generation complete: ${complete.width}x${complete.height}, " +
                                            "time=${complete.generationTimeMs}ms, seed=${complete.seed}")
                                    // Exit loop after receiving complete event
                                    break
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to parse complete event", e)
                                    throw RuntimeException("Failed to decode result image", e)
                                }
                            }
                            "error" -> {
                                Log.e(TAG, "Server error: $eventData")
                                throw RuntimeException("Generation error: $eventData")
                            }
                        }
                    }
                }
            }
        } finally {
            response.close()
        }

        return result ?: throw RuntimeException("No image received from server")
    }

    override fun interrupt(): Completable {
        // Native server doesn't have interrupt endpoint yet
        return Completable.complete()
    }

    override fun observeStatus(): Observable<LocalDiffusionStatus> {
        return statusSubject
    }

    override fun isAvailable(): Single<Boolean> {
        return Single.fromCallable {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/health")
                    .get()
                    .build()

                val response = httpClient.newCall(request).execute()
                val isOk = response.isSuccessful
                response.close()
                isOk
            } catch (e: Exception) {
                Log.d(TAG, "Health check failed: ${e.message}")
                false
            }
        }.subscribeOn(Schedulers.io())
    }

    override fun startService(): Completable {
        return Completable.fromAction {
            // Note: Model path should be provided from preferences
            // This is a placeholder that needs to be called with proper model path
            Log.w(TAG, "startService called without model path - use startServiceWithModel instead")
        }
    }

    fun startServiceWithModel(modelPath: String, width: Int = 512, height: Int = 512): Completable {
        return Completable.fromAction {
            val intent = QnnBackendService.createStartIntent(context, modelPath, width, height)
            context.startForegroundService(intent)
        }
    }

    override fun stopService(): Completable {
        return Completable.fromAction {
            currentServerWidth = 0
            currentServerHeight = 0
            val intent = QnnBackendService.createStopIntent(context)
            context.startService(intent)
        }
    }

    /**
     * Parse seed string to Long.
     * Returns null for random seed (empty string, -1, or invalid).
     * Server will generate random seed when null/omitted.
     */
    private fun parseSeed(seed: String): Long? {
        if (seed.isBlank()) return null
        return try {
            val value = seed.toLong()
            if (value < 0) null else value
        } catch (e: NumberFormatException) {
            null
        }
    }

    /**
     * Map A1111-style sampler names to QNN scheduler types.
     * QNN backend supports: "dpm" (DPM++ 2M), "euler_a" (Euler Ancestral)
     */
    private fun mapSamplerToQnnScheduler(sampler: String): String {
        return when {
            sampler.lowercase().contains("euler") -> "euler_a"
            sampler.lowercase().contains("dpm") -> "dpm"
            sampler.lowercase().contains("ddim") -> "dpm" // Fallback
            else -> preferenceManager.localQnnScheduler.ifEmpty { "dpm" }
        }
    }

    private fun decodeBase64Image(base64: String, width: Int, height: Int): Bitmap {
        val imageData = Base64.decode(base64, Base64.DEFAULT)

        // First try standard image formats (PNG, JPEG)
        val standardBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        if (standardBitmap != null) {
            return standardBitmap
        }

        // Server sends raw RGB data (width * height * 3 bytes)
        val expectedSize = width * height * 3
        if (imageData.size != expectedSize) {
            throw IllegalStateException(
                "Failed to decode image. Size mismatch: got ${imageData.size}, expected $expectedSize for ${width}x${height}x3"
            )
        }

        // Convert RGB bytes to ARGB_8888 Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)

        for (i in 0 until width * height) {
            val r = imageData[i * 3].toInt() and 0xFF
            val g = imageData[i * 3 + 1].toInt() and 0xFF
            val b = imageData[i * 3 + 2].toInt() and 0xFF
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
