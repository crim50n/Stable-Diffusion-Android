package dev.minios.pdaiv1.feature.diffusion.extensions

import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.domain.entity.LocalAiModel
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.feature.diffusion.environment.LocalModelIdProvider

fun modelPathPrefix(
    preferenceManager: PreferenceManager,
    fileProviderDescriptor: FileProviderDescriptor,
    localModelIdProvider: LocalModelIdProvider,
): String {
    val modelId = localModelIdProvider.get()
    return if (modelId == LocalAiModel.CustomOnnx.id) {
        preferenceManager.localOnnxCustomModelPath
    } else {
        "${fileProviderDescriptor.localModelDirPath}/${modelId}"
    }
}
