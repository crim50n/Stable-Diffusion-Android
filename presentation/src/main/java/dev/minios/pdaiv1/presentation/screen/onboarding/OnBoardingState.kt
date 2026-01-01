package dev.minios.pdaiv1.presentation.screen.onboarding

import dev.minios.pdaiv1.domain.entity.DarkThemeToken
import com.shifthackz.android.core.mvi.MviState

data class OnBoardingState(
    val darkThemeToken: DarkThemeToken = DarkThemeToken.FRAPPE,
    val appVersion: String = "",
) : MviState
