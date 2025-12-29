package com.shifthackz.aisdv1.presentation.screen.falai

import com.shifthackz.aisdv1.presentation.model.Modal
import com.shifthackz.aisdv1.presentation.screen.drawer.DrawerIntent
import com.shifthackz.android.core.mvi.MviIntent

sealed interface FalAiGenerationIntent : MviIntent {

    data class SelectEndpoint(val endpointId: String) : FalAiGenerationIntent

    data class UpdateProperty(val name: String, val value: Any?) : FalAiGenerationIntent

    data class ToggleAdvancedOptions(val visible: Boolean) : FalAiGenerationIntent

    data object Generate : FalAiGenerationIntent

    data class SetModal(val modal: Modal) : FalAiGenerationIntent

    data object DismissModal : FalAiGenerationIntent

    data class Drawer(val intent: DrawerIntent) : FalAiGenerationIntent

    data class ImportOpenApiJson(val json: String) : FalAiGenerationIntent

    data class DeleteEndpoint(val endpointId: String) : FalAiGenerationIntent
}
