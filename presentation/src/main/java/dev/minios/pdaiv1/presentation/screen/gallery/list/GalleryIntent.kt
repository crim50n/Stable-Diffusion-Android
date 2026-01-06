package dev.minios.pdaiv1.presentation.screen.gallery.list

import android.net.Uri
import dev.minios.pdaiv1.presentation.screen.drawer.DrawerIntent
import com.shifthackz.android.core.mvi.MviIntent

sealed interface GalleryIntent : MviIntent {

    sealed interface Export : GalleryIntent {

        enum class All : Export {
            Request, Confirm;
        }

        enum class Selection : Export {
            Request, Confirm;
        }
    }

    sealed interface Delete : GalleryIntent {

        enum class All : Export {
            Request, Confirm;
        }

        enum class AllUnliked : Delete {
            Request, Confirm;
        }

        enum class Selection : Delete {
            Request, Confirm;
        }
    }

    // Bulk actions for selected items
    data object LikeSelection : GalleryIntent
    data object HideSelection : GalleryIntent

    enum class Dropdown : GalleryIntent {
        Toggle, Show, Close;
    }

    data object DismissDialog : GalleryIntent

    data class OpenItem(val id: Long, val index: Int) : GalleryIntent

    data object CloseItem : GalleryIntent

    data object ClearScrollPosition : GalleryIntent

    data class OpenMediaStoreFolder(val uri: Uri) : GalleryIntent

    sealed interface SaveToGallery : GalleryIntent {
        enum class All : SaveToGallery {
            Request, Confirm;
        }
        enum class Selection : SaveToGallery {
            Request, Confirm;
        }
    }

    data class Drawer(val intent: DrawerIntent) : GalleryIntent

    data class ChangeSelectionMode(val flag: Boolean) : GalleryIntent

    data object UnselectAll : GalleryIntent

    data class ToggleItemSelection(val id: Long) : GalleryIntent

    sealed interface GridZoom : GalleryIntent {
        data object ZoomIn : GridZoom  // Less columns, bigger thumbnails
        data object ZoomOut : GridZoom // More columns, smaller thumbnails
    }

    sealed interface DragSelection : GalleryIntent {
        data class Start(val itemId: Long) : DragSelection
        data class UpdateRange(val fromIndex: Int, val toIndex: Int, val itemIds: List<Long>) : DragSelection
        data object End : DragSelection
    }

    // Immich-style lazy loading
    data class LoadThumbnails(val ids: List<Long>) : GalleryIntent
}
