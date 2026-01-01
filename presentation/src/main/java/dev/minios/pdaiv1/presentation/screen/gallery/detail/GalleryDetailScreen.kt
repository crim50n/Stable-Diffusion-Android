@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package dev.minios.pdaiv1.presentation.screen.gallery.detail

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import dev.minios.pdaiv1.core.localization.R as LocalizationR
import dev.minios.pdaiv1.presentation.R as PresentationR

@Composable
fun GalleryDetailScreen(itemId: Long) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val fileProviderDescriptor: FileProviderDescriptor = koinInject()
    val galleryDetailSharing: GalleryDetailSharing = koinInject()
    MviComponent(
        viewModel = koinViewModel<GalleryDetailViewModel>(
            parameters = { parametersOf(itemId) },
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

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
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
            )

            is GalleryDetailState.Loading -> Unit
        }

        // Top bar overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(200)) +
                    slideInVertically(animationSpec = tween(200)) { -it },
            exit = fadeOut(animationSpec = tween(200)) +
                   slideOutVertically(animationSpec = tween(200)) { -it },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            CenterAlignedTopAppBar(
                title = {},
                modifier = Modifier.statusBarsPadding(),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            processIntent(GalleryDetailIntent.NavigateBack)
                        },
                        content = {
                            Icon(
                                Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back button",
                            )
                        },
                    )
                },
                actions = {
                    AnimatedVisibility(
                        visible = state.selectedTab != GalleryDetailState.Tab.INFO,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Row {
                            IconButton(
                                onClick = { processIntent(GalleryDetailIntent.SaveToGallery) },
                                content = {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Save",
                                    )
                                },
                            )
                            IconButton(
                                onClick = { processIntent(GalleryDetailIntent.Export.Image) },
                                content = {
                                    Image(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(id = PresentationR.drawable.ic_share),
                                        contentDescription = "Export",
                                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                                    )
                                },
                            )
                        }
                    }
                }
            )
        }

        // Bottom bar overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(200)) +
                    slideInVertically(animationSpec = tween(200)) { it },
            exit = fadeOut(animationSpec = tween(200)) +
                   slideOutVertically(animationSpec = tween(200)) { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            GalleryDetailNavigationBar(
                state = state,
                processIntent = processIntent,
            )
        }

        ModalRenderer(screenModal = state.screenModal) {
            (it as? GalleryDetailIntent)?.let(processIntent::invoke)
        }
    }
}

@Composable
private fun GalleryDetailNavigationBar(
    state: GalleryDetailState,
    processIntent: (GalleryDetailIntent) -> Unit = {},
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
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
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 8.dp),
                            imageVector = Icons.Default.Report,
                            contentDescription = "Report",
                        )
                        Text(
                            text = stringResource(LocalizationR.string.report_title),
                            color = LocalContentColor.current
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                if (state.currentSource == ServerSource.FAL_AI) {
                    // Fal AI button - show for all images when Fal AI is active source
                    IconButton(
                        onClick = { processIntent(GalleryDetailIntent.SendTo.FalAi) },
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = PresentationR.drawable.ic_text),
                            contentDescription = "Fal AI",
                            tint = LocalContentColor.current,
                        )
                    }
                } else {
                    // Standard txt2img/img2img buttons for all other sources
                    IconButton(
                        onClick = { processIntent(GalleryDetailIntent.SendTo.Txt2Img) },
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = PresentationR.drawable.ic_text),
                            contentDescription = "txt2img",
                            tint = LocalContentColor.current,
                        )
                    }
                    IconButton(
                        onClick = { processIntent(GalleryDetailIntent.SendTo.Img2Img) },
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = PresentationR.drawable.ic_image),
                            contentDescription = "img2img",
                            tint = LocalContentColor.current,
                        )
                    }
                }
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
                    )
                }
                IconButton(
                    onClick = { processIntent(GalleryDetailIntent.Export.Params) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share prompt",
                    )
                }
                IconButton(
                    onClick = { processIntent(GalleryDetailIntent.Delete.Request) },
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                    )
                }
            }
        }
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            state.tabs.forEach { tab ->
                NavigationBarItem(
                    selected = state.selectedTab == tab,
                    label = {
                        Text(
                            text = stringResource(id = tab.label),
                            color = LocalContentColor.current,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors().copy(
                        selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                    ),
                    icon = {
                        Image(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(tab.iconRes),
                            contentDescription = stringResource(id = LocalizationR.string.gallery_tab_image),
                            colorFilter = ColorFilter.tint(LocalContentColor.current),
                        )
                    },
                    onClick = { processIntent(GalleryDetailIntent.SelectTab(tab)) },
                )
            }
        }
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
) {
    Column(
        modifier = modifier,
    ) {
        when (state.selectedTab) {
            GalleryDetailState.Tab.IMAGE -> {
                if (state.galleryIds.size > 1) {
                    // key() ensures pager is recreated with correct initialPage after deletion
                    key(state.galleryIds) {
                        val pagerState = rememberPagerState(
                            initialPage = state.currentIndex,
                            pageCount = { state.galleryIds.size }
                        )

                        LaunchedEffect(pagerState) {
                            snapshotFlow { pagerState.settledPage }
                                .collect { page ->
                                    if (page != state.currentIndex) {
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

                        HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        beyondViewportPageCount = 1,
                    ) { page ->
                        // Get bitmap from cache, or use current bitmap for current page
                        val pageBitmap = state.getBitmapForPage(page)
                            ?: if (page == state.currentIndex) state.bitmap else null

                        if (pageBitmap != null) {
                            ZoomableImage(
                                modifier = Modifier.fillMaxSize(),
                                source = ZoomableImageSource.Bmp(pageBitmap),
                                hideImage = page == state.currentIndex && state.hidden,
                                consumeGesturesWhenNotZoomed = false,
                                onTap = onImageTap,
                            )
                        } else {
                            // Show loading indicator while bitmap is being loaded
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        }
                    }
                } else {
                    ZoomableImage(
                        modifier = Modifier.fillMaxSize(),
                        source = ZoomableImageSource.Bmp(state.bitmap),
                        hideImage = state.hidden,
                        onTap = onImageTap,
                    )
                }
            }

            GalleryDetailState.Tab.ORIGINAL -> state.inputBitmap?.let { bmp ->
                ZoomableImage(
                    modifier = Modifier.fillMaxSize(),
                    source = ZoomableImageSource.Bmp(bmp),
                    onTap = onImageTap,
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
