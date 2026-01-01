package dev.minios.pdaiv1.presentation.screen.setup.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.presentation.screen.setup.ServerSetupIntent
import dev.minios.pdaiv1.presentation.screen.setup.ServerSetupState
import dev.minios.pdaiv1.presentation.screen.setup.forms.Automatic1111Form
import dev.minios.pdaiv1.presentation.screen.setup.forms.FalAiForm
import dev.minios.pdaiv1.presentation.screen.setup.forms.HordeForm
import dev.minios.pdaiv1.presentation.screen.setup.forms.HuggingFaceForm
import dev.minios.pdaiv1.presentation.screen.setup.forms.LocalDiffusionForm
import dev.minios.pdaiv1.presentation.screen.setup.forms.MediaPipeForm
import dev.minios.pdaiv1.presentation.screen.setup.forms.OpenAiForm
import dev.minios.pdaiv1.presentation.screen.setup.forms.QnnForm
import dev.minios.pdaiv1.presentation.screen.setup.forms.StabilityAiForm
import dev.minios.pdaiv1.presentation.screen.setup.forms.SwarmUiForm

@Composable
fun ConfigurationStep(
    modifier: Modifier = Modifier,
    state: ServerSetupState,
    buildInfoProvider: BuildInfoProvider = BuildInfoProvider.stub,
    processIntent: (ServerSetupIntent) -> Unit = {},
) {
    BaseServerSetupStateWrapper(modifier) {
        when (state.mode) {
            ServerSource.AUTOMATIC1111 -> Automatic1111Form(
                state = state,
                processIntent = processIntent,
            )

            ServerSource.HORDE -> HordeForm(
                state = state,
                processIntent = processIntent,
            )

            ServerSource.LOCAL_MICROSOFT_ONNX -> LocalDiffusionForm(
                state = state,
                buildInfoProvider = buildInfoProvider,
                processIntent = processIntent,
            )

            ServerSource.HUGGING_FACE -> HuggingFaceForm(
                state = state,
                processIntent = processIntent,
            )

            ServerSource.OPEN_AI -> OpenAiForm(
                state = state,
                processIntent = processIntent,
            )

            ServerSource.STABILITY_AI -> StabilityAiForm(
                state = state,
                processIntent = processIntent,
            )

            ServerSource.FAL_AI -> FalAiForm(
                state = state,
                processIntent = processIntent,
            )

            ServerSource.SWARM_UI -> SwarmUiForm(
                state = state,
                processIntent = processIntent,
            )

            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> MediaPipeForm(
                state = state,
                buildInfoProvider = buildInfoProvider,
                processIntent = processIntent,
            )

            ServerSource.LOCAL_QUALCOMM_QNN -> QnnForm(
                state = state,
                buildInfoProvider = buildInfoProvider,
                processIntent = processIntent,
            )
        }
    }
}
