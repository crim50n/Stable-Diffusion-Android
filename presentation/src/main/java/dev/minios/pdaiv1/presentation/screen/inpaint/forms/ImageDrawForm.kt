package dev.minios.pdaiv1.presentation.screen.inpaint.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.minios.pdaiv1.presentation.screen.inpaint.InPaintIntent
import dev.minios.pdaiv1.presentation.screen.inpaint.InPaintState
import dev.minios.pdaiv1.presentation.screen.inpaint.components.InPaintComponent

@Composable
fun ImageDrawForm(
    modifier: Modifier = Modifier,
    state: InPaintState = InPaintState(),
    processIntent: (InPaintIntent) -> Unit = {},
) {
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            InPaintComponent(
                drawMode = state.isDrawing,
                inPaint = state.model,
                bitmap = state.bitmap,
                capWidth = state.size,
                scale = state.zoomScale,
                offsetX = state.zoomOffsetX,
                offsetY = state.zoomOffsetY,
                onScaleChanged = { processIntent(InPaintIntent.UpdateZoomScale(it)) },
                onOffsetChanged = { x, y -> processIntent(InPaintIntent.UpdateZoomOffset(x, y)) },
                onPathDrawn = { processIntent(InPaintIntent.DrawPath(it)) },
                onPathBitmapDrawn = { processIntent(InPaintIntent.DrawPathBmp(it)) },
            )
        }

        // Control buttons overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            FilledTonalIconButton(
                onClick = { processIntent(InPaintIntent.Action.ResetZoom) },
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                enabled = state.isZoomed,
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOutMap,
                    contentDescription = "Reset Zoom",
                )
            }
            if (state.isDrawing) {
                FilledIconButton(
                    onClick = { processIntent(InPaintIntent.Action.ToggleDrawing) },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.Draw,
                        contentDescription = "Drawing Mode",
                    )
                }
            } else {
                FilledTonalIconButton(
                    onClick = { processIntent(InPaintIntent.Action.ToggleDrawing) },
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Draw,
                        contentDescription = "Drawing Mode",
                    )
                }
            }
        }
    }
}
