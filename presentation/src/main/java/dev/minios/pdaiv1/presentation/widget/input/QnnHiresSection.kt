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
import dev.minios.pdaiv1.domain.entity.QnnHiresConfig
import dev.minios.pdaiv1.presentation.model.QnnResolution
import dev.minios.pdaiv1.presentation.theme.sliderColors
import dev.minios.pdaiv1.core.localization.R as LocalizationR

/**
 * Composable for configuring QNN Hires.Fix settings (NPU only).
 *
 * Hires.Fix generates at base resolution, then upscales and refines
 * at the target resolution with the same aspect ratio.
 *
 * Supported upscale paths:
 * - 512×512 → 768×768, 1024×1024
 * - 512×768 → 768×1024
 * - 768×512 → 1024×768
 * - 768×768 → 1024×1024
 *
 * @param baseResolution Current base resolution selected in the form.
 */
@Composable
fun QnnHiresSection(
    modifier: Modifier = Modifier,
    baseResolution: QnnResolution,
    config: QnnHiresConfig,
    onConfigChange: (QnnHiresConfig) -> Unit,
) {
    // Get available targets for the current base resolution (same aspect ratio)
    val targetResolutions = QnnResolution.hiresTargetResolutions(baseResolution)

    // If no targets available for this base, don't show section
    if (targetResolutions.isEmpty()) return

    val currentTarget = QnnResolution.fromDimensions(config.targetWidth, config.targetHeight)
        ?.takeIf { it in targetResolutions }
        ?: QnnResolution.defaultHiresTarget(baseResolution)
        ?: return

    Column(modifier = modifier) {
        // Enable/Disable toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = stringResource(id = LocalizationR.string.hint_qnn_hires_enabled),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = stringResource(id = LocalizationR.string.hint_qnn_hires_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = config.enabled,
                onCheckedChange = { onConfigChange(config.copy(enabled = it)) },
            )
        }

        // Expanded settings when enabled
        AnimatedVisibility(visible = config.enabled) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                // Target resolution selection
                DropdownTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = LocalizationR.string.hint_qnn_hires_target.asUiText(),
                    value = currentTarget,
                    items = targetResolutions,
                    onItemSelected = { resolution ->
                        onConfigChange(config.copy(
                            targetWidth = resolution.width,
                            targetHeight = resolution.height
                        ))
                    },
                    displayDelegate = { it.displayName.asUiText() },
                )

                // Steps slider (0 = use same as first pass, 1-50)
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(
                        id = LocalizationR.string.hint_qnn_hires_steps,
                        config.steps.toString()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = config.steps.toFloat(),
                    onValueChange = { onConfigChange(config.copy(steps = it.toInt())) },
                    valueRange = 0f..50f,
                    steps = 50,
                    colors = sliderColors,
                )

                // Denoising strength slider (0.0 - 1.0)
                Text(
                    text = stringResource(
                        id = LocalizationR.string.hint_qnn_hires_denoising,
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
