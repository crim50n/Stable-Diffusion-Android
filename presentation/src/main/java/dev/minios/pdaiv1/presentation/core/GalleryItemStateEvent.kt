package dev.minios.pdaiv1.presentation.core

import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.subjects.PublishSubject

/**
 * Event bus for synchronizing gallery item states (hidden, liked) between
 * GalleryDetailViewModel and GalleryViewModel in real-time.
 */
class GalleryItemStateEvent {

    private val hiddenSubject: PublishSubject<HiddenChange> = PublishSubject.create()
    private val likedSubject: PublishSubject<LikedChange> = PublishSubject.create()

    fun emitHiddenChange(itemId: Long, hidden: Boolean) {
        hiddenSubject.onNext(HiddenChange(itemId, hidden))
    }

    fun emitLikedChange(itemId: Long, liked: Boolean) {
        likedSubject.onNext(LikedChange(itemId, liked))
    }

    fun observeHiddenChanges() = hiddenSubject.toFlowable(BackpressureStrategy.BUFFER)

    fun observeLikedChanges() = likedSubject.toFlowable(BackpressureStrategy.BUFFER)

    data class HiddenChange(val itemId: Long, val hidden: Boolean)
    data class LikedChange(val itemId: Long, val liked: Boolean)
}
