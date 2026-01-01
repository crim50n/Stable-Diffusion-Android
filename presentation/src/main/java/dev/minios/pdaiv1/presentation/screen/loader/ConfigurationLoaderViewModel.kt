package dev.minios.pdaiv1.presentation.screen.loader

import dev.minios.pdaiv1.core.common.log.errorLog
import dev.minios.pdaiv1.core.common.schedulers.DispatchersProvider
import dev.minios.pdaiv1.core.common.schedulers.SchedulersProvider
import dev.minios.pdaiv1.core.common.schedulers.subscribeOnMainThread
import dev.minios.pdaiv1.core.model.asUiText
import dev.minios.pdaiv1.core.viewmodel.MviRxViewModel
import dev.minios.pdaiv1.domain.usecase.caching.DataPreLoaderUseCase
import dev.minios.pdaiv1.presentation.navigation.router.main.MainRouter
import com.shifthackz.android.core.mvi.EmptyEffect
import com.shifthackz.android.core.mvi.EmptyIntent
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit
import dev.minios.pdaiv1.core.localization.R as LocalizationR

class ConfigurationLoaderViewModel(
    dataPreLoaderUseCase: DataPreLoaderUseCase,
    dispatchersProvider: DispatchersProvider,
    schedulersProvider: SchedulersProvider,
    mainRouter: MainRouter,
) : MviRxViewModel<ConfigurationLoaderState, EmptyIntent, EmptyEffect>() {

    override val initialState = ConfigurationLoaderState.StatusNotification(
        LocalizationR.string.splash_status_initializing.asUiText()
    )

    override val effectDispatcher = dispatchersProvider.immediate

    init {
        !dataPreLoaderUseCase()
            .timeout(15L, TimeUnit.SECONDS)
            .doOnSubscribe {
                updateState {
                    ConfigurationLoaderState.StatusNotification(
                        LocalizationR.string.splash_status_fetching.asUiText()
                    )
                }
            }
            .subscribeOnMainThread(schedulersProvider)
            .subscribeBy(
                onError = { t ->
                    updateState {
                        ConfigurationLoaderState.StatusNotification("Failed loading data".asUiText())
                    }
                    mainRouter.navigateToHomeScreen()
                    errorLog(t)
                },
                onComplete = {
                    updateState {
                        ConfigurationLoaderState.StatusNotification(
                            LocalizationR.string.splash_status_launching.asUiText()
                        )
                    }
                    mainRouter.navigateToHomeScreen()
                },
            )
    }
}
