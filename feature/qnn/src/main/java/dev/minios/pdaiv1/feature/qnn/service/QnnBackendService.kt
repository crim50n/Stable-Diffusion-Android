package dev.minios.pdaiv1.feature.qnn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

/**
 * Foreground service that manages the QNN backend server process.
 *
 * The service:
 * 1. Copies QNN libraries from assets to private directory
 * 2. Starts native executable (libstable_diffusion_core.so) as a process
 * 3. Monitors process output and health
 * 4. Keeps running until explicitly stopped
 *
 * Based on local-dream's BackendService implementation.
 */
class QnnBackendService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var backendProcess: Process? = null
    private lateinit var runtimeDir: File

    companion object {
        private const val TAG = "QnnBackendService"
        private const val NOTIFICATION_CHANNEL_ID = "qnn_backend_channel"
        private const val NOTIFICATION_ID = 9081
        private const val EXECUTABLE_NAME = "libstable_diffusion_core.so"
        private const val RUNTIME_DIR = "qnn_runtime"
        private const val SERVER_PORT = 8081

        const val ACTION_START = "dev.minios.pdaiv1.feature.qnn.START"
        const val ACTION_STOP = "dev.minios.pdaiv1.feature.qnn.STOP"
        const val EXTRA_MODEL_PATH = "model_path"
        const val EXTRA_WIDTH = "width"
        const val EXTRA_HEIGHT = "height"
        const val EXTRA_RUN_ON_CPU = "run_on_cpu"

        private object StateHolder {
            val _backendState = MutableStateFlow<BackendState>(BackendState.Idle)
        }

        val backendState: StateFlow<BackendState> = StateHolder._backendState

        private fun updateState(state: BackendState) {
            StateHolder._backendState.value = state
        }

        fun createStartIntent(
            context: Context,
            modelPath: String,
            width: Int = 512,
            height: Int = 512,
            runOnCpu: Boolean = false
        ): Intent {
            return Intent(context, QnnBackendService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MODEL_PATH, modelPath)
                putExtra(EXTRA_WIDTH, width)
                putExtra(EXTRA_HEIGHT, height)
                putExtra(EXTRA_RUN_ON_CPU, runOnCpu)
            }
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, QnnBackendService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }

    sealed class BackendState {
        object Idle : BackendState()
        object Starting : BackendState()
        object Running : BackendState()
        data class Error(val message: String) : BackendState()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        prepareRuntimeDir()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started command: ${intent?.action}")
        startForeground(NOTIFICATION_ID, createNotification("Initializing..."))

        when (intent?.action) {
            ACTION_START -> {
                val modelPath = intent.getStringExtra(EXTRA_MODEL_PATH)
                val width = intent.getIntExtra(EXTRA_WIDTH, 512)
                val height = intent.getIntExtra(EXTRA_HEIGHT, 512)
                val runOnCpu = intent.getBooleanExtra(EXTRA_RUN_ON_CPU, false)
                if (modelPath != null) {
                    startBackend(modelPath, width, height, runOnCpu)
                } else {
                    updateState(BackendState.Error("Model path not provided"))
                    stopSelf()
                }
            }
            ACTION_STOP -> {
                stopBackend()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopBackend()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun prepareRuntimeDir() {
        try {
            runtimeDir = File(filesDir, RUNTIME_DIR).apply {
                if (!exists()) mkdirs()
            }

            // Copy QNN libraries from assets
            val qnnLibsAssets = assets.list("qnnlibs")
            qnnLibsAssets?.forEach { fileName ->
                if (fileName == ".gitkeep") return@forEach
                val targetLib = File(runtimeDir, fileName)

                val needsCopy = !targetLib.exists() || run {
                    val assetInputStream = assets.open("qnnlibs/$fileName")
                    val assetSize = assetInputStream.use { it.available().toLong() }
                    targetLib.length() != assetSize
                }

                if (needsCopy) {
                    assets.open("qnnlibs/$fileName").use { input ->
                        FileOutputStream(targetLib).use { output ->
                            input.copyTo(output)
                        }
                    }
                    Log.d(TAG, "Copied $fileName to runtime directory")
                }

                targetLib.setReadable(true, true)
                targetLib.setExecutable(true, true)
            }

            runtimeDir.setReadable(true, true)
            runtimeDir.setExecutable(true, true)

            Log.i(TAG, "Runtime directory prepared: ${runtimeDir.absolutePath}")
            Log.i(TAG, "Runtime files: ${runtimeDir.list()?.joinToString()}")

        } catch (e: Exception) {
            Log.e(TAG, "Prepare runtime dir failed", e)
            updateState(BackendState.Error("Prepare runtime dir failed: ${e.message}"))
            throw RuntimeException("Failed to prepare runtime directory", e)
        }
    }

    private fun startBackend(modelPath: String, width: Int, height: Int, runOnCpu: Boolean) {
        Log.i(TAG, "Backend start, model: $modelPath, resolution: ${width}×${height}, CPU mode: $runOnCpu")
        updateState(BackendState.Starting)
        updateNotification(if (runOnCpu) "Starting CPU backend..." else "Starting NPU backend...")

        serviceScope.launch {
            try {
                val nativeDir = applicationInfo.nativeLibraryDir
                val modelsDir = findModelDirectory(File(modelPath))
                val executableFile = File(nativeDir, EXECUTABLE_NAME)

                if (modelsDir == null) {
                    Log.e(TAG, "Model directory not found: $modelPath")
                    updateState(BackendState.Error("Model not found"))
                    updateNotification("Error: Model not found")
                    return@launch
                }

                Log.i(TAG, "Using model directory: ${modelsDir.absolutePath}")

                // Auto-detect CPU/NPU mode based on model files
                // If model has unet.bin -> NPU mode, if only unet.mnn -> CPU mode
                val hasNpuModel = File(modelsDir, "unet.bin").exists()
                val hasMnnModel = File(modelsDir, "unet.mnn").exists()
                val actualRunOnCpu = if (hasNpuModel) runOnCpu else hasMnnModel

                if (actualRunOnCpu != runOnCpu) {
                    Log.i(TAG, "Auto-switching to ${if (actualRunOnCpu) "CPU" else "NPU"} mode based on model files")
                }

                if (!executableFile.exists()) {
                    Log.e(TAG, "Executable not found: ${executableFile.absolutePath}")
                    updateState(BackendState.Error("Executable not found"))
                    updateNotification("Error: Executable not found")
                    return@launch
                }

                val command: MutableList<String>

                if (actualRunOnCpu) {
                    // CPU mode - use MNN models
                    // Binary expects clip.mnn path and auto-upgrades to clip_v2.mnn if found
                    // IMPORTANT: We must pass clip.mnn path, not clip_v2.mnn, because the binary
                    // only sets use_clip_v2=true when it detects clip.mnn path and finds clip_v2.mnn
                    val clipMnnFile = File(modelsDir, "clip.mnn")
                    val clipV2MnnFile = File(modelsDir, "clip_v2.mnn")
                    val unetMnnFile = File(modelsDir, "unet.mnn")
                    val vaeDecoderMnnFile = File(modelsDir, "vae_decoder.mnn")
                    val tokenizerFile = File(modelsDir, "tokenizer.json")

                    val hasClip = clipMnnFile.exists() || clipV2MnnFile.exists()
                    if (!hasClip || !unetMnnFile.exists() || !vaeDecoderMnnFile.exists()) {
                        Log.e(TAG, "MNN model files missing for CPU mode")
                        updateState(BackendState.Error("CPU model files missing"))
                        updateNotification("Error: CPU model files missing")
                        return@launch
                    }

                    // Always pass clip.mnn path - binary auto-detects clip_v2.mnn and sets use_clip_v2=true
                    // If we pass clip_v2.mnn directly, use_clip_v2 stays false and causes crash!
                    val clipPath = clipMnnFile.absolutePath

                    command = mutableListOf(
                        executableFile.absolutePath,
                        "--clip", clipPath,
                        "--unet", unetMnnFile.absolutePath,
                        "--vae_decoder", vaeDecoderMnnFile.absolutePath,
                        "--tokenizer", tokenizerFile.absolutePath,
                        "--port", SERVER_PORT.toString(),
                        "--text_embedding_size", "768",
                        "--cpu"
                    )

                    // Add VAE encoder if exists (for img2img)
                    val vaeEncoderMnnFile = File(modelsDir, "vae_encoder.mnn")
                    if (vaeEncoderMnnFile.exists()) {
                        command.addAll(listOf("--vae_encoder", vaeEncoderMnnFile.absolutePath))
                    }
                } else {
                    // NPU mode - use QNN models
                    // Note: The binary automatically upgrades from clip.mnn to clip_v2.mnn if found
                    // So we always pass clip.mnn path, and binary will find clip_v2.mnn
                    val clipBinFile = File(modelsDir, "clip.bin")
                    val clipMnnFile = File(modelsDir, "clip.mnn")  // Binary expects this path
                    val clipV2MnnFile = File(modelsDir, "clip_v2.mnn")

                    val unetFile = File(modelsDir, "unet.bin")
                    val vaeDecoderFile = File(modelsDir, "vae_decoder.bin")
                    val tokenizerFile = File(modelsDir, "tokenizer.json")

                    // Check if we have clip (binary will auto-upgrade from clip.mnn path to clip_v2.mnn)
                    val hasClip = clipBinFile.exists() || clipV2MnnFile.exists() || clipMnnFile.exists()

                    if (!hasClip || !unetFile.exists() || !vaeDecoderFile.exists()) {
                        Log.e(TAG, "NPU model files missing in: ${modelsDir.absolutePath}")
                        updateState(BackendState.Error("NPU model files missing"))
                        updateNotification("Error: NPU model files missing")
                        return@launch
                    }

                    // Determine clip path: use clip.bin if exists, otherwise clip.mnn (binary auto-upgrades to clip_v2.mnn)
                    val clipPath = if (clipBinFile.exists()) clipBinFile.absolutePath else clipMnnFile.absolutePath
                    val useCpuClip = !clipBinFile.exists() && (clipV2MnnFile.exists() || clipMnnFile.exists())

                    command = mutableListOf(
                        executableFile.absolutePath,
                        "--clip", clipPath,
                        "--unet", unetFile.absolutePath,
                        "--vae_decoder", vaeDecoderFile.absolutePath,
                        "--tokenizer", tokenizerFile.absolutePath,
                        "--backend", File(runtimeDir, "libQnnHtp.so").absolutePath,
                        "--system_library", File(runtimeDir, "libQnnSystem.so").absolutePath,
                        "--port", SERVER_PORT.toString(),
                        "--text_embedding_size", "768"
                    )

                    // Add VAE encoder if exists (for img2img)
                    val vaeEncoderFile = File(modelsDir, "vae_encoder.bin")
                    if (vaeEncoderFile.exists()) {
                        command.addAll(listOf("--vae_encoder", vaeEncoderFile.absolutePath))
                    }

                    // Add patch file if resolution differs from 512
                    if (width != 512 || height != 512) {
                        val patchFile = if (width == height) {
                            val squarePatch = File(modelsDir, "${width}.patch")
                            if (squarePatch.exists()) squarePatch else File(modelsDir, "${width}x${height}.patch")
                        } else {
                            File(modelsDir, "${width}x${height}.patch")
                        }

                        if (patchFile.exists()) {
                            command.addAll(listOf("--patch", patchFile.absolutePath))
                            Log.i(TAG, "Using patch file: ${patchFile.name}")
                        } else {
                            Log.w(TAG, "Patch file not found: ${patchFile.absolutePath}, using 512×512")
                        }
                    }

                    // Add CPU clip flag if using MNN clip model
                    if (useCpuClip) {
                        command.add("--use_cpu_clip")
                    }
                }

                // Set up environment
                val env = mutableMapOf<String, String>()
                val systemLibPaths = mutableListOf(
                    runtimeDir.absolutePath,
                    "/system/lib64",
                    "/vendor/lib64",
                    "/vendor/lib64/egl"
                )
                env["LD_LIBRARY_PATH"] = systemLibPaths.joinToString(":")
                env["DSP_LIBRARY_PATH"] = runtimeDir.absolutePath

                Log.d(TAG, "COMMAND: ${command.joinToString(" ")}")
                Log.d(TAG, "LD_LIBRARY_PATH=${env["LD_LIBRARY_PATH"]}")
                Log.d(TAG, "DSP_LIBRARY_PATH=${env["DSP_LIBRARY_PATH"]}")

                val processBuilder = ProcessBuilder(command).apply {
                    directory(File(nativeDir))
                    redirectErrorStream(true)
                    environment().putAll(env)
                }

                backendProcess = processBuilder.start()
                startMonitorThread()

                // Wait for server to be ready
                delay(2000)
                updateState(BackendState.Running)
                updateNotification(if (runOnCpu) "Running on CPU (port $SERVER_PORT)" else "Running on NPU (port $SERVER_PORT)")

            } catch (e: Exception) {
                Log.e(TAG, "Backend start failed", e)
                updateState(BackendState.Error("Backend start failed: ${e.message}"))
                updateNotification("Error: ${e.message}")
            }
        }
    }

    private fun startMonitorThread() {
        Thread {
            try {
                backendProcess?.let { proc ->
                    proc.inputStream.bufferedReader().use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            Log.i(TAG, "Backend: $line")
                        }
                    }

                    val exitCode = proc.waitFor()
                    Log.i(TAG, "Backend process exited with code: $exitCode")
                    updateState(BackendState.Error("Process exited: $exitCode"))
                    updateNotification("Process exited: $exitCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Monitor error", e)
                updateState(BackendState.Error("Monitor error: ${e.message}"))
            }
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun stopBackend() {
        Log.i(TAG, "Stopping backend")
        backendProcess?.let { proc ->
            try {
                proc.destroy()
                if (!proc.waitFor(5, TimeUnit.SECONDS)) {
                    proc.destroyForcibly()
                }
                Log.i(TAG, "Process ended, code: ${proc.exitValue()}")
                updateState(BackendState.Idle)
            } catch (e: Exception) {
                Log.e(TAG, "Stop error", e)
                updateState(BackendState.Error("Stop error: ${e.message}"))
            } finally {
                backendProcess = null
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "QNN Backend Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Qualcomm NPU inference service"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(status: String): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("QNN Backend")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                PendingIntent.getService(
                    this,
                    0,
                    createStopIntent(this),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    private fun updateNotification(status: String) {
        val notification = createNotification(status)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Find the actual model directory containing the model files.
     * ZIP archives from HuggingFace may have nested structure like:
     * output_512/qnn_models_8gen2/clip.bin
     *
     * This function recursively searches for a directory containing unet.bin
     */
    private fun extractZipFile(zipFile: File, destinationDir: File) {
        ZipFile(zipFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { inputStream ->
                    val filePath = File(destinationDir, entry.name)
                    if (!entry.isDirectory) {
                        // Create parent directories if they don't exist
                        filePath.parentFile?.mkdirs()
                        BufferedOutputStream(FileOutputStream(filePath)).use { bos ->
                            val bytesIn = ByteArray(DEFAULT_BUFFER_SIZE)
                            var read: Int
                            while (inputStream.read(bytesIn).also { read = it } != -1) {
                                bos.write(bytesIn, 0, read)
                            }
                        }
                    } else {
                        filePath.mkdirs()
                    }
                }
            }
        }
    }

    private fun findModelDirectory(baseDir: File): File? {
        if (!baseDir.exists() || !baseDir.isDirectory) {
            return null
        }

        // Check for model.zip and extract if present
        val zipFile = File(baseDir, "model.zip")
        if (zipFile.exists()) {
            try {
                Log.i(TAG, "Found model.zip (${zipFile.length()} bytes), extracting...")
                extractZipFile(zipFile, baseDir)
                zipFile.delete()
                Log.i(TAG, "Model extracted and zip deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to extract model.zip", e)
                // Delete corrupted zip file so user can re-download
                try {
                    zipFile.delete()
                    Log.w(TAG, "Deleted corrupted model.zip")
                } catch (deleteError: Exception) {
                    Log.e(TAG, "Failed to delete corrupted zip", deleteError)
                }
                return null
            }
        }

        // Check if model files exist directly in this directory
        // For QNN (NPU): unet.bin, vae_decoder.bin
        // For MNN (CPU): unet.mnn, vae_decoder.mnn
        val hasQnnUnet = File(baseDir, "unet.bin").exists()
        val hasQnnVaeDecoder = File(baseDir, "vae_decoder.bin").exists()
        val hasMnnUnet = File(baseDir, "unet.mnn").exists()
        val hasMnnVaeDecoder = File(baseDir, "vae_decoder.mnn").exists()
        val hasTokenizer = File(baseDir, "tokenizer.json").exists()

        val hasQnnModel = hasQnnUnet && hasQnnVaeDecoder && hasTokenizer
        val hasMnnModel = hasMnnUnet && hasMnnVaeDecoder && hasTokenizer

        if (hasQnnModel || hasMnnModel) {
            return baseDir
        }

        // Recursively search subdirectories (max depth 3)
        return findModelDirectoryRecursive(baseDir, 0, 3)
    }

    private fun findModelDirectoryRecursive(dir: File, depth: Int, maxDepth: Int): File? {
        if (depth >= maxDepth) return null

        val subdirs = dir.listFiles { file -> file.isDirectory } ?: return null

        for (subdir in subdirs) {
            // For QNN (NPU): unet.bin, vae_decoder.bin
            // For MNN (CPU): unet.mnn, vae_decoder.mnn
            val hasQnnUnet = File(subdir, "unet.bin").exists()
            val hasQnnVaeDecoder = File(subdir, "vae_decoder.bin").exists()
            val hasMnnUnet = File(subdir, "unet.mnn").exists()
            val hasMnnVaeDecoder = File(subdir, "vae_decoder.mnn").exists()
            val hasTokenizer = File(subdir, "tokenizer.json").exists()

            val hasQnnModel = hasQnnUnet && hasQnnVaeDecoder && hasTokenizer
            val hasMnnModel = hasMnnUnet && hasMnnVaeDecoder && hasTokenizer

            if (hasQnnModel || hasMnnModel) {
                return subdir
            }

            // Search deeper
            val found = findModelDirectoryRecursive(subdir, depth + 1, maxDepth)
            if (found != null) {
                return found
            }
        }

        return null
    }
}
