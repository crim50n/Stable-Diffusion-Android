package dev.minios.pdaiv1.feature.mediapipe.extensions

import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.domain.entity.LocalAiModel
import dev.minios.pdaiv1.domain.preference.PreferenceManager

fun modelPath(
    preferenceManager: PreferenceManager,
    fileProviderDescriptor: FileProviderDescriptor,
): String {
    val modelId = preferenceManager.localMediaPipeModelId
    return if (modelId == LocalAiModel.CustomMediaPipe.id) {
        preferenceManager.localMediaPipeCustomModelPath
    } else {
        "${fileProviderDescriptor.localModelDirPath}/${modelId}"
    }
}
