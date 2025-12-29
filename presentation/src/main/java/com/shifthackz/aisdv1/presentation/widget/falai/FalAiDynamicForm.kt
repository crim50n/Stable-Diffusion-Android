@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.widget.falai

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.FalAiPropertyType
import com.shifthackz.aisdv1.presentation.model.FalAiEndpointUi
import com.shifthackz.aisdv1.presentation.model.FalAiPropertyUi
import com.shifthackz.aisdv1.presentation.screen.falai.FalAiGenerationState
import com.shifthackz.aisdv1.presentation.widget.input.DropdownTextField

@Composable
fun FalAiDynamicForm(
    state: FalAiGenerationState,
    onEndpointSelected: (String) -> Unit,
    onPropertyChanged: (String, Any?) -> Unit,
    onToggleAdvanced: (Boolean) -> Unit,
    onImportOpenApi: (String) -> Unit = {},
    onDeleteEndpoint: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Endpoint selector with import button
        FalAiEndpointSelector(
            endpoints = state.endpoints,
            selectedEndpoint = state.selectedEndpoint,
            onEndpointSelected = onEndpointSelected,
            onImportOpenApi = onImportOpenApi,
            onDeleteEndpoint = onDeleteEndpoint,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main properties (prompt, image fields, etc.)
        state.mainProperties.forEach { property ->
            if (property.type == FalAiPropertyType.INPAINT) {
                // Special handling for inpainting - combined image + mask field
                FalAiInpaintField(
                    property = property,
                    currentImageValue = state.propertyValues[property.name],
                    currentMaskValue = property.linkedMaskProperty?.let { state.propertyValues[it] },
                    onImageChange = { value -> onPropertyChanged(property.name, value) },
                    onMaskChange = { value ->
                        property.linkedMaskProperty?.let { maskProp ->
                            onPropertyChanged(maskProp, value)
                        }
                    },
                )
            } else {
                FalAiPropertyField(
                    property = property,
                    onValueChange = { value -> onPropertyChanged(property.name, value) },
                )
            }
        }

        // Advanced options section
        if (state.hasAdvancedProperties) {
            Spacer(modifier = Modifier.height(16.dp))

            AdvancedOptionsSection(
                visible = state.advancedOptionsVisible,
                onToggle = onToggleAdvanced,
                properties = state.advancedProperties,
                onPropertyChanged = onPropertyChanged,
            )
        }
    }
}

@Composable
fun FalAiEndpointSelector(
    endpoints: List<FalAiEndpointUi>,
    selectedEndpoint: FalAiEndpointUi?,
    onEndpointSelected: (String) -> Unit,
    onImportOpenApi: (String) -> Unit = {},
    onDeleteEndpoint: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val jsonFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val json = inputStream.bufferedReader().readText()
                onImportOpenApi(json)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Model",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )

            // Import button
            OutlinedButton(
                onClick = { jsonFilePicker.launch("application/json") },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp),
                )
                Text("Import")
            }

            // Delete button (only for custom endpoints)
            if (selectedEndpoint?.isCustom == true) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onDeleteEndpoint(selectedEndpoint.id) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete endpoint",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort endpoints by title
        val sortedEndpoints = endpoints.sortedBy { it.title }

        DropdownTextField(
            modifier = Modifier.fillMaxWidth(),
            label = "Select endpoint".asUiText(),
            value = selectedEndpoint,
            items = sortedEndpoints,
            onItemSelected = { endpoint -> onEndpointSelected(endpoint.id) },
            displayDelegate = { endpoint ->
                val customSuffix = if (endpoint.isCustom) " ⭐" else ""
                "${endpoint.title} (${endpoint.category})$customSuffix".asUiText()
            },
        )

        selectedEndpoint?.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
            )
        }
    }
}

@Composable
private fun AdvancedOptionsSection(
    visible: Boolean,
    onToggle: (Boolean) -> Unit,
    properties: List<FalAiPropertyUi>,
    onPropertyChanged: (String, Any?) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column {
            // Header (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle(!visible) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Advanced Options",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (visible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (visible) "Collapse" else "Expand",
                )
            }

            // Content (animated visibility)
            AnimatedVisibility(
                visible = visible,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    ),
                ) {
                    properties.forEach { property ->
                        FalAiPropertyField(
                            property = property,
                            onValueChange = { value -> onPropertyChanged(property.name, value) },
                        )
                    }
                }
            }
        }
    }
}
