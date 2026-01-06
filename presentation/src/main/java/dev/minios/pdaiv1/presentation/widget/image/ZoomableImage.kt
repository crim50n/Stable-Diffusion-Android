package dev.minios.pdaiv1.presentation.widget.image

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import dev.minios.pdaiv1.presentation.R as PresentationR

sealed interface ZoomableImageSource {
    data class Bmp(val bitmap: Bitmap) : ZoomableImageSource
    data class Resource(@DrawableRes val resId: Int) : ZoomableImageSource
}

/**
 * Zoomable image with Immich-style gesture handling:
 * - Separate gesture recognizers for taps vs transforms (like Flutter's RawGestureDetector)
 * - Double-tap to toggle zoom (initial ↔ 2x)
 * - Single tap to toggle UI
 * - Pinch to zoom
 * - Pan when zoomed
 * - Vertical swipe to dismiss/show info
 */
@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    source: ZoomableImageSource,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    minScale: Float = 1f,
    maxScale: Float = 6f,
    hideImage: Boolean = false,
    hideBlurRadius: Float = 69f,
    consumeGesturesWhenNotZoomed: Boolean = true,
    onTap: () -> Unit = {},
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
    onDragProgress: ((Float) -> Unit)? = null,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val width = configuration.screenWidthDp
    val scope = rememberCoroutineScope()

    val initialScale = remember(source, density) { calculateInitialScale(source, width, density) }
    val scale = remember { Animatable(initialScale) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // Reset scale and offset when source changes (fixes artifact when swiping between pages)
    LaunchedEffect(source) {
        scale.snapTo(initialScale)
        offset.snapTo(Offset.Zero)
    }

    // Threshold to consider image as "zoomed" - slightly above initial scale
    val zoomThreshold = initialScale * 1.05f

    // Target scale for double-tap zoom (2x from initial)
    val doubleTapScale = initialScale * 2f

    // For vertical swipe detection (swipe-to-dismiss)
    val swipeThreshold = 80f
    val maxDragDistance = 400f
    val verticalDragOffset = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .fillMaxSize()
            .background(backgroundColor)
            // Tap gesture detector (like Flutter's TapGestureRecognizer + DoubleTapGestureRecognizer)
            // This runs FIRST and handles taps independently from transform gestures
            .pointerInput(initialScale, doubleTapScale, zoomThreshold) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        // Double tap: toggle between initial and 2x scale (like Immich's nextScaleState)
                        scope.launch {
                            if (scale.value > zoomThreshold) {
                                // Zoomed in → reset to initial (like Immich's PhotoViewScaleState.initial)
                                launch { scale.animateTo(initialScale, tween(300)) }
                                launch { offset.animateTo(Offset.Zero, tween(300)) }
                            } else {
                                // Not zoomed → zoom to 2x centered on tap point
                                val targetScale = doubleTapScale.coerceAtMost(maxScale)
                                // Calculate offset to zoom toward tap point
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val focusX = tapOffset.x - centerX
                                val focusY = tapOffset.y - centerY
                                val scaleDelta = targetScale / scale.value
                                val newOffsetX = offset.value.x - focusX * (scaleDelta - 1)
                                val newOffsetY = offset.value.y - focusY * (scaleDelta - 1)

                                launch { scale.animateTo(targetScale, tween(300)) }
                                launch { offset.animateTo(Offset(newOffsetX, newOffsetY), tween(300)) }
                            }
                        }
                    },
                    onTap = {
                        // Single tap: toggle UI visibility
                        onTap()
                    }
                )
            }
            // Transform gesture detector (pinch zoom, pan, vertical swipe)
            .pointerInput(initialScale, zoomThreshold, onSwipeUp, onSwipeDown, onDragProgress) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var verticalSwipeStarted = false
                    var accumulatedVerticalDrag = 0f

                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        val isPinch = event.changes.size >= 2
                        val newScale = scale.value * zoom
                        val isZoomed = scale.value > zoomThreshold

                        if (isPinch) {
                            // Pinch to zoom
                            scope.launch {
                                scale.snapTo(newScale.coerceIn(minScale, maxScale))
                                offset.snapTo(
                                    Offset(
                                        offset.value.x + pan.x * zoom,
                                        offset.value.y + pan.y * zoom
                                    )
                                )
                            }
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } else if (!isZoomed && (onSwipeUp != null || onSwipeDown != null)) {
                            // Not zoomed - handle vertical swipes for dismiss/info
                            val isVerticalDrag = kotlin.math.abs(pan.y) > kotlin.math.abs(pan.x) * 1.5f

                            if (isVerticalDrag) {
                                accumulatedVerticalDrag += pan.y
                            }

                            val significantVerticalMovement = kotlin.math.abs(accumulatedVerticalDrag) > 30f
                            if (isVerticalDrag && significantVerticalMovement) {
                                verticalSwipeStarted = true
                            }

                            if (verticalSwipeStarted) {
                                if (accumulatedVerticalDrag > 0) {
                                    val dragProgress = (accumulatedVerticalDrag / maxDragDistance).coerceIn(0f, 1f)
                                    onDragProgress?.invoke(1f - dragProgress)
                                    scope.launch { verticalDragOffset.snapTo(accumulatedVerticalDrag) }
                                }
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        } else if (isZoomed) {
                            // Zoomed - handle panning
                            scope.launch {
                                offset.snapTo(
                                    Offset(
                                        offset.value.x + pan.x,
                                        offset.value.y + pan.y
                                    )
                                )
                            }
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    // Handle vertical swipe completion
                    if (verticalSwipeStarted) {
                        when {
                            accumulatedVerticalDrag > swipeThreshold -> onSwipeDown?.invoke()
                            accumulatedVerticalDrag < -swipeThreshold -> {
                                onSwipeUp?.invoke()
                                scope.launch { verticalDragOffset.snapTo(0f) }
                            }
                            else -> {
                                scope.launch {
                                    verticalDragOffset.animateTo(0f, tween(200))
                                    onDragProgress?.invoke(1f)
                                }
                            }
                        }
                    }
                }
            },
    ) {
        val effectiveMinScale = maxOf(minScale, initialScale)
        val currentScale = scale.value

        // Calculate swipe-to-dismiss visual effects
        val swipeDragProgress = (verticalDragOffset.value.coerceAtLeast(0f) / maxDragDistance).coerceIn(0f, 1f)
        val swipeScale = 1f - (swipeDragProgress * 0.3f)

        val imageModifier = Modifier
            .align(Alignment.Center)
            .graphicsLayer(
                scaleX = maxOf(effectiveMinScale, minOf(maxScale, currentScale)) * swipeScale,
                scaleY = maxOf(effectiveMinScale, minOf(maxScale, currentScale)) * swipeScale,
                translationX = offset.value.x,
                translationY = offset.value.y + verticalDragOffset.value.coerceAtLeast(0f),
                clip = hideImage, // Clip blur to image bounds
            )
            .then(
                // BlurEffect only works on Android 12+ (API 31)
                if (!hideImage) {
                    Modifier
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        renderEffect = BlurEffect(
                            radiusX = hideBlurRadius,
                            radiusY = hideBlurRadius,
                            edgeTreatment = TileMode.Decal, // Don't extend blur beyond image edges
                        )
                    }
                } else {
                    // For Android < 12, make image nearly invisible (overlay will cover it)
                    Modifier.graphicsLayer { alpha = 0.05f }
                }
            )

        when (source) {
            is ZoomableImageSource.Bmp -> Image(
                modifier = imageModifier,
                contentDescription = null,
                bitmap = source.bitmap.asImageBitmap(),
            )

            is ZoomableImageSource.Resource -> Image(
                modifier = imageModifier,
                contentDescription = null,
                painter = painterResource(id = source.resId),
            )
        }
        
        // Overlay for Android < 12 when hiding image (fallback for no blur support)
        if (hideImage && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "hidden",
                    tint = Color.White.copy(alpha = 0.5f),
                )
            }
        }
    }
}

private fun calculateInitialScale(
    source: ZoomableImageSource,
    screenWidthDp: Int,
    density: Float,
): Float {
    if (source is ZoomableImageSource.Bmp) {
        val screenWidthPx = screenWidthDp * density
        return screenWidthPx / source.bitmap.width.toFloat()
    }
    return 1f
}

@Preview
@Composable
private fun ZoomableImagePreview() {
    ZoomableImage(
        modifier = Modifier.fillMaxSize(),
        source = ZoomableImageSource.Resource(PresentationR.drawable.ic_gallery)
    )
}
