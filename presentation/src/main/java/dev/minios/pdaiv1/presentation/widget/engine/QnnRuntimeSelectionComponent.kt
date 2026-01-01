package dev.minios.pdaiv1.presentation.widget.engine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import org.koin.compose.koinInject
import dev.minios.pdaiv1.core.localization.R as LocalizationR

/**
 * Component for selecting QNN runtime mode (CPU or GPU/OpenCL).
 * Only shown when QNN backend is active and model supports CPU mode.
 */
@Composable
fun QnnRuntimeSelectionComponent(
    modifier: Modifier = Modifier,
) {
    val preferenceManager: PreferenceManager = koinInject()
    var useOpenCL by remember { mutableStateOf(preferenceManager.localQnnUseOpenCL) }
    var showGpuWarningDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = LocalizationR.string.hint_runtime),
            style = MaterialTheme.typography.bodyMedium
        )
        FilterChip(
            selected = !useOpenCL,
            onClick = {
                useOpenCL = false
                preferenceManager.localQnnUseOpenCL = false
            },
            label = { Text("CPU") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = useOpenCL,
            onClick = {
                if (!useOpenCL) {
                    showGpuWarningDialog = true
                } else {
                    useOpenCL = false
                    preferenceManager.localQnnUseOpenCL = false
                }
            },
            label = { Text("GPU") },
            modifier = Modifier.weight(1f)
        )
    }

    if (showGpuWarningDialog) {
        AlertDialog(
            onDismissRequest = { showGpuWarningDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text(stringResource(id = LocalizationR.string.warning_gpu_runtime_title)) },
            text = { Text(stringResource(id = LocalizationR.string.warning_gpu_runtime_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showGpuWarningDialog = false
                        useOpenCL = true
                        preferenceManager.localQnnUseOpenCL = true
                    }
                ) {
                    Text(stringResource(id = LocalizationR.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpuWarningDialog = false }) {
                    Text(stringResource(id = LocalizationR.string.cancel))
                }
            }
        )
    }
}
