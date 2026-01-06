package dev.minios.pdaiv1.presentation.screen.drawer

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import dev.minios.pdaiv1.core.common.appbuild.BuildInfoProvider
import dev.minios.pdaiv1.core.model.asString
import com.shifthackz.android.core.mvi.MviComponent
import dev.minios.pdaiv1.presentation.R
import dev.minios.pdaiv1.presentation.model.NavItem
import dev.minios.pdaiv1.presentation.navigation.NavigationRoute
import dev.minios.pdaiv1.presentation.utils.Constants
import dev.minios.pdaiv1.presentation.widget.item.NavigationItemIcon
import dev.minios.pdaiv1.presentation.widget.work.BackgroundWorkWidget
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun DrawerScreen(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    backStackEntry: NavBackStackEntry? = null,
    homeRouteEntry: NavigationRoute? = null,
    navItems: List<NavItem> = emptyList(),
    onRootNavigate: (NavigationRoute) -> Unit = {},
    onHomeNavigate: (NavigationRoute) -> Unit = {},
    content: @Composable () -> Unit,
) {
    if (navItems.isEmpty()) {
        return content()
    }
    val currentRootRoute = backStackEntry?.destination
    val currentRoute: Any? = if (currentRootRoute?.hasRoute(NavigationRoute.Home::class) == true) {
        homeRouteEntry ?: NavigationRoute.HomeNavigation.TxtToImg
    } else {
        currentRootRoute
    }

    MviComponent(
        viewModel = koinViewModel<DrawerViewModel>(),
    ) { _, intentHandler ->
        ModalNavigationDrawer(
            gesturesEnabled = drawerState.isOpen, // Only allow gesture to close, not to open
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.background,
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val itemModifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    Row(
                        modifier = itemModifier,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_pdai_logo),
                            contentDescription = "PDAI Android Branding",
                        )
                        Column(
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "PDAI",
                                    style = MaterialTheme.typography.headlineMedium,
                                )
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = "Android",
                                )
                            }
                            Text(
                                text = "${koinInject<BuildInfoProvider>()}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    navItems.forEach { item ->
                        val selected = (currentRoute as? NavigationRoute)?.let { navRoute ->
                            item.navRoute == navRoute
                        } ?: (currentRoute as? NavDestination)?.let { destination ->
                            destination.route?.contains("${item.navRoute}") == true
                        } ?: false
                        NavigationDrawerItem(
                            modifier = itemModifier.padding(bottom = 4.dp),
                            selected = selected,
                            label = {
                                Text(
                                    text = item.name.asString(),
                                    color = LocalContentColor.current,
                                )
                            },
                            icon = { NavigationItemIcon(item.icon) },
                            onClick = {
                                if (!selected) {
                                    if (Constants.homeRoutes.any { homeRoute -> item.navRoute == homeRoute }) {
                                        if (currentRootRoute?.hasRoute(NavigationRoute.Home::class) == false) {
                                            onRootNavigate(NavigationRoute.Home)
                                        }
                                        onHomeNavigate(item.navRoute)
                                    } else {
                                        onRootNavigate(item.navRoute)
                                    }
                                }
                                intentHandler(DrawerIntent.Close)
                            },
                        )
                    }
                }
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
                
                // Global floating BackgroundWorkWidget - inside drawer content so drawer is on top
                BackgroundWorkWidget(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 88.dp), // Above bottom navigation with extra spacing for swipe
                )
            }
        }
    }
}
