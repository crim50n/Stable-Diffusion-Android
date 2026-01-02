package dev.minios.pdaiv1.domain.usecase.downloadable

import dev.minios.pdaiv1.domain.entity.LocalAiModel
import dev.minios.pdaiv1.domain.preference.PreferenceManager
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
                // ONNX requires: text_encoder/model.ort, unet/model.ort, vae_decoder/model.ort
                // and tokenizer/vocab.json, tokenizer/merges.txt
                val hasTextEncoder = File(dir, "text_encoder/model.ort").exists()
                val hasUnet = File(dir, "unet/model.ort").exists()
                val hasVaeDecoder = File(dir, "vae_decoder/model.ort").exists()
                val hasVocab = File(dir, "tokenizer/vocab.json").exists()
                val hasMerges = File(dir, "tokenizer/merges.txt").exists()

                hasTextEncoder && hasUnet && hasVaeDecoder && hasVocab && hasMerges
            }
            LocalAiModel.Type.MediaPipe -> {
                // MediaPipe requires bins folder with model files
                // Structure: model_folder/bins/* (multiple .bin files)
                val binsDir = File(dir, "bins")
                if (binsDir.exists() && binsDir.isDirectory) {
                    val binsFiles = binsDir.listFiles()?.map { it.name } ?: emptyList()
                    // Should not contain QNN-specific files
                    val isNotQnn = !binsFiles.any { it.startsWith("clip.") || it.startsWith("unet.") }
                    binsFiles.isNotEmpty() && isNotQnn
                } else {
                    false
                }
            }
            LocalAiModel.Type.QNN -> {
                // QNN requires: clip + unet + vae_decoder + tokenizer.json
                // NPU mode: clip.bin, unet.bin, vae_decoder.bin
                // CPU mode: clip.mnn (or clip_v2.mnn), unet.mnn, vae_decoder.mnn
                val hasClipBin = files.contains("clip.bin")
                val hasClipMnn = files.contains("clip.mnn") || files.contains("clip_v2.mnn")
                val hasUnetBin = files.contains("unet.bin")
                val hasUnetMnn = files.contains("unet.mnn")
                val hasVaeBin = files.contains("vae_decoder.bin")
                val hasVaeMnn = files.contains("vae_decoder.mnn")
                val hasTokenizer = files.contains("tokenizer.json")

                val hasNpuModel = hasClipBin && hasUnetBin && hasVaeBin
                val hasCpuModel = hasClipMnn && hasUnetMnn && hasVaeMnn

                hasTokenizer && (hasNpuModel || hasCpuModel)
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
