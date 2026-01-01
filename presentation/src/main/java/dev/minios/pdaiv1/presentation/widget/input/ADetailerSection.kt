package dev.minios.pdaiv1.presentation.widget.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.minios.pdaiv1.core.common.math.roundTo
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.domain.entity.ADetailerConfig
import dev.minios.pdaiv1.presentation.theme.sliderColors
import dev.minios.pdaiv1.core.localization.R as LocalizationR

/**
 * Composable for configuring ADetailer extension settings.
 * ADetailer automatically detects faces/hands and refines them.
 */
@Composable
fun ADetailerSection(
    modifier: Modifier = Modifier,
    config: ADetailerConfig,
    onConfigChange: (ADetailerConfig) -> Unit,
) {
    Column(modifier = modifier) {
        // Enable/Disable toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = LocalizationR.string.hint_adetailer_enabled),
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = config.enabled,
                onCheckedChange = { onConfigChange(config.copy(enabled = it)) },
            )
        }

        // Expanded settings when enabled
        AnimatedVisibility(visible = config.enabled) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                // Model selection
                DropdownTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = LocalizationR.string.hint_adetailer_model.asUiText(),
                    value = config.model,
                    items = ADetailerConfig.AVAILABLE_MODELS,
                    onItemSelected = { onConfigChange(config.copy(model = it)) },
                    displayDelegate = { it.asUiText() },
                )

                // Confidence slider
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(
                        id = LocalizationR.string.hint_adetailer_confidence,
                        config.confidence.roundTo(2).toString()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = config.confidence,
                    onValueChange = { onConfigChange(config.copy(confidence = it.roundTo(2))) },
                    valueRange = 0.1f..1.0f,
                    colors = sliderColors,
                )

                // Denoising strength slider
                Text(
                    text = stringResource(
                        id = LocalizationR.string.hint_adetailer_denoising,
                        config.denoisingStrength.roundTo(2).toString()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = config.denoisingStrength,
                    onValueChange = { onConfigChange(config.copy(denoisingStrength = it.roundTo(2))) },
                    valueRange = 0.0f..1.0f,
                    colors = sliderColors,
                )
            }
        }
    }
}
