package dev.minios.pdaiv1.presentation.screen.setup.forms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.core.common.appbuild.BuildType
import dev.minios.pdaiv1.presentation.screen.setup.ServerSetupIntent
import dev.minios.pdaiv1.presentation.screen.setup.ServerSetupState

@Composable
fun MediaPipeForm(
    modifier: Modifier = Modifier,
    state: ServerSetupState,
    buildInfoProvider: BuildInfoProvider = BuildInfoProvider.stub,
    processIntent: (ServerSetupIntent) -> Unit = {},
) {
    when (buildInfoProvider.type) {
        BuildType.FOSS -> {

        }

        else -> LocalDiffusionForm(
            modifier = modifier,
            state = state,
            buildInfoProvider = buildInfoProvider,
            processIntent = processIntent,
        )
    }
}
