package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.reactivex.rxjava3.core.Single
import java.io.File

class ScanCustomModelsUseCaseImpl(
    private val preferenceManager: PreferenceManager,
) : ScanCustomModelsUseCase {

    override fun invoke(type: LocalAiModel.Type): Single<List<LocalAiModel>> = Single.create { emitter ->
        try {
            val basePath = when (type) {
                LocalAiModel.Type.ONNX -> preferenceManager.localOnnxCustomModelPath
                LocalAiModel.Type.MediaPipe -> preferenceManager.localMediaPipeCustomModelPath
                LocalAiModel.Type.QNN -> preferenceManager.localQnnCustomModelPath
            }

            val baseDir = File(basePath)
            if (!baseDir.exists() || !baseDir.isDirectory) {
                emitter.onSuccess(emptyList())
                return@create
            }

            val models = baseDir.listFiles()
                ?.filter { it.isDirectory }
                ?.filter { dir -> isValidModelDirectory(dir, type) }
                ?.map { dir ->
                    LocalAiModel(
                        id = "${getCustomPrefix(type)}:${dir.name}",
                        type = type,
                        name = dir.name,
                        size = formatSize(calculateDirSize(dir)),
                        sources = emptyList(),
                        downloaded = true,
                        selected = false,
                    )
                }
                ?: emptyList()

            emitter.onSuccess(models)
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }

    private fun getCustomPrefix(type: LocalAiModel.Type): String = when (type) {
        LocalAiModel.Type.ONNX -> "CUSTOM_ONNX"
        LocalAiModel.Type.MediaPipe -> "CUSTOM_MP"
        LocalAiModel.Type.QNN -> "CUSTOM_QNN"
    }

    private fun isValidModelDirectory(dir: File, type: LocalAiModel.Type): Boolean {
        val files = dir.listFiles()?.map { it.name } ?: return false

        return when (type) {
            LocalAiModel.Type.ONNX -> {
                // ONNX requires: text_encoder, unet, vae_decoder, vae_encoder folders
                // or *.onnx files
                files.any { it.endsWith(".onnx") } ||
                    (files.contains("text_encoder") &&
                     files.contains("unet") &&
                     files.contains("vae_decoder"))
            }
            LocalAiModel.Type.MediaPipe -> {
                // MediaPipe requires .tflite files
                files.any { it.endsWith(".tflite") || it.endsWith(".bin") }
            }
            LocalAiModel.Type.QNN -> {
                // QNN requires: clip.bin/mnn, unet.bin/mnn, vae_decoder.bin/mnn, tokenizer.json
                val hasClip = files.any { it.startsWith("clip.") }
                val hasUnet = files.any { it.startsWith("unet.") }
                val hasVae = files.any { it.startsWith("vae_decoder.") || it.startsWith("vae.") }
                val hasTokenizer = files.contains("tokenizer.json")

                (hasClip && hasUnet && hasVae) || hasTokenizer
            }
        }
    }

    private fun calculateDirSize(dir: File): Long {
        return dir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }

    private fun formatSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> String.format("%.2f GB", gb)
            mb >= 1.0 -> String.format("%.2f MB", mb)
            else -> String.format("%.2f KB", kb)
        }
    }
}
