package com.shifthackz.aisdv1.presentation.screen.setup.forms

import android.content.Intent
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileDownloadDone
import androidx.compose.material.icons.outlined.FileDownloadOff
import androidx.compose.material.icons.outlined.Landslide
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.file.LOCAL_DIFFUSION_CUSTOM_PATH
import com.shifthackz.aisdv1.core.extensions.getRealPath
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.presentation.screen.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.theme.textFieldColors
import com.shifthackz.aisdv1.core.localization.R as LocalizationR

@Composable
fun QnnForm(
    modifier: Modifier = Modifier,
    state: ServerSetupState,
    buildInfoProvider: BuildInfoProvider = BuildInfoProvider.stub,
    processIntent: (ServerSetupIntent) -> Unit = {},
) {
    val modelItemUi: @Composable (ServerSetupState.LocalModel) -> Unit = { model ->
        val isCustomModel = model.id == LocalAiModel.CustomQnn.id
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f))
                .defaultMinSize(minHeight = 50.dp)
                .border(
                    width = 2.dp,
                    color = if (model.selected) MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                )
                .clickable {
                    processIntent(ServerSetupIntent.SelectLocalModel(model))
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(42.dp),
                    imageVector = if (isCustomModel) Icons.Outlined.Landslide else Icons.Outlined.Memory,
                    contentDescription = null,
                )
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!isCustomModel) {
                        Text(
                            text = model.size,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                if (!isCustomModel) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(24.dp),
                        imageVector = when {
                            model.downloaded -> Icons.Outlined.FileDownloadDone
                            model.downloadState is DownloadState.Downloading -> Icons.Outlined.FileDownload
                            else -> Icons.Outlined.FileDownloadOff
                        },
                        contentDescription = null,
                        tint = when {
                            model.downloaded -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        },
                    )
                }
            }

            // Custom model info
            if (isCustomModel) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = LocalizationR.string.model_local_custom_title),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(id = LocalizationR.string.model_qnn_custom_sub_title),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))

                fun folderModifier(treeNum: Int) =
                    Modifier.padding(start = ((treeNum - 1) * 12).dp)

                val folderStyle = MaterialTheme.typography.bodySmall
                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = state.localQnnCustomModelPath,
                    style = folderStyle,
                )

                Text(modifier = folderModifier(3), text = "clip.bin / clip.mnn", style = folderStyle)
                Text(modifier = folderModifier(3), text = "unet.bin / unet.mnn", style = folderStyle)
                Text(modifier = folderModifier(3), text = "vae_decoder.bin / vae_decoder.mnn", style = folderStyle)
                Text(modifier = folderModifier(3), text = "tokenizer.json", style = folderStyle)
            }

            // Progress for downloading
            if (!isCustomModel) {
                when (val downloadState = model.downloadState) {
                    is DownloadState.Downloading -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = { downloadState.percent / 100f },
                        )
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = "${downloadState.percent}%",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    else -> Unit
                }

                if (model.downloadState is DownloadState.Downloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                processIntent(ServerSetupIntent.LocalModel.ClickReduce(model))
                            },
                        ) {
                            Text(
                                text = stringResource(id = LocalizationR.string.cancel),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
                if (!model.downloaded && model.downloadState !is DownloadState.Downloading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                processIntent(ServerSetupIntent.LocalModel.ClickReduce(model))
                            },
                        ) {
                            Text(
                                text = stringResource(id = LocalizationR.string.download),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
                if (model.downloaded && model.selected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            processIntent(ServerSetupIntent.LocalModel.ClickReduce(model))
                        },
                    ) {
                        Text(
                            text = stringResource(id = LocalizationR.string.delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 8.dp),
            text = stringResource(id = LocalizationR.string.hint_qnn_title),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            text = stringResource(id = LocalizationR.string.hint_qnn_sub_title),
            style = MaterialTheme.typography.bodyMedium,
        )

        // Custom model switch
        if (buildInfoProvider.type != BuildType.PLAY) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = state.localQnnCustomModel,
                    onCheckedChange = {
                        processIntent(ServerSetupIntent.AllowLocalQnnCustomModel(it))
                    },
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = LocalizationR.string.model_local_custom_switch),
                )
            }
        }

        // Custom model path configuration
        if (state.localQnnCustomModel && buildInfoProvider.type != BuildType.PLAY) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp),
                text = stringResource(id = LocalizationR.string.model_local_permission_header),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = stringResource(id = LocalizationR.string.model_local_permission_title),
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                onClick = { processIntent(ServerSetupIntent.LaunchManageStoragePermission) },
            ) {
                Text(
                    text = stringResource(id = LocalizationR.string.model_local_permission_button),
                    color = LocalContentColor.current,
                )
            }
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp),
                text = stringResource(id = LocalizationR.string.model_local_path_header),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            val context = LocalContext.current
            val uriFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                result.data?.data?.let { uri ->
                    context.contentResolver.takePersistableUriPermission(uri, uriFlags)
                    val docUri = DocumentsContract.buildDocumentUriUsingTree(
                        uri,
                        DocumentsContract.getTreeDocumentId(uri)
                    )
                    getRealPath(context, docUri)
                        ?.let(ServerSetupIntent::SelectLocalQnnModelPath)
                        ?.let(processIntent::invoke)
                }
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                value = state.localQnnCustomModelPath,
                onValueChange = { string ->
                    string.filter { it != '\n' }
                        .let(ServerSetupIntent::SelectLocalQnnModelPath)
                        .let(processIntent::invoke)
                },
                enabled = true,
                label = { Text(stringResource(LocalizationR.string.model_local_path_title)) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            processIntent(
                                ServerSetupIntent.SelectLocalQnnModelPath(LOCAL_DIFFUSION_CUSTOM_PATH)
                            )
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset",
                            )
                        },
                    )
                },
                isError = state.localCustomQnnPathValidationError != null,
                supportingText = {
                    state.localCustomQnnPathValidationError
                        ?.let { Text(it.asString(), color = MaterialTheme.colorScheme.error) }
                },
                colors = textFieldColors,
            )
            OutlinedButton(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp, bottom = 8.dp),
                onClick = {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        addFlags(uriFlags)
                    }
                    launcher.launch(intent)
                },
            ) {
                Text(
                    text = stringResource(id = LocalizationR.string.model_local_path_button),
                    color = LocalContentColor.current,
                )
            }

            // Scan button
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                onClick = { processIntent(ServerSetupIntent.ScanCustomModels) },
            ) {
                Icon(
                    modifier = Modifier.padding(end = 8.dp),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                )
                Text(
                    text = stringResource(id = LocalizationR.string.action_scan_models),
                    color = LocalContentColor.current,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Show scanned custom models when custom model mode is enabled
        if (state.localQnnCustomModel && buildInfoProvider.type != BuildType.PLAY) {
            if (state.scannedQnnCustomModels.isEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    text = stringResource(id = LocalizationR.string.model_scan_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            } else {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = stringResource(id = LocalizationR.string.model_scan_found, state.scannedQnnCustomModels.size),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                state.scannedQnnCustomModels.forEach { localModel ->
                    modelItemUi(localModel)
                }
            }
        }

        // Filter models - show only downloaded models when not in custom mode
        if (!state.localQnnCustomModel) {
            val modelsToShow = state.localQnnModels.filter { model ->
                model.id != LocalAiModel.CustomQnn.id
            }

            if (modelsToShow.isEmpty()) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    text = "No QNN models available yet.\n\nQNN models will be available for download from HuggingFace (xororz/sd-qnn).",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            } else {
                modelsToShow.forEach { localModel ->
                    modelItemUi(localModel)
                }
            }
        }

        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(id = LocalizationR.string.hint_local_diffusion_warning),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
