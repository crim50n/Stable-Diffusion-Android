package dev.minios.pdaiv1.presentation.screen.gallery.list

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter.Input
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter.Output
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import dev.minios.pdaiv1.presentation.utils.Constants
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

typealias GalleryPagedResult = PagingSource.LoadResult<Int, GalleryGridItemUi>

class GalleryPagingSource(
    private val getGenerationResultPagedUseCase: GetGenerationResultPagedUseCase,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
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
            .subscribeOn(schedulersProvider.computation)
            .flatMapObservable { list -> Observable.fromIterable(list) }
            .map { ai -> Triple(ai.id, ai.hidden, ai.image) }
            .map { triple -> Triple(triple.first, triple.second, Input(triple.third)) }
            // Use flatMap with maxConcurrency for parallel file loading
            .flatMap(
                { triple: Triple<Long, Boolean, Input> ->
                    base64ToBitmapConverter(triple.third)
                        .map { out -> Triple(triple.first, triple.second, out) }
                        .toObservable()
                },
                MAX_CONCURRENT_LOADS, // maxConcurrency - load multiple images in parallel
            )
            .map { triple -> mapOutputToUi(triple) }
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

    private fun mapOutputToUi(output: Triple<Long, Boolean, Output>) = GalleryGridItemUi(
        output.first,
        output.third.bitmap,
        output.second,
    )

    companion object {
        const val FIRST_KEY = 0
        const val MAX_CONCURRENT_LOADS = 8 // Parallel image loading for smoother scrolling
    }
}
