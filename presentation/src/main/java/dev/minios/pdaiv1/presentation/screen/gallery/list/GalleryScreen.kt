@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class,
)

package dev.minios.pdaiv1.presentation.screen.gallery.list

import android.content.Intent
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import dev.minios.pdaiv1.presentation.screen.gallery.list.selection.rememberDragSelectionState
import dev.minios.pdaiv1.presentation.screen.gallery.list.selection.DragSelectionUtils
import dev.minios.pdaiv1.presentation.components.DraggableScrollbar
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.core.extensions.items
import dev.minios.pdaiv1.core.extensions.shake
import dev.minios.pdaiv1.core.extensions.shimmer
import dev.minios.pdaiv1.core.sharing.shareFile
import dev.minios.pdaiv1.domain.entity.Grid
import dev.minios.pdaiv1.presentation.R
import dev.minios.pdaiv1.presentation.modal.ModalRenderer
import dev.minios.pdaiv1.presentation.screen.drawer.DrawerIntent
import dev.minios.pdaiv1.presentation.utils.Constants
import dev.minios.pdaiv1.presentation.widget.work.BackgroundWorkWidget
import com.shifthackz.android.core.mvi.MviComponent
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import dev.minios.pdaiv1.presentation.navigation.LocalSetHideBottomNavigation
import dev.minios.pdaiv1.presentation.navigation.LocalSharedTransitionScope
import dev.minios.pdaiv1.presentation.navigation.LocalAnimatedVisibilityScope
import dev.minios.pdaiv1.presentation.navigation.galleryImageSharedKey
import dev.minios.pdaiv1.presentation.screen.gallery.detail.GalleryDetailScreen
import androidx.compose.runtime.CompositionLocalProvider
import dev.minios.pdaiv1.core.localization.R as LocalizationR

@Composable
fun GalleryScreen() {
    val viewModel = koinViewModel<GalleryViewModel>()
    val context = LocalContext.current
    val fileProviderDescriptor: FileProviderDescriptor = koinInject()
    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect) {
                is GalleryEffect.OpenUri -> with(Intent(Intent.ACTION_VIEW)) {
                    setDataAndType(effect.uri, DocumentsContract.Document.MIME_TYPE_DIR)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(this)
                }

                is GalleryEffect.Share -> context.shareFile(
                    file = effect.zipFile,
                    fileProviderPath = fileProviderDescriptor.providerPath,
                    fileMimeType = Constants.MIME_TYPE_ZIP,
                )

                GalleryEffect.Refresh -> {
                    // Immich-style: IDs are reloaded in ViewModel's loadAllIds()
                    // Thumbnail cache is preserved, will reload thumbnails as needed
                }

                GalleryEffect.AllImagesSavedToGallery -> {
                    Toast.makeText(
                        context,
                        context.getString(LocalizationR.string.gallery_save_all_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                }

                GalleryEffect.SelectionImagesSavedToGallery -> {
                    Toast.makeText(
                        context,
                        context.getString(LocalizationR.string.gallery_save_selection_success),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        },
    ) { state, intentHandler ->
        BackHandler(state.selectionMode || state.selectedItemId != null) {
            when {
                state.selectedItemId != null -> intentHandler(GalleryIntent.CloseItem)
                state.selectionMode -> intentHandler(GalleryIntent.ChangeSelectionMode(false))
            }
        }
        GalleryScreenContent(
            state = state,
            processIntent = intentHandler,
        )
    }
}

@Composable
fun GalleryScreenContent(
    modifier: Modifier = Modifier,
    state: GalleryState,
    processIntent: (GalleryIntent) -> Unit = {},
) {
    val listState = rememberLazyGridState()

    // Check if we have a scroll target on first composition (returning from detail)
    val hasScrollTarget = state.scrollToItemIndex != null
    var isRestoringScroll by remember { mutableStateOf(hasScrollTarget) }

    // Pinch-to-zoom gesture state
    var accumulatedZoom by remember { mutableStateOf(1f) }
    val zoomThreshold = 0.3f

    // Drag selection state
    val dragSelectionState = rememberDragSelectionState()
    var dragStartIndex by remember { mutableIntStateOf(-1) }
    var dragCurrentIndex by remember { mutableIntStateOf(-1) }

    // UI visibility state for scroll hide/show behavior
    // toolbarOffsetHeightPx: 0f = fully visible, -topBarHeightPx = fully hidden
    var toolbarOffsetHeightPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val topBarContentHeight = 72.dp // Height of top bar content (without status bar)
    val statusBarHeightDp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = topBarContentHeight + statusBarHeightDp
    val bottomNavHeight = 80.dp // Approximate height of bottom navigation
    val topBarHeightPx = with(density) { topBarHeight.toPx() }

    // Get callback to hide/show bottom navigation in HomeNavigationScreen
    val setHideBottomNavigation = LocalSetHideBottomNavigation.current

    // NestedScrollConnection for hiding/showing toolbar - standard Android pattern
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = toolbarOffsetHeightPx + delta
                toolbarOffsetHeightPx = newOffset.coerceIn(-topBarHeightPx, 0f)
                return Offset.Zero // Don't consume scroll, let grid handle it
            }
        }
    }
    
    // Update bottom navigation visibility when toolbar offset changes
    LaunchedEffect(toolbarOffsetHeightPx) {
        val isHidden = toolbarOffsetHeightPx < -topBarHeightPx / 2
        setHideBottomNavigation?.invoke(isHidden)
    }
    
    // Show toolbar when at top of list
    LaunchedEffect(listState) {
        snapshotFlow { 
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 50 
        }.collect { isAtTop ->
            if (isAtTop) {
                toolbarOffsetHeightPx = 0f
            }
        }
    }

    // Calculate if UI should be shown (for selection mode override)
    // In selection mode, always show UI regardless of offset
    val effectiveToolbarOffset = if (state.selectionMode || state.selectedItemId != null) 0f else toolbarOffsetHeightPx

    // Reset toolbar offset when returning from detail
    LaunchedEffect(state.selectedItemId) {
        if (state.selectedItemId == null) {
            toolbarOffsetHeightPx = 0f
        }
    }

    // Scroll to saved position - Immich-style: use allIds since all IDs are loaded upfront
    LaunchedEffect(Unit) {
        val targetIndex = state.scrollToItemIndex
        if (targetIndex == null) {
            isRestoringScroll = false
            return@LaunchedEffect
        }

        // Wait for IDs to load (they load instantly at startup)
        while (state.allIds.isEmpty() && state.isInitialLoading) {
            delay(50)
        }

        // With Immich-style loading, all IDs are available immediately
        // Just scroll to target - no need to wait for items to load
        if (targetIndex < state.allIds.size) {
            listState.scrollToItem(targetIndex)
        }
        isRestoringScroll = false
        processIntent(GalleryIntent.ClearScrollPosition)
    }

    // Show shimmer while restoring
    val showShimmerForScrollRestore = isRestoringScroll

    // Immich-style: check empty based on allIds after initial load completes
    val emptyStatePredicate: () -> Boolean = {
        !state.isInitialLoading && state.allIds.isEmpty()
    }

    // Trigger thumbnail loading for visible items
    LaunchedEffect(listState, state.allIds) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            layoutInfo.visibleItemsInfo.mapNotNull { item ->
                // Only consider actual grid items (not spacers)
                if (item.index < state.allIds.size) {
                    state.allIds.getOrNull(item.index)
                } else null
            }
        }.collect { visibleIds ->
            if (visibleIds.isNotEmpty()) {
                // Also prefetch some items before and after visible range
                val firstVisible = listState.firstVisibleItemIndex
                val prefetchRange = 30 // Load 30 items before/after visible
                val startIndex = (firstVisible - prefetchRange).coerceAtLeast(0)
                val endIndex = (firstVisible + listState.layoutInfo.visibleItemsInfo.size + prefetchRange)
                    .coerceAtMost(state.allIds.size)
                val idsToLoad = state.allIds.subList(startIndex, endIndex)
                processIntent(GalleryIntent.LoadThumbnails(idsToLoad))
            }
        }
    }

    Box(modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // No topBar/bottomBar - they're rendered as overlays to avoid layout jumps
        ) { _ ->
            when {
                emptyStatePredicate() -> GalleryEmptyState(Modifier.fillMaxSize())

                // Immich-style: show shimmer while initial IDs are loading
                state.isInitialLoading -> LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize(),
                    columns = GridCells.Fixed(state.grid.size),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomNavHeight + 32.dp, top = topBarHeight),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val max = when (state.grid) {
                        Grid.Fixed1 -> 3
                        Grid.Fixed2 -> 6
                        Grid.Fixed3 -> 12
                        Grid.Fixed4 -> 20
                        Grid.Fixed5 -> 30
                        Grid.Fixed6 -> 42
                    }
                    repeat(max) {
                        item(it) {
                            GalleryUiItemShimmer()
                        }
                    }
                }

                else -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(state.grid) {
                            awaitEachGesture {
                                // Wait for first pointer - use pass Final so children get events first
                                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
                                do {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Final)
                                    // Only handle pinch-to-zoom with 2+ pointers
                                    if (event.changes.size >= 2) {
                                        val zoom = event.calculateZoom()
                                        accumulatedZoom *= zoom

                                        when {
                                            accumulatedZoom > 1f + zoomThreshold -> {
                                                accumulatedZoom = 1f
                                                processIntent(GalleryIntent.GridZoom.ZoomIn)
                                            }
                                            accumulatedZoom < 1f - zoomThreshold -> {
                                                accumulatedZoom = 1f
                                                processIntent(GalleryIntent.GridZoom.ZoomOut)
                                            }
                                        }

                                        event.changes.forEach { it.consume() }
                                    }
                                } while (event.changes.any { it.pressed })
                                accumulatedZoom = 1f
                            }
                        }
                ) {
                    // Calculate item size for drag selection hit testing
                    val contentPaddingPx = with(density) { 16.dp.toPx() }
                    val spacingPx = with(density) { 16.dp.toPx() }
                    var gridWidth by remember { mutableFloatStateOf(0f) }
                    var gridHeight by remember { mutableFloatStateOf(0f) }
                    val itemSizePx = remember(gridWidth, state.grid.size) {
                        if (gridWidth > 0 && state.grid.size > 0) {
                            (gridWidth - contentPaddingPx * 2 - spacingPx * (state.grid.size - 1)) / state.grid.size
                        } else 0f
                    }
                    val topPaddingPx = with(density) { topBarHeight.toPx() }
                    val itemWithSpacingPx = itemSizePx + spacingPx

                    // Track which items are being dragged for proper deselection
                    var draggedIds by remember { mutableStateOf(setOf<Long>()) }

                    // Auto-scroll state
                    val dragScope = rememberCoroutineScope()
                    var autoScrollJob by remember { mutableStateOf<Job?>(null) }
                    var currentDragPosition by remember { mutableStateOf<androidx.compose.ui.geometry.Offset?>(null) }

                    // Helper function to calculate item index from position
                    fun calculateIndexFromPosition(offset: androidx.compose.ui.geometry.Offset): Int {
                        if (itemSizePx <= 0 || itemWithSpacingPx <= 0) return -1

                        // Y position adjusted for top content padding
                        val adjustedY = offset.y - topPaddingPx

                        val col = ((offset.x - contentPaddingPx) / itemWithSpacingPx).toInt()
                            .coerceIn(0, state.grid.size - 1)

                        // Calculate which row in the visible area
                        val rowInView = (adjustedY / itemWithSpacingPx).toInt()

                        // Account for scroll: firstVisibleItemIndex and scroll offset
                        val firstVisibleRow = listState.firstVisibleItemIndex / state.grid.size
                        // Scroll offset is how much the first visible row is scrolled out (positive = scrolled up)
                        // We need to add fractional rows based on scroll offset
                        val scrollOffsetRows = listState.firstVisibleItemScrollOffset / itemWithSpacingPx

                        // If adjustedY is negative, we're in the content padding area above first item
                        val actualRow = if (adjustedY < 0) {
                            // Above visible content, clamp to first visible row
                            firstVisibleRow
                        } else {
                            // Add scroll offset to account for partial scroll
                            (firstVisibleRow.toFloat() + rowInView.toFloat() + scrollOffsetRows).toInt()
                        }

                        return (actualRow * state.grid.size + col).coerceIn(0, state.allIds.size - 1)
                    }

                    // Auto-scroll effect - runs when drag is active and position is at edges
                    LaunchedEffect(dragSelectionState.isActive, currentDragPosition) {
                        if (!dragSelectionState.isActive || currentDragPosition == null) {
                            autoScrollJob?.cancel()
                            return@LaunchedEffect
                        }

                        val pos = currentDragPosition!!
                        val edgeThreshold = gridHeight * 0.10f // 10% of height

                        val scrollDirection = when {
                            pos.y < edgeThreshold -> -1 // Scroll up
                            pos.y > gridHeight - edgeThreshold -> 1 // Scroll down
                            else -> 0
                        }

                        if (scrollDirection != 0) {
                            autoScrollJob?.cancel()
                            autoScrollJob = dragScope.launch {
                                while (true) {
                                    val scrollAmount = if (scrollDirection > 0) 150f else -150f
                                    listState.dispatchRawDelta(scrollAmount)

                                    // Update selection after scroll - same position now points to different index
                                    val currentIndex = calculateIndexFromPosition(pos)
                                    if (currentIndex >= 0 && currentIndex != dragCurrentIndex) {
                                        val range = DragSelectionUtils.calculateSelectedRange(dragStartIndex, currentIndex)
                                        val newDraggedIds = range.mapNotNull { idx ->
                                            state.allIds.getOrNull(idx)
                                        }.toSet()

                                        // Deselect items no longer in range
                                        val toDeselect = draggedIds - newDraggedIds
                                        toDeselect.forEach { id ->
                                            processIntent(GalleryIntent.ToggleItemSelection(id))
                                        }

                                        // Select items newly in range
                                        val toSelect = newDraggedIds - draggedIds
                                        toSelect.forEach { id ->
                                            processIntent(GalleryIntent.ToggleItemSelection(id))
                                        }

                                        draggedIds = newDraggedIds
                                        dragCurrentIndex = currentIndex
                                    }

                                    delay(50)
                                }
                            }
                        } else {
                            autoScrollJob?.cancel()
                        }
                    }

                    LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                        .onGloballyPositioned { coordinates ->
                            gridWidth = coordinates.size.width.toFloat()
                            gridHeight = coordinates.size.height.toFloat()
                        }
                        .pointerInput(state.grid.size, state.allIds.size) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    if (itemSizePx <= 0) {
                                        return@detectDragGesturesAfterLongPress
                                    }

                                    val index = calculateIndexFromPosition(offset)
                                    if (index < 0) {
                                        return@detectDragGesturesAfterLongPress
                                    }

                                    dragStartIndex = index
                                    dragCurrentIndex = index
                                    dragSelectionState.startDrag(offset, index)
                                    currentDragPosition = offset

                                    // Enter selection mode
                                    if (!state.selectionMode) {
                                        processIntent(GalleryIntent.ChangeSelectionMode(true))
                                    }
                                    
                                    // Select first item
                                    val id = state.allIds.getOrNull(index)
                                    if (id != null) {
                                        draggedIds = setOf(id)
                                        // Toggle selection for first item
                                        processIntent(GalleryIntent.ToggleItemSelection(id))
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (!dragSelectionState.isActive || itemSizePx <= 0) return@detectDragGesturesAfterLongPress
                                    change.consume()

                                    val offset = change.position
                                    currentDragPosition = offset

                                    val currentIndex = calculateIndexFromPosition(offset)
                                    if (currentIndex < 0) return@detectDragGesturesAfterLongPress

                                    if (currentIndex != dragCurrentIndex) {
                                        // Calculate range between anchor and current
                                        val range = DragSelectionUtils.calculateSelectedRange(dragStartIndex, currentIndex)

                                        // Collect new set of IDs in range
                                        val newDraggedIds = range.mapNotNull { idx ->
                                            state.allIds.getOrNull(idx)
                                        }.toSet()

                                        // Deselect items no longer in range
                                        val toDeselect = draggedIds - newDraggedIds
                                        toDeselect.forEach { id ->
                                            processIntent(GalleryIntent.ToggleItemSelection(id))
                                        }

                                        // Select items newly in range
                                        val toSelect = newDraggedIds - draggedIds
                                        toSelect.forEach { id ->
                                            processIntent(GalleryIntent.ToggleItemSelection(id))
                                        }

                                        draggedIds = newDraggedIds
                                        dragCurrentIndex = currentIndex
                                    }
                                    dragSelectionState.updateDrag(offset)
                                },
                                onDragEnd = {
                                    autoScrollJob?.cancel()
                                    currentDragPosition = null
                                    dragSelectionState.endDrag()
                                    dragStartIndex = -1
                                    dragCurrentIndex = -1
                                    draggedIds = emptySet()
                                },
                                onDragCancel = {
                                    autoScrollJob?.cancel()
                                    currentDragPosition = null
                                    dragSelectionState.endDrag()
                                    dragStartIndex = -1
                                    dragCurrentIndex = -1
                                    draggedIds = emptySet()
                                }
                            )
                        },
                    columns = GridCells.Fixed(state.grid.size),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = bottomNavHeight + 32.dp,
                        top = topBarHeight
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    state = listState,
                ) {
                    // Immich-style: use allIds for count, build items from thumbnailCache
                    items(
                        count = state.allIds.size,
                        key = { index -> state.allIds.getOrNull(index) ?: index.toLong() },
                    ) { index ->
                        val id = state.allIds[index]
                        val bitmap = state.thumbnailCache[id]
                        val hidden = state.hiddenIds.contains(id)
                        val liked = state.likedIds.contains(id)
                        val blurHash = state.blurHashCache[id] ?: ""
                        val item = GalleryGridItemUi(id = id, bitmap = bitmap, hidden = hidden, liked = liked, blurHash = blurHash)
                        val selected = state.selection.contains(id)
                        GalleryUiItem(
                            modifier = Modifier
                                .animateItem(tween(300))
                                .then(
                                    if (state.selectionMode && !selected) {
                                        Modifier.shake(
                                            enabled = true,
                                            animationDurationMillis = 188,
                                            animationStartOffset = (id % 320).toInt(),
                                        )
                                    } else Modifier
                                ),
                            item = item,
                            selectionMode = state.selectionMode,
                            checked = selected,
                            onCheckedChange = {
                                processIntent(GalleryIntent.ToggleItemSelection(id))
                            },
                            onLongClick = {
                                // Long press is handled by drag selection gesture on grid level
                                // Only act if drag selection is not active (accessibility fallback)
                                if (!dragSelectionState.isActive && !state.selectionMode) {
                                    processIntent(GalleryIntent.ChangeSelectionMode(true))
                                    processIntent(GalleryIntent.ToggleItemSelection(id))
                                }
                            },
                            onClick = {
                                processIntent(GalleryIntent.OpenItem(id, index))
                            },
                        )
                    }
                    items(2) { Spacer(modifier = Modifier.height(32.dp)) }
                }
                    // Shimmer overlay while restoring scroll position
                    AnimatedVisibility(
                        visible = showShimmerForScrollRestore,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            columns = GridCells.Fixed(state.grid.size),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottomNavHeight + 32.dp, top = topBarHeight),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            val max = when (state.grid) {
                                Grid.Fixed1 -> 3
                                Grid.Fixed2 -> 6
                                Grid.Fixed3 -> 12
                                Grid.Fixed4 -> 20
                                Grid.Fixed5 -> 30
                                Grid.Fixed6 -> 42
                            }
                            repeat(max) {
                                item(it) {
                                    GalleryUiItemShimmer()
                                }
                            }
                        }
                    }

                    // Draggable scrollbar for fast navigation (Immich-style)
                    DraggableScrollbar(
                        lazyGridState = listState,
                        totalItems = state.allIds.size,
                        columns = state.grid.size,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(top = topBarHeight, bottom = bottomNavHeight + 32.dp, end = 4.dp),
                    )

                    // Multi-select indicator (Immich-style) - shows count and close button
                    AnimatedVisibility(
                        visible = state.selectionMode,
                        enter = fadeIn() + slideInVertically { -it },
                        exit = fadeOut() + slideOutVertically { -it },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 16.dp, top = topBarHeight + 8.dp),
                    ) {
                        androidx.compose.material3.ElevatedButton(
                            onClick = { processIntent(GalleryIntent.ChangeSelectionMode(false)) },
                            colors = androidx.compose.material3.ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel selection",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.selection.size.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
            }
        }

        // TopBar as overlay - slides up/down based on scroll offset
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = effectiveToolbarOffset
                }
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
        ) {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
                navigationIcon = {
                    AnimatedContent(
                        targetState = state.selectionMode,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "main_nav_icon_animation",
                        ) { isInSelectionMode ->
                            IconButton(
                                onClick = {
                                    val intent = if (isInSelectionMode) {
                                        GalleryIntent.ChangeSelectionMode(false)
                                    } else {
                                        GalleryIntent.Drawer(DrawerIntent.Open)
                                    }
                                    processIntent(intent)
                                },
                            ) {
                                Icon(
                                    imageVector = if (isInSelectionMode) {
                                        Icons.Default.Close
                                    } else {
                                        Icons.Default.Menu
                                    },
                                    contentDescription = if (isInSelectionMode) "Close" else "Menu",
                                )
                            }
                        }
                    },
                    title = {
                        AnimatedVisibility(
                            visible = !state.selectionMode,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            Text(
                                text = stringResource(id = LocalizationR.string.title_gallery),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                        }
                    },
                    actions = {
                        AnimatedContent(
                            targetState = state.selectionMode,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "action_nav_icon_animation",
                        ) { isInSelectionMode ->
                            if (isInSelectionMode) {
                                AnimatedVisibility(
                                    visible = state.selection.isNotEmpty(),
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                ) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                processIntent(GalleryIntent.LikeSelection)
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Like",
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                processIntent(GalleryIntent.HideSelection)
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VisibilityOff,
                                                contentDescription = "Hide",
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                processIntent(GalleryIntent.Delete.Selection.Request)
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                processIntent(GalleryIntent.SaveToGallery.Selection.Request)
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Save,
                                                contentDescription = "Save to Gallery",
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                processIntent(GalleryIntent.Export.Selection.Request)
                                            },
                                        ) {
                                            Image(
                                                modifier = Modifier.size(24.dp),
                                                painter = painterResource(id = R.drawable.ic_share),
                                                contentDescription = "Export",
                                                colorFilter = ColorFilter.tint(LocalContentColor.current),
                                            )
                                        }
                                    }
                                }
                            } else {
                                AnimatedVisibility(
                                    visible = state.allIds.isNotEmpty(),
                                    enter = fadeIn(),
                                    exit = fadeOut(),
                                ) {
                                    IconButton(
                                        onClick = {
                                            processIntent(GalleryIntent.Dropdown.Toggle)
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Dropdown",
                                        )
                                    }
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = state.dropdownMenuShow,
                            onDismissRequest = { processIntent(GalleryIntent.Dropdown.Close) },
                            containerColor = MaterialTheme.colorScheme.background,
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Checklist,
                                        contentDescription = "Dropdown",
                                        tint = LocalContentColor.current,
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(
                                            id = LocalizationR.string.gallery_menu_selection_mode,
                                        ),
                                    )
                                },
                                onClick = {
                                    processIntent(GalleryIntent.Dropdown.Close)
                                    processIntent(GalleryIntent.ChangeSelectionMode(true))
                                },
                            )
                            if (state.mediaStoreInfo.isNotEmpty) DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FileOpen,
                                        contentDescription = "Browse",
                                        tint = LocalContentColor.current,
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = LocalizationR.string.browse)
                                    )
                                },
                                onClick = {
                                    processIntent(GalleryIntent.Dropdown.Close)
                                    state.mediaStoreInfo.folderUri?.let {
                                        processIntent(GalleryIntent.OpenMediaStoreFolder(it))
                                    }
                                },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Image(
                                        modifier = Modifier.size(24.dp),
                                        painter = painterResource(id = R.drawable.ic_share),
                                        contentDescription = "Export",
                                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = LocalizationR.string.gallery_menu_export_all)
                                    )
                                },
                                onClick = {
                                    processIntent(GalleryIntent.Dropdown.Close)
                                    processIntent(GalleryIntent.Export.All.Request)
                                },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Save to Gallery",
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = LocalizationR.string.gallery_menu_save_all)
                                    )
                                },
                                onClick = {
                                    processIntent(GalleryIntent.Dropdown.Close)
                                    processIntent(GalleryIntent.SaveToGallery.All.Request)
                                },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = LocalizationR.string.gallery_menu_delete_all),
                                    )
                                },
                                onClick = {
                                    processIntent(GalleryIntent.Dropdown.Close)
                                    processIntent(GalleryIntent.Delete.All.Request)
                                },
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.HeartBroken,
                                        contentDescription = "Delete unliked",
                                    )
                                },
                                text = {
                                    Text(
                                        text = stringResource(id = LocalizationR.string.gallery_menu_delete_unliked),
                                    )
                                },
                                onClick = {
                                    processIntent(GalleryIntent.Dropdown.Close)
                                    processIntent(GalleryIntent.Delete.AllUnliked.Request)
                                },
                            )
                        }
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                )
            }

        // Immich-style: Gallery Detail as overlay with shared element transitions
        // This keeps the gallery visible underneath during swipe-to-dismiss animation
        // Remember the last itemId to show during exit animation
        var lastItemId by remember { mutableStateOf<Long?>(null) }
        if (state.selectedItemId != null) {
            lastItemId = state.selectedItemId
        }

        val sharedTransitionScope = LocalSharedTransitionScope.current
        sharedTransitionScope?.let { transitionScope ->
            with(transitionScope) {
                AnimatedVisibility(
                    visible = state.selectedItemId != null,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)),
                ) {
                    lastItemId?.let { itemId ->
                        CompositionLocalProvider(
                            LocalAnimatedVisibilityScope provides this@AnimatedVisibility
                        ) {
                            key(itemId) {
                                GalleryDetailOverlay(
                                    itemId = itemId,
                                    onDismiss = { processIntent(GalleryIntent.CloseItem) },
                                )
                            }
                        }
                    }
                }
            }
        } ?: run {
            // Fallback without shared element transitions
            state.selectedItemId?.let { itemId ->
                key(itemId) {
                    GalleryDetailOverlay(
                        itemId = itemId,
                        onDismiss = { processIntent(GalleryIntent.CloseItem) },
                    )
                }
            }
        }

        ModalRenderer(screenModal = state.screenModal) {
            (it as? GalleryIntent)?.let(processIntent::invoke)
        }
    }
}

@Composable
fun GalleryUiItem(
    modifier: Modifier = Modifier,
    item: GalleryGridItemUi,
    checked: Boolean = false,
    onClick: (GalleryGridItemUi) -> Unit = {},
    onLongClick: () -> Unit = {},
    onCheckedChange: (Boolean) -> Unit = {},
    selectionMode: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor by animateColorAsState(
        targetValue = if (selectionMode && checked) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.Transparent
        },
        label = "border_color",
    )

    // Get shared transition scope for Immich-style hero animation
    val sharedTransitionScope = LocalSharedTransitionScope.current

    Box(
        modifier = modifier.clip(shape),
    ) {
        val imageModifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(
                width = 4.dp,
                color = borderColor,
                shape = shape,
            )
            .then(
                if (!item.hidden) {
                    Modifier
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    Modifier.graphicsLayer {
                        clip = true
                        renderEffect = BlurEffect(
                            radiusX = 100f,
                            radiusY = 100f,
                            edgeTreatment = TileMode.Decal,
                        )
                    }
                } else {
                    // Fallback for Android < 12
                    Modifier.graphicsLayer { alpha = 0.1f }
                }
            )
            .combinedClickable(
                onLongClick = null, // Long press handled by drag selection on grid level
                onClick = {
                    if (!selectionMode) {
                        onClick(item)
                    } else {
                        onCheckedChange(!checked)
                    }
                },
            )

        // Apply shared element modifier when AnimatedVisibilityScope is available
        // This works when navigating from GalleryFull route in main NavHost
        val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
        val finalModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
            with(sharedTransitionScope) {
                imageModifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = galleryImageSharedKey(item.id)),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        } else {
            imageModifier
        }

        // Show placeholder if bitmap not loaded yet (Immich-style lazy loading)
        val imageBitmap = item.imageBitmap
        if (imageBitmap != null) {
            Image(
                modifier = finalModifier,
                bitmap = imageBitmap,
                contentScale = ContentScale.Crop,
                contentDescription = "gallery_item",
            )
        } else if (item.blurHash.isNotEmpty()) {
            // BlurHash placeholder
            val blurHashBitmap = remember(item.blurHash) {
                dev.minios.pdaiv1.core.imageprocessing.blurhash.BlurHashDecoder.decodeStatic(item.blurHash, 32, 32)
            }
            if (blurHashBitmap != null) {
                Image(
                    modifier = finalModifier,
                    bitmap = blurHashBitmap.asImageBitmap(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "gallery_item_placeholder",
                )
            } else {
                // Shimmer placeholder if BlurHash decode failed
                Box(
                    modifier = finalModifier.shimmer()
                )
            }
        } else {
            // Shimmer placeholder while loading (no BlurHash available)
            Box(
                modifier = finalModifier.shimmer()
            )
        }
        if (item.hidden) {
            // Overlay for Android < 12 (no blur support)
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                )
            }
            Icon(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center),
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = "hidden",
                tint = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    MaterialTheme.colorScheme.primary
                } else {
                    Color.White.copy(alpha = 0.6f)
                },
            )
        }
        // Like indicator (top-right corner)
        if (item.liked && !selectionMode) {
            Icon(
                modifier = Modifier
                    .size(20.dp)
                    .padding(4.dp)
                    .align(Alignment.TopEnd),
                imageVector = Icons.Default.Favorite,
                contentDescription = "liked",
                tint = Color.Red,
            )
        }
        if (selectionMode) {
            val checkBoxShape = RoundedCornerShape(4.dp)
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
                    .align(Alignment.TopEnd)
                    .clip(checkBoxShape)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        shape = checkBoxShape,
                    ),
            ) {
                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = onCheckedChange,
                        colors = CheckboxDefaults.colors(
                            uncheckedColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun GalleryUiItemShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .shimmer()
    )
}

@Composable
private fun GalleryEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = stringResource(id = LocalizationR.string.gallery_empty_title),
            fontSize = 20.sp,
        )
        Text(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(id = LocalizationR.string.gallery_empty_sub_title),
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Immich-style overlay for gallery detail view.
 * Shows the detail screen as an overlay on top of the gallery grid,
 * allowing the gallery to remain visible during swipe-to-dismiss animation.
 */
@Composable
private fun GalleryDetailOverlay(
    itemId: Long,
    onDismiss: () -> Unit,
) {
    // Hide bottom navigation bar when detail overlay is shown
    val setHideBottomNavigation = LocalSetHideBottomNavigation.current

    LaunchedEffect(Unit) {
        setHideBottomNavigation?.invoke(true)
    }

    // Restore bottom navigation when dismissed
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            setHideBottomNavigation?.invoke(false)
        }
    }

    GalleryDetailScreen(
        itemId = itemId,
        onNavigateBack = onDismiss,
    )
}

/**
 * Data class to hold scroll state for snapshotFlow
 */
private data class GalleryScrollInfo(
    val firstVisibleItemIndex: Int,
    val firstVisibleItemScrollOffset: Int,
    val isScrollInProgress: Boolean,
)
