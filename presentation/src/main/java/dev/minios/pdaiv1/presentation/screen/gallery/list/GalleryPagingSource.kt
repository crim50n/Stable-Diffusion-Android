package dev.minios.pdaiv1.presentation.screen.gallery.list

import android.graphics.Bitmap
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.ThumbnailGenerator
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import dev.minios.pdaiv1.presentation.utils.Constants
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

typealias GalleryPagedResult = PagingSource.LoadResult<Int, GalleryGridItemUi>

/**
 * Data class for passing image info through the processing pipeline
 */
private data class ImageInfo(
    val id: Long,
    val hidden: Boolean,
    val base64Image: String,
)

/**
 * Immich-style paging source that loads thumbnails in parallel.
 * Uses high concurrency and processes items as they complete,
 * maintaining order via sorting after parallel loading.
 */
class GalleryPagingSource(
    private val getGenerationResultPagedUseCase: GetGenerationResultPagedUseCase,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val schedulersProvider: SchedulersProvider,
) : RxPagingSource<Int, GalleryGridItemUi>() {

    override fun getRefreshKey(state: PagingState<Int, GalleryGridItemUi>) = FIRST_KEY

    override fun loadSingle(params: LoadParams<Int>) = loadSingleImpl(params)

    private fun loadSingleImpl(params: LoadParams<Int>): Single<GalleryPagedResult> {
        val pageSize = params.loadSize
        val pageNext = params.key ?: FIRST_KEY
        return getGenerationResultPagedUseCase(
            limit = pageSize,
            offset = pageNext * Constants.PAGINATION_PAYLOAD_SIZE,
        )
            .subscribeOn(schedulersProvider.io) // Use IO for database access
            .flatMapObservable { list -> Observable.fromIterable(list) }
            .map { ai -> ImageInfo(ai.id, ai.hidden, ai.image) }
            // High concurrency parallel thumbnail loading (like Immich)
            .flatMap(
                { info: ImageInfo ->
                    thumbnailGenerator.generate(
                        id = info.id.toString(),
                        base64ImageString = info.base64Image,
                    )
                        .map { bitmap -> Triple(info.id, info.hidden, bitmap) }
                        .toObservable()
                        .subscribeOn(schedulersProvider.computation) // Each thumbnail on computation
                },
                MAX_CONCURRENT_LOADS,
            )
            .map { triple -> mapToUi(triple) }
            .toList()
            // Sort by id descending to maintain order after parallel loading
            .map { payload -> payload.sortedByDescending { item -> item.id } }
            .map { payload ->
                Wrapper(
                    LoadResult.Page(
                        data = payload,
                        prevKey = if (pageNext == FIRST_KEY) null else pageNext - 1,
                        nextKey = if (payload.isEmpty()) null else pageNext + 1,
                    )
                )
            }
            .onErrorReturn { t: Throwable ->
                errorLog(t)
                Wrapper(LoadResult.Error(t))
            }
            .map { wrapper -> wrapper.loadResult }
    }

    private data class Wrapper(val loadResult: GalleryPagedResult)

    private fun mapToUi(data: Triple<Long, Boolean, Bitmap>) = GalleryGridItemUi(
        id = data.first,
        bitmap = data.third,
        hidden = data.second,
    )

    companion object {
        const val FIRST_KEY = 0
        // Very high concurrency for fast gallery loading (Immich uses parallel streams)
        const val MAX_CONCURRENT_LOADS = 32
    }
}
