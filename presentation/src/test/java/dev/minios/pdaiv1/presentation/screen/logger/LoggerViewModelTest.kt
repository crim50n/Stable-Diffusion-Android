package dev.minios.pdaiv1.presentation.screen.logger

import dev.minios.pdaiv1.core.common.file.FileProviderDescriptor
import dev.minios.pdaiv1.presentation.core.CoreViewModelTest
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.stub.stubDispatchersProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Ignore("ToDo: Investigate why sometimes tests fail on remote worker due to race-conditions.")
class LoggerViewModelTest : CoreViewModelTest<LoggerViewModel>() {

    private val stubFileProviderDescriptor = mockk<FileProviderDescriptor>()
    private val stubMainRouter = mockk<MainRouter>()

    override fun initializeViewModel() = LoggerViewModel(
        dispatchersProvider = stubDispatchersProvider,
        fileProviderDescriptor = stubFileProviderDescriptor,
        mainRouter = stubMainRouter,
    )

    @Before
    override fun initialize() {
        super.initialize()
        every {
            stubFileProviderDescriptor.logsCacheDirPath
        } returns "/tmp/local"
    }

    @Test
    fun `initialize, read logs, expected loaded state`() {
        runTest {
            val expected = LoggerState(
                loading = false,
                text = ""
            )
            val actual = viewModel.state.value
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given received NavigateBack intent, expected router navigateBack() called`() {
        every {
            stubMainRouter.navigateBack()
        } returns Unit

        viewModel.processIntent(LoggerIntent.NavigateBack)

        verify {
            stubMainRouter.navigateBack()
        }
    }
}
