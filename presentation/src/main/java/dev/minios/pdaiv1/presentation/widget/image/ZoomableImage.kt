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
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import dev.minios.pdaiv1.presentation.R as PresentationR

sealed interface ZoomableImageSource {
    data class Bmp(val bitmap: Bitmap) : ZoomableImageSource
    data class Resource(@DrawableRes val resId: Int) : ZoomableImageSource
}

/**
 * Allows to implement image zoom pinch, rotate behavior gestures
 *
 * Source: https://stackoverflow.com/questions/66005066/android-jetpack-compose-how-to-zoom-a-image-in-a-box
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
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val width = configuration.screenWidthDp
    val scope = rememberCoroutineScope()

    val initialScale = remember(source, density) { calculateInitialScale(source, width, density) }
    val scale = remember { Animatable(initialScale) }
    val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // Threshold to consider image as "zoomed" - slightly above initial scale
    val zoomThreshold = initialScale * 1.05f

    // Target scale for double-tap zoom (2x from initial)
    val doubleTapScale = initialScale * 2f

    // For double-tap detection
    var lastTapTime by remember { mutableLongStateOf(0L) }
    val doubleTapTimeout = 300L
    val tapMovementThreshold = 50f

    Box(
        modifier = modifier
            .clip(RectangleShape)
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(initialScale, doubleTapScale, zoomThreshold, consumeGesturesWhenNotZoomed) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    val downPosition = down.position
                    var totalMovement = Offset.Zero
                    var wasPinch = false
                    var wasMovement = false

                    do {
                        val event = awaitPointerEvent()
                        val zoom = event.calculateZoom()
                        val pan = event.calculatePan()

                        // Check if this is a pinch gesture (2+ fingers)
                        val isPinch = event.changes.size >= 2
                        if (isPinch) wasPinch = true

                        totalMovement += pan
                        val movedSignificantly = totalMovement.getDistance() > tapMovementThreshold
                        if (movedSignificantly) wasMovement = true

                        // Calculate new scale
                        val newScale = scale.value * zoom
                        val isZoomed = scale.value > zoomThreshold

                        // Apply zoom (always allow pinch-to-zoom)
                        if (isPinch) {
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
                        } else if (movedSignificantly && (isZoomed || consumeGesturesWhenNotZoomed)) {
                            // Single finger pan - only consume if zoomed or explicitly allowed
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
                        // If not zoomed and consumeGesturesWhenNotZoomed=false,
                        // don't consume - let parent (HorizontalPager) handle it
                    } while (event.changes.any { it.pressed })

                    // Handle taps only if no significant movement or pinch occurred
                    if (!wasPinch && !wasMovement) {
                        val currentTime = System.currentTimeMillis()
                        val timeSinceLastTap = currentTime - lastTapTime

                        if (timeSinceLastTap < doubleTapTimeout) {
                            // Double tap detected
                            lastTapTime = 0L
                            scope.launch {
                                if (scale.value > zoomThreshold) {
                                    // Already zoomed - reset to initial scale with animation
                                    launch { scale.animateTo(initialScale, tween(300)) }
                                    launch { offset.animateTo(Offset.Zero, tween(300)) }
                                } else {
                                    // Not zoomed - zoom to 2x with animation
                                    scale.animateTo(doubleTapScale.coerceAtMost(maxScale), tween(300))
                                }
                            }
                        } else {
                            // Single tap - wait a bit to see if it's a double tap
                            lastTapTime = currentTime
                            scope.launch {
                                kotlinx.coroutines.delay(doubleTapTimeout)
                                if (lastTapTime == currentTime) {
                                    // No second tap came, it's a single tap
                                    onTap()
                                }
                            }
                        }
                    }
                }
            },
    ) {
        val effectiveMinScale = maxOf(minScale, initialScale)
        val currentScale = scale.value
        val imageModifier = Modifier
            .align(Alignment.Center)
            .graphicsLayer(
                scaleX = maxOf(effectiveMinScale, minOf(maxScale, currentScale)),
                scaleY = maxOf(effectiveMinScale, minOf(maxScale, currentScale)),
                translationX = offset.value.x,
                translationY = offset.value.y,
            )
            .then(
                if (!hideImage) Modifier
                else Modifier.graphicsLayer {
                    renderEffect = BlurEffect(
                        radiusX = hideBlurRadius,
                        radiusY = hideBlurRadius,
                    )
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
//        if (hideImage) {
//            Icon(
//                modifier = Modifier
//                    .size(28.dp)
//                    .align(Alignment.Center),
//                imageVector = Icons.Default.VisibilityOff,
//                contentDescription = "hidden",
//                tint = MaterialTheme.colorScheme.primary,
//            )
//        }
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
