package com.shifthackz.aisdv1.feature.qnn.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Manages QNN libraries and models.
 *
 * Responsibilities:
 * - Copy QNN .so files from assets to app's private directory
 * - Copy base MNN models from assets
 * - Manage downloaded SD models
 * - Track model conversion state
 */
class QnnModelManager(
    private val context: Context
) {

    private val filesDir = context.filesDir
    private val qnnLibsDir = File(filesDir, "qnn_libs")
    private val modelsDir = File(filesDir, "qnn_models")
    private val cvtbaseDir = File(filesDir, "cvtbase")

    /**
     * Prepare QNN libraries by copying from assets to private directory.
     * Libraries need to be in a directory accessible at runtime.
     */
    suspend fun prepareLibraries(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            qnnLibsDir.mkdirs()
            cvtbaseDir.mkdirs()
            modelsDir.mkdirs()

            // Copy QNN libraries
            copyAssetsDirectory("qnnlibs", qnnLibsDir)

            // Copy base models
            copyAssetsDirectory("cvtbase", cvtbaseDir)
        }
    }

    /**
     * Check if QNN libraries are prepared.
     */
    fun areLibrariesReady(): Boolean {
        val requiredLibs = listOf("libQnnHtp.so", "libQnnSystem.so")
        return requiredLibs.all { File(qnnLibsDir, it).exists() }
    }

    /**
     * Get path to QNN libraries directory.
     */
    fun getQnnLibsPath(): String = qnnLibsDir.absolutePath

    /**
     * Get path to base models directory.
     */
    fun getModelsPath(): String = cvtbaseDir.absolutePath

    /**
     * Get path to downloaded models directory.
     */
    fun getDownloadedModelsPath(): String = modelsDir.absolutePath

    /**
     * Get list of available models.
     */
    fun getAvailableModels(): List<QnnModel> {
        return modelsDir.listFiles()?.filter { it.isDirectory }?.map { dir ->
            QnnModel(
                id = dir.name,
                name = dir.name,
                path = dir.absolutePath,
                isConverted = File(dir, "unet.qnn").exists(),
                sizeBytes = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
            )
        } ?: emptyList()
    }

    /**
     * Check if device supports QNN HTP (NPU).
     */
    fun checkQnnSupport(): QnnSupportStatus {
        // Check for Qualcomm SoC
        val socModel = try {
            File("/sys/devices/soc0/soc_id").readText().trim()
        } catch (e: Exception) {
            null
        }

        val supportedSocs = mapOf(
            "457" to "SM8450", // Snapdragon 8 Gen 1
            "530" to "SM8475", // Snapdragon 8+ Gen 1
            "536" to "SM8550", // Snapdragon 8 Gen 2
            "557" to "SM8650", // Snapdragon 8 Gen 3
            "614" to "SM8750"  // Snapdragon 8 Elite
        )

        val socName = supportedSocs[socModel]
        return if (socName != null) {
            QnnSupportStatus.Supported(socName)
        } else {
            QnnSupportStatus.Unsupported(
                "Device SoC not supported. QNN HTP requires Snapdragon 8 Gen 1 or newer."
            )
        }
    }

    /**
     * Delete a model.
     */
    suspend fun deleteModel(modelId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val modelDir = File(modelsDir, modelId)
            if (modelDir.exists()) {
                modelDir.deleteRecursively()
            }
        }
    }

    private fun copyAssetsDirectory(assetPath: String, targetDir: File) {
        val assetManager = context.assets
        val files = assetManager.list(assetPath) ?: return

        for (filename in files) {
            if (filename == ".gitkeep") continue

            val assetFilePath = "$assetPath/$filename"
            val targetFile = File(targetDir, filename)

            try {
                // Check if it's a directory
                val subFiles = assetManager.list(assetFilePath)
                if (subFiles != null && subFiles.isNotEmpty()) {
                    targetFile.mkdirs()
                    copyAssetsDirectory(assetFilePath, targetFile)
                } else {
                    // It's a file
                    if (!targetFile.exists() || targetFile.length() == 0L) {
                        assetManager.open(assetFilePath).use { input ->
                            FileOutputStream(targetFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

/**
 * Represents a Stable Diffusion model for QNN.
 */
data class QnnModel(
    val id: String,
    val name: String,
    val path: String,
    val isConverted: Boolean,
    val sizeBytes: Long
)

/**
 * QNN support status for the device.
 */
sealed class QnnSupportStatus {
    data class Supported(val socName: String) : QnnSupportStatus()
    data class Unsupported(val reason: String) : QnnSupportStatus()
}
