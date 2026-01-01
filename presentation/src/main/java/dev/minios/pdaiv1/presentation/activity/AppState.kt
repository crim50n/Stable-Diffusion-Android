package dev.minios.pdaiv1.presentation.activity

import androidx.compose.runtime.Immutable
import dev.minios.pdaiv1.presentation.model.NavItem
import dev.minios.pdaiv1.presentation.navigation.graph.mainDrawerNavItems
import com.shifthackz.android.core.mvi.MviState

@Immutable
data class AppState(
    val drawerItems: List<NavItem> = mainDrawerNavItems(),
    val isShowSplash: Boolean = true,
) : MviState
