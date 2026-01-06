@file:OptIn(ExperimentalMaterial3Api::class)

package dev.minios.pdaiv1.presentation.screen.gallery.editor

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.RotateLeft
import androidx.compose.material.icons.automirrored.outlined.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.android.core.mvi.MviComponent
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import dev.minios.pdaiv1.core.localization.R as LocalizationR

@Composable
fun ImageEditorScreen(itemId: Long) {
    val context = LocalContext.current

    MviComponent(
        viewModel = koinViewModel<ImageEditorViewModel>(
            parameters = { parametersOf(itemId) },
        ),
        processEffect = { effect ->
            when (effect) {
                ImageEditorEffect.SavedSuccessfully -> {
                    Toast.makeText(
                        context,
                        context.getString(LocalizationR.string.gallery_save_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                ImageEditorEffect.SavedAsNewImage -> {
                    Toast.makeText(
                        context,
                        context.getString(LocalizationR.string.gallery_save_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        },
    ) { state, intentHandler ->
        ScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = state,
            processIntent = intentHandler,
        )
    }
}

@Composable
private fun ScreenContent(
    modifier: Modifier = Modifier,
    state: ImageEditorState,
    processIntent: (ImageEditorIntent) -> Unit = {},
) {
    var showRotateSheet by remember { mutableStateOf(false) }
    var showAdjustSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.background(Color.Black),
    ) {
        // Image content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                )
            } else {
                state.displayBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Edited image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { processIntent(ImageEditorIntent.NavigateBack) },
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }

            Text(
                text = stringResource(LocalizationR.string.edit),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )

            TextButton(
                onClick = { processIntent(ImageEditorIntent.Save) },
                enabled = state.hasChanges && !state.isSaving,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(LocalizationR.string.gallery_save_to_gallery),
                        color = if (state.hasChanges) MaterialTheme.colorScheme.primary else Color.Gray,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Bottom navigation bar (Immich style)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(35.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EditorToolButton(
                    icon = Icons.AutoMirrored.Outlined.RotateRight,
                    label = stringResource(LocalizationR.string.editor_rotate),
                    onClick = { showRotateSheet = true },
                )
                EditorToolButton(
                    icon = Icons.Default.Tune,
                    label = stringResource(LocalizationR.string.editor_adjust),
                    onClick = { showAdjustSheet = true },
                )
            }
        }

        // Reset button (shown when there are changes)
        AnimatedVisibility(
            visible = state.hasChanges,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
                .navigationBarsPadding(),
        ) {
            TextButton(
                onClick = { processIntent(ImageEditorIntent.ResetFilters) },
            ) {
                Text(
                    text = "Reset",
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }

        // Rotate/Flip Bottom Sheet
        if (showRotateSheet) {
            ModalBottomSheet(
                onDismissRequest = { showRotateSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                },
            ) {
                RotateFlipContent(
                    onRotateLeft = { processIntent(ImageEditorIntent.RotateLeft) },
                    onRotateRight = { processIntent(ImageEditorIntent.RotateRight) },
                    onFlipH = { processIntent(ImageEditorIntent.FlipHorizontal) },
                    onFlipV = { processIntent(ImageEditorIntent.FlipVertical) },
                    onDismiss = { showRotateSheet = false },
                )
            }
        }

        // Adjust Bottom Sheet
        if (showAdjustSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAdjustSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .size(width = 40.dp, height = 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                },
            ) {
                AdjustContent(
                    brightness = state.brightness,
                    contrast = state.contrast,
                    saturation = state.saturation,
                    onBrightnessChange = { processIntent(ImageEditorIntent.UpdateBrightness(it)) },
                    onContrastChange = { processIntent(ImageEditorIntent.UpdateContrast(it)) },
                    onSaturationChange = { processIntent(ImageEditorIntent.UpdateSaturation(it)) },
                    onDismiss = { showAdjustSheet = false },
                )
            }
        }
    }
}

@Composable
private fun EditorToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 24.dp),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RotateFlipContent(
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(LocalizationR.string.editor_rotate),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RotateFlipButton(
                icon = Icons.AutoMirrored.Outlined.RotateLeft,
                label = "90° Left",
                onClick = onRotateLeft,
            )
            RotateFlipButton(
                icon = Icons.AutoMirrored.Outlined.RotateRight,
                label = "90° Right",
                onClick = onRotateRight,
            )
            RotateFlipButton(
                icon = Icons.Default.FlipCameraAndroid,
                label = "Flip H",
                onClick = onFlipH,
            )
            RotateFlipButton(
                icon = Icons.Default.FlipCameraAndroid,
                label = "Flip V",
                onClick = onFlipV,
                iconRotation = 90f,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun RotateFlipButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    iconRotation: Float = 0f,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AdjustContent(
    brightness: Float,
    contrast: Float,
    saturation: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSaturationChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
    ) {
        Text(
            text = stringResource(LocalizationR.string.editor_adjust),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        // Brightness
        AdjustSlider(
            label = stringResource(LocalizationR.string.editor_brightness),
            value = brightness,
            valueRange = -1f..1f,
            onValueChange = onBrightnessChange,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contrast
        AdjustSlider(
            label = stringResource(LocalizationR.string.editor_contrast),
            value = contrast,
            valueRange = 0.5f..2f,
            defaultValue = 1f,
            onValueChange = onContrastChange,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Saturation
        AdjustSlider(
            label = stringResource(LocalizationR.string.editor_saturation),
            value = saturation,
            valueRange = 0f..2f,
            defaultValue = 1f,
            onValueChange = onSaturationChange,
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun AdjustSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    defaultValue: Float = 0f,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = String.format("%.0f%%", ((value - defaultValue) / (valueRange.endInclusive - valueRange.start)) * 200),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}
