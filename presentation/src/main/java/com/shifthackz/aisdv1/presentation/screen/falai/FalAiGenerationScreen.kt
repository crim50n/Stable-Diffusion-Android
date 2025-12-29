@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.falai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifthackz.android.core.mvi.MviComponent
import com.shifthackz.aisdv1.presentation.modal.ModalRenderer
import com.shifthackz.aisdv1.presentation.model.Modal
import com.shifthackz.aisdv1.presentation.screen.drawer.DrawerIntent
import com.shifthackz.aisdv1.presentation.widget.falai.FalAiDynamicForm
import com.shifthackz.aisdv1.presentation.widget.work.BackgroundWorkWidget
import org.koin.androidx.compose.koinViewModel
import com.shifthackz.aisdv1.core.localization.R as LocalizationR

@Composable
fun FalAiGenerationScreen() {
    MviComponent(
        viewModel = koinViewModel<FalAiGenerationViewModel>(),
    ) { state, intentHandler ->
        FalAiGenerationScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = state,
            processIntent = intentHandler,
        )
    }
}

@Composable
fun FalAiGenerationScreenContent(
    modifier: Modifier = Modifier,
    state: FalAiGenerationState,
    processIntent: (FalAiGenerationIntent) -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Box(modifier) {
        Scaffold(
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = {
                            IconButton(onClick = {
                                processIntent(FalAiGenerationIntent.Drawer(DrawerIntent.Open))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Fal AI",
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        },
                        windowInsets = WindowInsets(0, 0, 0, 0),
                    )
                    BackgroundWorkWidget(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 4.dp),
                    )
                }
            },
            content = { paddingValues ->
                when {
                    state.loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    state.endpoints.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "No endpoints available",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .padding(paddingValues)
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp),
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            FalAiDynamicForm(
                                state = state,
                                onEndpointSelected = { id ->
                                    processIntent(FalAiGenerationIntent.SelectEndpoint(id))
                                },
                                onPropertyChanged = { name, value ->
                                    processIntent(FalAiGenerationIntent.UpdateProperty(name, value))
                                },
                                onToggleAdvanced = { visible ->
                                    processIntent(FalAiGenerationIntent.ToggleAdvancedOptions(visible))
                                },
                                onImportOpenApi = { json ->
                                    processIntent(FalAiGenerationIntent.ImportOpenApiJson(json))
                                },
                                onDeleteEndpoint = { id ->
                                    processIntent(FalAiGenerationIntent.DeleteEndpoint(id))
                                },
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Generate button
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = state.canGenerate,
                                onClick = {
                                    keyboardController?.hide()
                                    processIntent(FalAiGenerationIntent.Generate)
                                },
                            ) {
                                Text(
                                    text = stringResource(id = LocalizationR.string.action_generate),
                                    color = LocalContentColor.current,
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            },
        )

        // Modal rendering
        ModalRenderer(
            screenModal = state.screenModal,
            processIntent = { intent ->
                when (intent) {
                    is FalAiGenerationIntent -> processIntent(intent)
                    else -> {
                        // Handle dismiss - check if it's a dismiss intent
                        if (state.screenModal != Modal.None) {
                            processIntent(FalAiGenerationIntent.DismissModal)
                        }
                    }
                }
            },
        )
    }
}
