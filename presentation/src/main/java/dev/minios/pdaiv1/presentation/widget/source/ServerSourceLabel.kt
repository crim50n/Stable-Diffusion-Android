package dev.minios.pdaiv1.presentation.widget.source

import androidx.compose.runtime.Composable
import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asString
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.core.localization.R as LocalizationR

@Composable
fun ServerSource.getName(): String {
    return getNameUiText().asString()
}

fun ServerSource.getNameUiText(): UiText = when (this) {
    ServerSource.AUTOMATIC1111 -> LocalizationR.string.srv_type_own
    ServerSource.HORDE -> LocalizationR.string.srv_type_horde
    ServerSource.LOCAL_MICROSOFT_ONNX -> LocalizationR.string.srv_type_local
    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> LocalizationR.string.srv_type_media_pipe
    ServerSource.LOCAL_QUALCOMM_QNN -> LocalizationR.string.srv_type_qnn
    ServerSource.HUGGING_FACE -> LocalizationR.string.srv_type_hugging_face
    ServerSource.OPEN_AI -> LocalizationR.string.srv_type_open_ai
    ServerSource.STABILITY_AI -> LocalizationR.string.srv_type_stability_ai
    ServerSource.FAL_AI -> LocalizationR.string.srv_type_fal_ai
    ServerSource.SWARM_UI -> LocalizationR.string.srv_type_swarm_ui
}.asUiText()
