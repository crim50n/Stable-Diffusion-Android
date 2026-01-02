package dev.minios.pdaiv1.presentation.screen.onboarding

import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.domain.entity.DarkThemeToken
import dev.minios.pdaiv1.domain.preference.PreferenceManager
import dev.minios.pdaiv1.domain.usecase.splash.SplashNavigationUseCase
import dev.minios.pdaiv1.presentation.core.CoreViewModelTest
import dev.minios.pdaiv1.presentation.model.LaunchSource
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import dev.minios.pdaiv1.presentation.stub.stubDispatchersProvider
import dev.minios.pdaiv1.presentation.stub.stubSchedulersProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class OnBoardingViewModelTest : CoreViewModelTest<OnBoardingViewModel>() {

    private val stubMainRouter = mockk<MainRouter>()
    private val stubSplashNavigationUseCase = mockk<SplashNavigationUseCase>()
    private val stubPreferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val stubBuildInfoProvider = mockk<BuildInfoProvider>()

    override fun initializeViewModel() = OnBoardingViewModel(
        launchSource = LaunchSource.SPLASH,
        dispatchersProvider = stubDispatchersProvider,
        mainRouter = stubMainRouter,
        splashNavigationUseCase = stubSplashNavigationUseCase,
        preferenceManager = stubPreferenceManager,
        schedulersProvider = stubSchedulersProvider,
        buildInfoProvider = stubBuildInfoProvider,
    )

    @Before
    override fun initialize() {
        super.initialize()

        every {
            stubPreferenceManager.designDarkThemeToken
        } returns DarkThemeToken.FRAPPE.toString()

        every {
            stubBuildInfoProvider.toString()
        } returns "1.0.0"
    }

    @Test
    fun `initialized, expected dark theme token from preferences in state`() {
        runTest {
            val expected = DarkThemeToken.FRAPPE
            val actual = viewModel.state.value.darkThemeToken
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `initialized, expected app version from build info in state`() {
        runTest {
            val expected = "1.0.0"
            val actual = viewModel.state.value.appVersion
            Assert.assertEquals(expected, actual)
        }
    }

    @Test
    fun `given Navigate intent with SETTINGS source, expected navigateBack called`() {
        val viewModelWithSettings = OnBoardingViewModel(
            launchSource = LaunchSource.SETTINGS,
            dispatchersProvider = stubDispatchersProvider,
            mainRouter = stubMainRouter,
            splashNavigationUseCase = stubSplashNavigationUseCase,
            preferenceManager = stubPreferenceManager,
            schedulersProvider = stubSchedulersProvider,
            buildInfoProvider = stubBuildInfoProvider,
        )

        every {
            stubMainRouter.navigateBack()
        } returns Unit

        viewModelWithSettings.processIntent(OnBoardingIntent.Navigate)

        verify {
            stubPreferenceManager.onBoardingComplete = true
            stubMainRouter.navigateBack()
        }
    }

    @Test
    fun `given Navigate intent with SPLASH source, expected onBoardingComplete set to true`() {
        every {
            stubSplashNavigationUseCase()
        } returns Single.just(mockk(relaxed = true))

        viewModel.processIntent(OnBoardingIntent.Navigate)

        verify {
            stubPreferenceManager.onBoardingComplete = true
        }
    }
}
