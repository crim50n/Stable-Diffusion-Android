@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)

package dev.minios.pdaiv1.presentation.screen.gallery.detail

import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.core.model.UiText
import dev.minios.pdaiv1.core.model.asString
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.sharing.shareFile
import com.shifthackz.android.core.mvi.MviComponent
import dev.minios.pdaiv1.domain.entity.AiGenerationResult
import dev.minios.pdaiv1.domain.entity.ServerSource
import dev.minios.pdaiv1.presentation.modal.ModalRenderer
import dev.minios.pdaiv1.presentation.theme.colors
import dev.minios.pdaiv1.presentation.utils.Constants
import dev.minios.pdaiv1.presentation.widget.image.ZoomableImage
import dev.minios.pdaiv1.presentation.widget.image.ZoomableImageSource
import com.shifthackz.catppuccin.palette.Catppuccin
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import dev.minios.pdaiv1.presentation.navigation.LocalAnimatedVisibilityScope
import dev.minios.pdaiv1.presentation.navigation.LocalSharedTransitionScope
import dev.minios.pdaiv1.presentation.navigation.galleryImageSharedKey
import dev.minios.pdaiv1.core.localization.R as LocalizationR
import dev.minios.pdaiv1.presentation.R as PresentationR

@Composable
fun GalleryDetailScreen(
    itemId: Long,
    onNavigateBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val fileProviderDescriptor: FileProviderDescriptor = koinInject()
    val galleryDetailSharing: GalleryDetailSharing = koinInject()
    MviComponent(
        viewModel = koinViewModel<GalleryDetailViewModel>(
            key = "gallery_detail_$itemId",
            parameters = { parametersOf(itemId, onNavigateBack) },
        ),
        processEffect = { effect ->
            when (effect) {
                is GalleryDetailEffect.ShareImageFile -> context.shareFile(
                    file = effect.file,
                    fileProviderPath = fileProviderDescriptor.providerPath,
                    fileMimeType = Constants.MIME_TYPE_JPG,
                )

                is GalleryDetailEffect.ShareGenerationParams -> galleryDetailSharing(
                    context = context,
                    state = effect.state,
                )

                is GalleryDetailEffect.ShareClipBoard -> {
                    clipboardManager.setText(AnnotatedString(effect.text))
                }

                GalleryDetailEffect.ImageSavedToGallery -> {
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
    state: GalleryDetailState,
    processIntent: (GalleryDetailIntent) -> Unit = {},
) {
    val isImageTab = state.selectedTab == GalleryDetailState.Tab.IMAGE ||
                     state.selectedTab == GalleryDetailState.Tab.ORIGINAL
    val showControls = state.controlsVisible || !isImageTab

    // Track swipe progress for controls fade
    var controlsAlpha by remember { mutableFloatStateOf(1f) }

    // No background here - GalleryDetailContentState handles its own animated background
    // This allows the gallery grid to be visible underneath during swipe-to-dismiss

    Box(
        modifier = modifier,
    ) {
        // Image content - fills entire screen when controls hidden
        when (state) {
            is GalleryDetailState.Content -> GalleryDetailContentState(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onCopyTextClick = {
                    processIntent(GalleryDetailIntent.CopyToClipboard(it))
                },
                onPageChanged = { page ->
                    processIntent(GalleryDetailIntent.PageChanged(page))
                },
                onImageTap = {
                    if (isImageTab) {
                        processIntent(GalleryDetailIntent.ToggleControlsVisibility)
                    }
                },
                onSwipeDown = {
                    processIntent(GalleryDetailIntent.NavigateBack)
                },
                onSwipeUp = {
                    processIntent(GalleryDetailIntent.ShowInfoBottomSheet)
                },
                onDragProgressChanged = { alpha ->
                    controlsAlpha = alpha
                },
            )

            is GalleryDetailState.Loading -> Unit
        }

        // Top bar overlay with gradient background for visibility on black
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(200)) +
                    slideInVertically(animationSpec = tween(200)) { -it },
            exit = fadeOut(animationSpec = tween(200)) +
                   slideOutVertically(animationSpec = tween(200)) { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .graphicsLayer { alpha = controlsAlpha },
        ) {
            var showDropdownMenu by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isImageTab) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        }
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Back button
                    IconButton(
                        onClick = { processIntent(GalleryDetailIntent.NavigateBack) },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back button",
                            tint = if (isImageTab) Color.White else LocalContentColor.current,
                        )
                    }

                    // Menu button
                    AnimatedVisibility(
                        visible = state.selectedTab != GalleryDetailState.Tab.INFO,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box {
                            IconButton(
                                onClick = { showDropdownMenu = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = if (isImageTab) Color.White else LocalContentColor.current,
                                )
                            }
                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(LocalizationR.string.gallery_save_to_gallery)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        processIntent(GalleryDetailIntent.SaveToGallery)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Save, contentDescription = null)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(LocalizationR.string.gallery_info_field_prompt)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        processIntent(GalleryDetailIntent.Export.Params)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Share, contentDescription = null)
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(LocalizationR.string.gallery_tab_info)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        processIntent(GalleryDetailIntent.ShowInfoBottomSheet)
                                    },
                                    leadingIcon = {
                                        Image(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(id = PresentationR.drawable.ic_text),
                                            contentDescription = null,
                                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom bar overlay with gradient for visibility on black
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(200)) +
                    slideInVertically(animationSpec = tween(200)) { it },
            exit = fadeOut(animationSpec = tween(200)) +
                   slideOutVertically(animationSpec = tween(200)) { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer { alpha = controlsAlpha },
        ) {
            GalleryDetailNavigationBar(
                state = state,
                isImageTab = isImageTab,
                processIntent = processIntent,
            )
        }

        ModalRenderer(screenModal = state.screenModal) {
            (it as? GalleryDetailIntent)?.let(processIntent::invoke)
        }

        // Info Bottom Sheet (shown on swipe up)
        if (state.showInfoBottomSheet && state is GalleryDetailState.Content) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

            ModalBottomSheet(
                onDismissRequest = {
                    processIntent(GalleryDetailIntent.HideInfoBottomSheet)
                },
                sheetState = sheetState,
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
                GalleryDetailsTable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    state = state,
                    onCopyTextClick = { text ->
                        processIntent(GalleryDetailIntent.CopyToClipboard(text))
                    },
                )
            }
        }
    }
}

@Composable
private fun GalleryDetailNavigationBar(
    state: GalleryDetailState,
    isImageTab: Boolean,
    processIntent: (GalleryDetailIntent) -> Unit = {},
) {
    val iconTint = if (isImageTab) Color.White else LocalContentColor.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isImageTab) {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f),
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface,
                        )
                    )
                }
            )
    ) {
        Column {
            if (state is GalleryDetailState.Content) {
                if (state.showReportButton) {
                    OutlinedButton(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        onClick = { processIntent(GalleryDetailIntent.Report) },
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = iconTint,
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = iconTint.copy(alpha = 0.5f),
                        ),
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 8.dp),
                            imageVector = Icons.Default.Report,
                            contentDescription = "Report",
                            tint = iconTint,
                        )
                        Text(
                            text = stringResource(LocalizationR.string.report_title),
                            color = iconTint,
                        )
                    }
                }
                // Action buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    if (state.currentSource == ServerSource.FAL_AI) {
                        // Fal AI button
                        IconButton(
                            onClick = { processIntent(GalleryDetailIntent.SendTo.FalAi) },
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = PresentationR.drawable.ic_text),
                                contentDescription = "Fal AI",
                                tint = iconTint,
                            )
                        }
                    } else {
                        // txt2img button
                        IconButton(
                            onClick = { processIntent(GalleryDetailIntent.SendTo.Txt2Img) },
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = PresentationR.drawable.ic_text),
                                contentDescription = "txt2img",
                                tint = iconTint,
                            )
                        }
                        // img2img button
                        IconButton(
                            onClick = { processIntent(GalleryDetailIntent.SendTo.Img2Img) },
                        ) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(id = PresentationR.drawable.ic_image),
                                contentDescription = "img2img",
                                tint = iconTint,
                            )
                        }
                    }
                    // Visibility toggle
                    IconButton(
                        onClick = { processIntent(GalleryDetailIntent.ToggleVisibility) },
                    ) {
                        Icon(
                            imageVector = if (state.hidden) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = "Toggle visibility",
                            tint = iconTint,
                        )
                    }
                    // Like toggle
                    IconButton(
                        onClick = { processIntent(GalleryDetailIntent.ToggleLike) },
                    ) {
                        Icon(
                            imageVector = if (state.liked) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = "Toggle like",
                            tint = if (state.liked) Color.Red else iconTint,
                        )
                    }
                    // Share button
                    IconButton(
                        onClick = { processIntent(GalleryDetailIntent.Export.Image) },
                    ) {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = PresentationR.drawable.ic_share),
                            contentDescription = "Share",
                            colorFilter = ColorFilter.tint(iconTint),
                        )
                    }
                    // Delete button
                    IconButton(
                        onClick = { processIntent(GalleryDetailIntent.Delete.Request) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = iconTint,
                        )
                    }
                }
            }

            // Bottom padding for navigation bar height
            Spacer(modifier = Modifier.height(16.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun GalleryDetailContentState(
    modifier: Modifier = Modifier,
    state: GalleryDetailState.Content,
    onCopyTextClick: (CharSequence) -> Unit = {},
    onPageChanged: (Int) -> Unit = {},
    onImageTap: () -> Unit = {},
    onSwipeDown: () -> Unit = {},
    onSwipeUp: () -> Unit = {},
    onDragProgressChanged: (Float) -> Unit = {},
) {
    // Animate background appearance for Immich-style effect
    val backgroundAnimatable = remember { Animatable(1f) }

    // Track current drag alpha from ZoomableImage
    var currentDragAlpha by remember { mutableFloatStateOf(1f) }

    // Combined background alpha
    val backgroundAlpha = backgroundAnimatable.value * currentDragAlpha

    // Notify parent about drag progress for controls fade
    LaunchedEffect(backgroundAlpha) {
        onDragProgressChanged(backgroundAlpha)
    }

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = backgroundAlpha)),
    ) {
        // Get shared transition scope for Immich-style hero animation
        val sharedTransitionScope = LocalSharedTransitionScope.current
        val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

        // Remember the initial gallery ID for shared element transition
        // This ensures the hero animation targets the correct image
        val initialGalleryId = remember { state.id }

        when (state.selectedTab) {
            GalleryDetailState.Tab.IMAGE -> {
                if (state.galleryIds.size > 1) {
                    // key() ensures pager is recreated with correct initialPage after deletion
                    key(state.galleryIds) {
                        val pagerState = rememberPagerState(
                            initialPage = state.currentIndex,
                            pageCount = { state.galleryIds.size }
                        )

                        // Haptic feedback on page change (like Immich)
                        val view = LocalView.current
                        LaunchedEffect(pagerState) {
                            snapshotFlow { pagerState.settledPage }
                                .collect { page ->
                                    if (page != state.currentIndex) {
                                        // Haptic feedback like Immich's selectionClick
                                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                        onPageChanged(page)
                                    }
                                }
                        }

                        // Animate to target page (swipe effect for deletion)
                        LaunchedEffect(state.animateToPage) {
                            state.animateToPage?.let { targetPage ->
                                if (targetPage in 0 until pagerState.pageCount &&
                                    pagerState.currentPage != targetPage) {
                                    pagerState.animateScrollToPage(targetPage)
                                }
                            }
                        }

                        // Fast scroll physics like Immich (FastClampingScrollPhysics)
                        // No bounce to avoid showing placeholder of adjacent page
                        val flingBehavior = PagerDefaults.flingBehavior(
                            state = pagerState,
                            snapAnimationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMedium,
                            ),
                        )

                        HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize(),
                        beyondViewportPageCount = 0, // Don't preload - avoids showing unloaded pages during overscroll
                        flingBehavior = flingBehavior,
                    ) { page ->
                        // Get bitmap from cache, or use current bitmap for current page
                        val pageBitmap = state.getBitmapForPage(page)
                            ?: if (page == state.currentIndex) state.bitmap else null

                        // Get gallery ID for this page for shared element
                        val pageGalleryId = state.galleryIds.getOrNull(page) ?: 0L

                        // Apply shared element only to the initially opened image
                        val pageSharedModifier = if (
                            sharedTransitionScope != null &&
                            animatedVisibilityScope != null &&
                            pageGalleryId == initialGalleryId
                        ) {
                            with(sharedTransitionScope) {
                                Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(key = galleryImageSharedKey(pageGalleryId)),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                )
                            }
                        } else {
                            Modifier
                        }

                        if (pageBitmap != null) {
                            ZoomableImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(pageSharedModifier),
                                source = ZoomableImageSource.Bmp(pageBitmap),
                                backgroundColor = Color.Transparent,
                                hideImage = page == state.currentIndex && state.hidden,
                                consumeGesturesWhenNotZoomed = false,
                                onTap = onImageTap,
                                onSwipeUp = onSwipeUp,
                                onSwipeDown = onSwipeDown,
                                onDragProgress = { alpha ->
                                    currentDragAlpha = alpha
                                },
                            )
                        } else {
                            // Blur placeholder while loading (like Immich's loadingBuilder)
                            // Try to get thumbnail from cache for blur effect
                            val thumbnailBitmap = state.getThumbnailForPage(page)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (thumbnailBitmap != null) {
                                    // Show blurred thumbnail as placeholder
                                    // RenderEffect requires API 31+
                                    val blurModifier = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                        Modifier.graphicsLayer {
                                            renderEffect = android.graphics.RenderEffect
                                                .createBlurEffect(30f, 30f, android.graphics.Shader.TileMode.CLAMP)
                                                .asComposeRenderEffect()
                                        }
                                    } else {
                                        Modifier
                                    }
                                    Image(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .then(blurModifier),
                                        bitmap = thumbnailBitmap.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                    )
                                }
                                // Loading indicator on top
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = Color.White.copy(alpha = 0.7f),
                                    strokeWidth = 3.dp,
                                )
                            }
                        }
                        }
                    }
                } else {
                    // Single image - apply shared element
                    val singleImageSharedModifier = if (
                        sharedTransitionScope != null &&
                        animatedVisibilityScope != null
                    ) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = galleryImageSharedKey(state.id)),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                        }
                    } else {
                        Modifier
                    }
                    ZoomableImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(singleImageSharedModifier),
                        source = ZoomableImageSource.Bmp(state.bitmap),
                        backgroundColor = Color.Transparent,
                        hideImage = state.hidden,
                        onTap = onImageTap,
                        onSwipeUp = onSwipeUp,
                        onSwipeDown = onSwipeDown,
                        onDragProgress = { alpha ->
                            currentDragAlpha = alpha
                        },
                    )
                }
            }

            GalleryDetailState.Tab.ORIGINAL -> state.inputBitmap?.let { bmp ->
                ZoomableImage(
                    modifier = Modifier.fillMaxSize(),
                    source = ZoomableImageSource.Bmp(bmp),
                    backgroundColor = Color.Transparent,
                    onTap = onImageTap,
                    onSwipeUp = onSwipeUp,
                    onSwipeDown = onSwipeDown,
                    onDragProgress = { alpha ->
                        currentDragAlpha = alpha
                    },
                )
            }

            GalleryDetailState.Tab.INFO -> GalleryDetailsTable(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(top = 64.dp),
                state = state,
                onCopyTextClick = onCopyTextClick,
            )
        }
    }
}

@Composable
private fun GalleryDetailsTable(
    modifier: Modifier = Modifier,
    state: GalleryDetailState.Content,
    onCopyTextClick: (CharSequence) -> Unit = {},
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        val colorOddBg = MaterialTheme.colorScheme.surface
        val colorOddText = colors(
            light = Catppuccin.Latte.Text,
            dark = Catppuccin.Frappe.Text
        )
        val colorEvenBg = MaterialTheme.colorScheme.surfaceTint
        GalleryDetailRow(
            modifier = Modifier.background(color = colorOddBg),
            name = LocalizationR.string.gallery_info_field_date.asUiText(),
            value = state.createdAt,
            color = colorOddText,
            onCopyTextClick = onCopyTextClick,
        )
        GalleryDetailRow(
            modifier = Modifier.background(color = colorEvenBg),
            name = LocalizationR.string.gallery_info_field_type.asUiText(),
            value = state.type,
            color = colorOddText,
            onCopyTextClick = onCopyTextClick,
        )
        if (state.modelName.asString().isNotBlank()) {
            GalleryDetailRow(
                modifier = Modifier.background(color = colorOddBg),
                name = LocalizationR.string.gallery_info_field_model.asUiText(),
                value = state.modelName,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
        }
        if (state.isFalAi) {
            // Fal AI specific fields
            GalleryDetailRow(
                modifier = Modifier.background(color = colorOddBg),
                name = "Endpoint".asUiText(),
                value = state.sampler,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
        }
        GalleryDetailRow(
            modifier = Modifier.background(color = if (state.isFalAi) colorEvenBg else colorOddBg),
            name = LocalizationR.string.gallery_info_field_prompt.asUiText(),
            value = state.prompt,
            color = colorOddText,
            onCopyTextClick = onCopyTextClick,
        )
        if (!state.isFalAi || state.negativePrompt.asString().isNotBlank()) {
            GalleryDetailRow(
                modifier = Modifier.background(color = if (state.isFalAi) colorOddBg else colorEvenBg),
                name = LocalizationR.string.gallery_info_field_negative_prompt.asUiText(),
                value = state.negativePrompt,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
        }
        GalleryDetailRow(
            modifier = Modifier.background(color = colorOddBg),
            name = LocalizationR.string.gallery_info_field_size.asUiText(),
            value = state.size,
            color = colorOddText,
            onCopyTextClick = onCopyTextClick,
        )
        if (!state.isFalAi) {
            // SD specific fields - hide for Fal AI
            GalleryDetailRow(
                modifier = Modifier.background(color = colorEvenBg),
                name = LocalizationR.string.gallery_info_field_sampling_steps.asUiText(),
                value = state.samplingSteps,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
            GalleryDetailRow(
                modifier = Modifier.background(color = colorOddBg),
                name = LocalizationR.string.gallery_info_field_cfg.asUiText(),
                value = state.cfgScale,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
            GalleryDetailRow(
                modifier = Modifier.background(color = colorEvenBg),
                name = LocalizationR.string.gallery_info_field_restore_faces.asUiText(),
                value = state.restoreFaces,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
            GalleryDetailRow(
                modifier = Modifier.background(color = colorOddBg),
                name = LocalizationR.string.gallery_info_field_sampler.asUiText(),
                value = state.sampler,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
        }
        if (state.seed.asString().isNotBlank()) {
            GalleryDetailRow(
                modifier = Modifier.background(color = colorEvenBg),
                name = LocalizationR.string.gallery_info_field_seed.asUiText(),
                value = state.seed,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
        }
        if (!state.isFalAi) {
            GalleryDetailRow(
                modifier = Modifier.background(color = colorOddBg),
                name = LocalizationR.string.gallery_info_field_sub_seed.asUiText(),
                value = state.subSeed,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
            GalleryDetailRow(
                modifier = Modifier.background(color = colorEvenBg),
                name = LocalizationR.string.gallery_info_field_sub_seed_strength.asUiText(),
                value = state.subSeedStrength,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
            if (state.generationType == AiGenerationResult.Type.IMAGE_TO_IMAGE) GalleryDetailRow(
                modifier = Modifier.background(color = colorOddBg),
                name = LocalizationR.string.gallery_info_field_denoising_strength.asUiText(),
                value = state.denoisingStrength,
                color = colorOddText,
                onCopyTextClick = onCopyTextClick,
            )
        }
    }
}

@Composable
private fun GalleryDetailRow(
    modifier: Modifier = Modifier,
    column1Weight: Float = 0.4f,
    column2Weight: Float = 0.6f,
    name: UiText,
    value: UiText,
    color: Color,
    onCopyTextClick: (CharSequence) -> Unit = {},
) {
    val rawValue = value.asString()
    Row(modifier) {
        GalleryDetailCell(
            text = name,
            modifier = Modifier.weight(column1Weight),
            color = color,
        )
        GalleryDetailCell(
            text = value,
            modifier = Modifier.weight(column2Weight),
            color = color,
        )
        if (rawValue.isNotBlank()) {
            IconButton(
                onClick = { onCopyTextClick(rawValue) },
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun GalleryDetailCell(
    modifier: Modifier = Modifier,
    text: UiText,
    color: Color,
) {
    Text(
        modifier = modifier
            .padding(start = 12.dp)
            .padding(vertical = 8.dp),
        text = text.asString(),
        color = color,
    )
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewGalleryScreenTxt2ImgContentTabImage() {
    ScreenContent(state = mockGalleryDetailTxt2Img)
}

@Composable
@Preview(showSystemUi = true, showBackground = true)
private fun PreviewGalleryScreenTxt2ImgContentTabInfo() {
    ScreenContent(state = mockGalleryDetailTxt2Img.copy(selectedTab = GalleryDetailState.Tab.INFO))
}
