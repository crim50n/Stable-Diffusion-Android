package dev.minios.pdaiv1.presentation.modal.history

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import dev.minios.pdaiv1.presentation.screen.gallery.list.GalleryPagingSource
import dev.minios.pdaiv1.presentation.utils.Constants
import com.shifthackz.android.core.mvi.EmptyEffect
import com.shifthackz.android.core.mvi.EmptyIntent
import com.shifthackz.android.core.mvi.EmptyState
import kotlinx.coroutines.flow.Flow

class InputHistoryViewModel(
    dispatchersProvider: DispatchersProvider,
    private val getGenerationResultPagedUseCase: GetGenerationResultPagedUseCase,
    private val base64ToBitmapConverter: Base64ToBitmapConverter,
    private val schedulersProvider: SchedulersProvider,
) : MviRxViewModel<EmptyState, EmptyIntent, EmptyEffect>() {

    override val initialState = EmptyState

    override val effectDispatcher = dispatchersProvider.immediate

    private val config = PagingConfig(
        pageSize = Constants.PAGINATION_PAYLOAD_SIZE,
        initialLoadSize = Constants.PAGINATION_PAYLOAD_SIZE
    )

    private val pager: Pager<Int, InputHistoryItemUi> = Pager(
        config = config,
        initialKey = GalleryPagingSource.FIRST_KEY,
        pagingSourceFactory = {
            InputHistoryPagingSource(
                getGenerationResultPagedUseCase,
                base64ToBitmapConverter,
                schedulersProvider,
            )
        }
    )

    val pagingFlow: Flow<PagingData<InputHistoryItemUi>> = pager.flow
}
