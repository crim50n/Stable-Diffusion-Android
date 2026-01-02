package dev.minios.pdaiv1.presentation.modal.history

import dev.minios.pdaiv1.core.imageprocessing.Base64ToBitmapConverter
import dev.minios.pdaiv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import dev.minios.pdaiv1.presentation.core.CoreViewModelTest
import dev.minios.pdaiv1.presentation.stub.stubDispatchersProvider
import dev.minios.pdaiv1.presentation.stub.stubSchedulersProvider
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import com.shifthackz.android.core.mvi.EmptyState

class InputHistoryViewModelTest : CoreViewModelTest<InputHistoryViewModel>() {

    private val stubGetGenerationResultPagedUseCase = mockk<GetGenerationResultPagedUseCase>()
    private val stubBase64ToBitmapConverter = mockk<Base64ToBitmapConverter>()

    override fun initializeViewModel() = InputHistoryViewModel(
        dispatchersProvider = stubDispatchersProvider,
        getGenerationResultPagedUseCase = stubGetGenerationResultPagedUseCase,
        base64ToBitmapConverter = stubBase64ToBitmapConverter,
        schedulersProvider = stubSchedulersProvider,
    )

    @Test
    fun `initialized, expected initialState is EmptyState`() {
        runTest {
            val state = viewModel.state.value
            Assert.assertEquals(EmptyState, state)
        }
    }

    @Test
    fun `initialized, expected pagingFlow is not null`() {
        runTest {
            Assert.assertNotNull(viewModel.pagingFlow)
        }
    }
}
