# PDAI v0.7.1 — Release Notes

## New Features

### Favorites (Like)

- "Favorite" button (heart) in image detail view
- Like indicator in gallery grid (red heart in the top right corner)
- Bulk like: "Like" button in selection mode likes all selected images
- "Delete Unliked" function in gallery menu — deletes all images without a like
- Real-time sync of like status between detail view and grid
- Like status persists between sessions

### Hide Images

- Bulk hide: "Hide" button in selection mode hides all selected images
- Real-time sync of hide status between detail view and grid
- Fallback for Android < 12: dimming with icon instead of blur

### Image Editor

Built-in editor for generated images:

- Rotate left/right
- Flip horizontally and vertically
- Adjust brightness, contrast, and saturation
- Save changes to original or as a new image

---

### Completely Redesigned Gallery with Improved Performance

**Smart Loading:**
- Thumbnails load only for visible items
- File-based thumbnail loading (no Base64) — fixed OOM on fast scrolling
- BlurHash placeholders — blurred preview while thumbnail loads
- Shimmer animation for items without BlurHash
- Two-level cache for thumbnails and full images

**Grid Management:**
- Grid size from 1 to 6 columns (previously 2-5)
- Pinch to resize thumbnails
- Draggable scrollbar for fast navigation

**Drag Selection:**
- Long press activates selection mode
- Dragging finger selects a range of images
- Dragging back deselects
- Auto-scroll when reaching screen edges
- Smooth animations when opening images

**Selection Mode Actions:**
- Like (heart) — like all selected
- Hide (eye) — hide all selected
- Delete — delete all selected
- Save to device gallery
- Export

**Update on Delete:**
- Gallery auto-updates when deleting images from detail view
- Gallery auto-updates after generation completes

---

### UI Improvements

**Floating Generation Indicator:**
- Global generation status widget over all screens
- Swipe left/right to temporarily hide
- Automatically appears on status change (generation start/result)
- Does not block navigation (drawer opens over widget)

**Collapsible Header:**
- Top bar hides on scroll down
- Appears on scroll up or when reaching top of list
- Standard NestedScrollConnection pattern (like Google Photos)
- Unified height of 72dp on all screens

**Swipe Navigation:**
- Swipe between home screen tabs (HorizontalPager)
- Drawer opens only by button (not edge swipe)

**Image Viewing:**
- Double tap to zoom/reset
- Swipe up/down to show/hide info
- Fixed artifacts when swiping between images

**Navigation Bar:**
- Smoothly hides in fullscreen view
- Automatically appears when returning to gallery

**Image Details:**
- "Share" button
- "Save to device gallery" button
- "Favorite" (like) button

---

## Fixes

### Image Export
- Fixed OOM when exporting many images
- Direct file copy instead of loading into memory (Base64)
- Parallel processing (4 threads) for faster export

### "Report" Button
- Disabled for Full and FOSS builds (Play Store only)

### Aspect Ratio
- Aspect ratio now always uses width as the base (not the longer side)

### Fal AI
- Fixed issue with API keys containing control characters
- `requestId` field in Fal AI response is now optional (fixed crash on fast completion)

---

## Visual Changes

- Updated notification icon

---

## Technical Changes

### Dependencies

- Added BlurHash 0.3.0 for blurred placeholders in gallery

### Database

- Migration v10 → v11: added `blur_hash` field to generation results table
- Migration v11 → v12: added `liked` field for favorites feature

### New Components

- `ImageEditor` — image editing screen
- `CollapsibleScaffold` — scaffold with collapsible header
- `DraggableScrollbar` — draggable scrollbar
- `DragSelectionState` / `DragSelectionUtils` — drag selection
- `ThumbnailGenerator` — thumbnail generator
- `ImageCacheManager` — image cache manager
- `BlurHashEncoder` / `BlurHashDecoder` — BlurHash encoding/decoding
- `GalleryItemStateEvent` — real-time sync of hide/like states
- `GetThumbnailInfoUseCase` / `GetGalleryItemsRawUseCase` — use cases for file-based thumbnail loading
- `ToggleLikeUseCase` / `DeleteAllUnlikedUseCase` — use cases for favorites feature
- `LikeItemsUseCase` / `HideItemsUseCase` — use cases for bulk operations

---

## Localization

Added strings:
- Share, Edit, Save to gallery
- Rotate, Settings, Brightness, Contrast, Saturation
- Delete unliked, confirm delete unliked

