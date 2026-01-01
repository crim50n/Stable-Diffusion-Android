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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.file.LOCAL_DIFFUSION_CUSTOM_PATH
import com.shifthackz.aisdv1.core.extensions.getRealPath
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.screen.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.ServerSetupScreenTags.CUSTOM_MODEL_SWITCH
import com.shifthackz.aisdv1.presentation.screen.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.theme.textFieldColors
import com.shifthackz.aisdv1.core.localization.R as LocalizationR

@Composable
fun LocalDiffusionForm(
    modifier: Modifier = Modifier,
    state: ServerSetupState,
    buildInfoProvider: BuildInfoProvider = BuildInfoProvider.stub,
    processIntent: (ServerSetupIntent) -> Unit = {},
) {
    val isQnn = state.mode == ServerSource.LOCAL_QUALCOMM_QNN
    val modelItemUi: @Composable (ServerSetupState.LocalModel) -> Unit = { model ->
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f))
                .defaultMinSize(minHeight = 50.dp)
                .border(
                    width = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    color = if (!isQnn && model.selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                )
                .let { mod ->
                    if (isQnn) mod else mod.clickable { processIntent(ServerSetupIntent.SelectLocalModel(model)) }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val icon = when {
                    model.id == LocalAiModel.CustomOnnx.id -> Icons.Outlined.Landslide
                    model.id == LocalAiModel.CustomMediaPipe.id -> Icons.Outlined.Landslide
                    else -> Icons.Outlined.Memory
                }
                Icon(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(42.dp),
                    imageVector = icon,
                    contentDescription = null,
                )
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (model.id != LocalAiModel.CustomOnnx.id
                        && model.id != LocalAiModel.CustomMediaPipe.id
                    ) {
                        Text(
                            text = model.size,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                // Status icon on right side (for non-custom models)
                if (model.id != LocalAiModel.CustomOnnx.id
                    && model.id != LocalAiModel.CustomMediaPipe.id
                ) {
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

            // Custom model path info
            if (model.id == LocalAiModel.CustomOnnx.id
                || model.id == LocalAiModel.CustomMediaPipe.id
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = LocalizationR.string.model_local_custom_title),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (model.id == LocalAiModel.CustomOnnx.id) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = LocalizationR.string.model_local_custom_sub_title),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    fun folderModifier(treeNum: Int) =
                        Modifier.padding(start = (treeNum - 1) * 12.dp)

                    val folderStyle = MaterialTheme.typography.bodySmall
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = state.localOnnxCustomModelPath,
                        style = folderStyle,
                    )

                    Text(modifier = folderModifier(3), text = "text_encoder", style = folderStyle)
                    Text(modifier = folderModifier(4), text = "model.ort", style = folderStyle)
                    Text(modifier = folderModifier(3), text = "tokenizer", style = folderStyle)
                    Text(modifier = folderModifier(4), text = "merges.txt", style = folderStyle)
                    Text(modifier = folderModifier(3), text = "special_tokens_map.json", style = folderStyle)
                    Text(modifier = folderModifier(4), text = "tokenizer_config.json", style = folderStyle)
                    Text(modifier = folderModifier(4), text = "vocab.json", style = folderStyle)
                    Text(modifier = folderModifier(3), text = "unet", style = folderStyle)
                    Text(modifier = folderModifier(4), text = "model.ort", style = folderStyle)
                    Text(modifier = folderModifier(3), text = "vae_decoder", style = folderStyle)
                    Text(modifier = folderModifier(4), text = "model.ort", style = folderStyle)
                }
            }

            // Progress indicator for downloading
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
                is DownloadState.Error -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = LocalizationR.string.error_download_fail),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                else -> Unit
            }

            // Action buttons (for non-custom models)
            if (model.id != LocalAiModel.CustomOnnx.id
                && model.id != LocalAiModel.CustomMediaPipe.id
            ) {
                // Cancel button during download
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

                // Retry button on error
                if (model.downloadState is DownloadState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                processIntent(ServerSetupIntent.LocalModel.ClickReduce(model))
                            },
                        ) {
                            Text(
                                text = stringResource(id = LocalizationR.string.retry),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }

                // Download button for not downloaded models
                if (!model.downloaded
                    && model.downloadState !is DownloadState.Downloading
                    && model.downloadState !is DownloadState.Error
                ) {
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

                // Delete button for downloaded and selected models
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
            text = stringResource(
                id = when (state.mode) {
                    ServerSource.LOCAL_MICROSOFT_ONNX,
                    ServerSource.LOCAL_QUALCOMM_QNN -> LocalizationR.string.hint_local_diffusion_title
                    else -> LocalizationR.string.hint_mediapipe_title
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
        )
        Text(
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            text = stringResource(
                id = when (state.mode) {
                    ServerSource.LOCAL_MICROSOFT_ONNX -> LocalizationR.string.hint_local_diffusion_sub_title
                    ServerSource.LOCAL_QUALCOMM_QNN -> LocalizationR.string.hint_qnn_sub_title
                    else -> LocalizationR.string.hint_mediapipe_sub_title
                },
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (buildInfoProvider.type != BuildType.PLAY) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    modifier = Modifier.testTag(CUSTOM_MODEL_SWITCH),
                    checked = state.localCustomModel,
                    onCheckedChange = {
                        processIntent(ServerSetupIntent.AllowLocalCustomModel(it))
                    },
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = LocalizationR.string.model_local_custom_switch),
                )
            }
        }
        if (state.localCustomModel && buildInfoProvider.type != BuildType.PLAY) {
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
                        ?.let(ServerSetupIntent::SelectLocalModelPath)
                        ?.let(processIntent::invoke)
                }
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                value = state.localCustomModelPath,
                onValueChange = { string ->
                    string.filter { it != '\n' }
                        .let(ServerSetupIntent::SelectLocalModelPath)
                        .let(processIntent::invoke)
                },
                enabled = true,
                label = { Text(stringResource(LocalizationR.string.model_local_path_title)) },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            processIntent(
                                ServerSetupIntent.SelectLocalModelPath(LOCAL_DIFFUSION_CUSTOM_PATH)
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
                isError = state.localCustomModelPathValidationError != null,
                supportingText = {
                    state.localCustomModelPathValidationError
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
            Spacer(modifier = Modifier.height(8.dp))
        }
        state.localModels
            .filter {
                val customPredicate =
                    it.id == LocalAiModel.CustomOnnx.id || it.id == LocalAiModel.CustomMediaPipe.id
                if (state.localCustomModel) customPredicate else !customPredicate
            }
            .forEach { localModel -> modelItemUi(localModel) }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(id = LocalizationR.string.hint_local_diffusion_warning),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
