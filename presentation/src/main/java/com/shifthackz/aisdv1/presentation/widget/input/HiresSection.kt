package com.shifthackz.aisdv1.presentation.widget.input

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
import com.shifthackz.aisdv1.core.common.math.roundTo
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.presentation.theme.sliderColors
import com.shifthackz.aisdv1.core.localization.R as LocalizationR

/**
 * Composable for configuring Hires. Fix settings.
 * Hires. Fix upscales and refines images using a second pass.
 */
@Composable
fun HiresSection(
    modifier: Modifier = Modifier,
    config: HiresConfig,
    onConfigChange: (HiresConfig) -> Unit,
) {
    Column(modifier = modifier) {
        // Enable/Disable toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = LocalizationR.string.hint_hires_enabled),
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
                // Upscaler selection
                DropdownTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = LocalizationR.string.hint_hires_upscaler.asUiText(),
                    value = config.upscaler,
                    items = HiresConfig.AVAILABLE_UPSCALERS,
                    onItemSelected = { onConfigChange(config.copy(upscaler = it)) },
                    displayDelegate = { it.asUiText() },
                )

                // Scale slider (1.0 - 4.0)
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(
                        id = LocalizationR.string.hint_hires_scale,
                        config.scale.roundTo(1).toString()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = config.scale,
                    onValueChange = { onConfigChange(config.copy(scale = it.roundTo(1))) },
                    valueRange = 1.0f..4.0f,
                    colors = sliderColors,
                )

                // Steps slider (0 = use same as first pass, 1-150)
                Text(
                    text = stringResource(
                        id = LocalizationR.string.hint_hires_steps,
                        config.steps.toString()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = config.steps.toFloat(),
                    onValueChange = { onConfigChange(config.copy(steps = it.toInt())) },
                    valueRange = 0f..150f,
                    steps = 150,
                    colors = sliderColors,
                )

                // Denoising strength slider (0.0 - 1.0)
                Text(
                    text = stringResource(
                        id = LocalizationR.string.hint_hires_denoising,
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
