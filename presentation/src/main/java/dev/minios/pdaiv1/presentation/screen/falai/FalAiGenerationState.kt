package dev.minios.pdaiv1.presentation.screen.falai

import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.presentation.model.FalAiEndpointUi
import dev.minios.pdaiv1.presentation.model.FalAiPropertyUi
import dev.minios.pdaiv1.presentation.model.Modal
import com.shifthackz.android.core.mvi.MviState

@Immutable
data class FalAiGenerationState(
    val loading: Boolean = true,
    val screenModal: Modal = Modal.None,
    val endpoints: List<FalAiEndpointUi> = emptyList(),
    val selectedEndpoint: FalAiEndpointUi? = null,
    val propertyValues: Map<String, Any?> = emptyMap(),
    val advancedOptionsVisible: Boolean = false,
    val generating: Boolean = false,
    val generationError: String? = null,
    val mainProperties: List<FalAiPropertyUi> = emptyList(),
    val advancedProperties: List<FalAiPropertyUi> = emptyList(),
) : MviState {

    val hasAdvancedProperties: Boolean
        get() = advancedProperties.isNotEmpty()

    val canGenerate: Boolean
        get() = selectedEndpoint != null && !generating && propertyValues["prompt"]?.toString()?.isNotBlank() == true
}
