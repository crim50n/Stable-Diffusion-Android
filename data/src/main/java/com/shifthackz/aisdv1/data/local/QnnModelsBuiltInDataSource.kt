package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.core.common.device.DeviceChipsetDetector
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import io.reactivex.rxjava3.core.Single

/**
 * Built-in QNN models data source.
 * Provides hardcoded QNN/MNN models from HuggingFace (xororz/sd-qnn and xororz/sd-mnn).
 * Models are automatically filtered based on device chipset compatibility.
 */
internal class QnnModelsBuiltInDataSource {

    private val deviceChipsetSuffix: String by lazy { DeviceChipsetDetector.getChipsetSuffix() }

    fun getAll(): Single<List<LocalAiModel>> = Single.fromCallable {
        buildAllModels().filter { model ->
            // Show models that match device chipset or have no chipset requirement (MNN/CPU models)
            model.chipsetSuffix == null || model.chipsetSuffix == deviceChipsetSuffix
        }
    }

    /**
     * Get all models without filtering by chipset.
     * Useful for debugging or showing all available options.
     */
    fun getAllUnfiltered(): Single<List<LocalAiModel>> = Single.just(buildAllModels())

    private fun buildAllModels(): List<LocalAiModel> {
        val npuModels = buildNpuModels()
        val cpuModels = buildCpuModels()
        return npuModels + cpuModels
    }

    private fun buildNpuModels(): List<LocalAiModel> {
        return npuModelDefinitions.flatMap { (baseName, displayName, sizes) ->
            listOf(
                createNpuModel(baseName, displayName, "8gen2", sizes.gen2Size),
                createNpuModel(baseName, displayName, "8gen1", sizes.gen1Size),
                createNpuModel(baseName, displayName, "min", sizes.minSize),
            )
        }
    }

    private fun buildCpuModels(): List<LocalAiModel> {
        return cpuModelDefinitions.map { (baseName, displayName, size) ->
            createCpuModel(baseName, displayName, size)
        }
    }

    private fun createNpuModel(
        baseName: String,
        displayName: String,
        suffix: String,
        size: String,
    ): LocalAiModel {
        val chipsetDisplayName = when (suffix) {
            "8gen2" -> "8Gen2/3/4"
            "8gen1" -> "8Gen1"
            "min" -> "Other Snapdragon"
            else -> suffix
        }
        return LocalAiModel(
            id = "qnn-${baseName.lowercase()}-$suffix",
            type = LocalAiModel.Type.QNN,
            name = "$displayName ($chipsetDisplayName)",
            size = size,
            sources = listOf(
                "https://huggingface.co/xororz/sd-qnn/resolve/main/${baseName}_qnn2.28_$suffix.zip?download=true"
            ),
            chipsetSuffix = suffix,
        )
    }

    private fun createCpuModel(
        baseName: String,
        displayName: String,
        size: String,
    ): LocalAiModel {
        return LocalAiModel(
            id = "mnn-${baseName.lowercase()}",
            type = LocalAiModel.Type.QNN,
            name = "$displayName (CPU)",
            size = size,
            sources = listOf(
                "https://huggingface.co/xororz/sd-mnn/resolve/main/$baseName.zip?download=true"
            ),
            runOnCpu = true,
            chipsetSuffix = null, // CPU models work on all devices
        )
    }

    companion object {
        /**
         * NPU model definitions: (baseName, displayName, ModelSizes)
         */
        private data class ModelSizes(
            val gen2Size: String,
            val gen1Size: String,
            val minSize: String,
        )

        private val npuModelDefinitions = listOf(
            Triple("AbsoluteReality", "Absolute Reality", ModelSizes("1.05 GB", "1.06 GB", "993 MB")),
            Triple("AnythingV5", "Anything V5", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("ChilloutMix", "ChilloutMix", ModelSizes("1.07 GB", "1.07 GB", "1.01 GB")),
            Triple("CrossKemono2.5", "Cross Kemono 2.5", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("CuteYukiMix", "CuteYukiMix", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("DarkSushiV4", "Dark Sushi Mix V4", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("DreamShaperV8", "DreamShaper V8", ModelSizes("1.03 GB", "1.03 GB", "968 MB")),
            Triple("HyperSpireV5", "HyperSpire V5", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("MajicmixRealisticV7", "MajicMix Realistic V7", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("MeinaMixV12", "MeinaMix V12", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("MistoonAnimeV3", "Mistoon Anime V3", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("NaiAnimeV2", "NAI Anime V2", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("NeverEndingDreamV122", "NeverEnding Dream V1.22", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("QteaMix", "QteaMix", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("RealisianV6", "Realisian V6", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("RealisticVisionHyper", "Realistic Vision Hyper", ModelSizes("1.07 GB", "1.07 GB", "1.01 GB")),
            Triple("SweetMixV22Flat", "SweetMix V2.2 Flat", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("counterfeitV30", "Counterfeit V3.0", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("superinvinciblev2", "Super Invincible V2", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
            Triple("unStableIllusion", "UnStable Illusion", ModelSizes("1.06 GB", "1.06 GB", "995 MB")),
        )

        /**
         * CPU model definitions: (baseName, displayName, size)
         * These models use MNN and work on any device via CPU/GPU.
         */
        private val cpuModelDefinitions = listOf(
            Triple("AbsoluteReality", "Absolute Reality", "1.05 GB"),
            Triple("AnythingV5", "Anything V5", "1.05 GB"),
            Triple("ChilloutMix", "ChilloutMix", "1.05 GB"),
            Triple("CrossKemono2.5", "Cross Kemono 2.5", "1.05 GB"),
            Triple("CuteYukiMix", "CuteYukiMix", "1.05 GB"),
            Triple("DarkSushiV4", "Dark Sushi Mix V4", "1.05 GB"),
            Triple("DreamShaperV8", "DreamShaper V8", "1.05 GB"),
            Triple("HyperSpireV5", "HyperSpire V5", "1.05 GB"),
            Triple("MajicmixRealisticV7", "MajicMix Realistic V7", "1.05 GB"),
            Triple("MeinaMixV12", "MeinaMix V12", "1.05 GB"),
            Triple("MistoonAnimeV3", "Mistoon Anime V3", "1.05 GB"),
            Triple("NaiAnimeV2", "NAI Anime V2", "1.05 GB"),
            Triple("NeverEndingDreamV122", "NeverEnding Dream V1.22", "1.05 GB"),
            Triple("QteaMix", "QteaMix", "1.05 GB"),
            Triple("RealisianV6", "Realisian V6", "1.05 GB"),
            Triple("RealisticVisionHyper", "Realistic Vision Hyper", "1.05 GB"),
            Triple("SweetMixV22Flat", "SweetMix V2.2 Flat", "1.05 GB"),
            Triple("counterfeitV30", "Counterfeit V3.0", "1.05 GB"),
            Triple("superinvinciblev2", "Super Invincible V2", "1.05 GB"),
            Triple("unStableIllusion", "UnStable Illusion", "1.05 GB"),
        )
    }
}
