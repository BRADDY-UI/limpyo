package com.limpyo.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.limpyo.app.R
import com.limpyo.app.core.designsystem.component.BottomNavItem
import com.limpyo.app.core.designsystem.component.LimpyoBottomNav
import com.limpyo.app.core.designsystem.icon.LimpyoIcons
import com.limpyo.app.core.designsystem.theme.LimpyoTheme
import com.limpyo.app.core.designsystem.theme.ThemeMode
import com.limpyo.app.core.designsystem.theme.tweenShort
import com.limpyo.app.feature.home.HomeScreen
import com.limpyo.app.feature.report.ReportScreen
import com.limpyo.app.feature.scan.ScanScreen
import com.limpyo.app.feature.schedule.ScheduleScreen
import com.limpyo.app.feature.you.YouScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// Type-safe routes: each screen is a small Kotlin object instead of a string like "home".
// IDs match docs/SCREENS.md.
@Serializable data object HomeRoute      // R-HOME
@Serializable data object ScheduleRoute  // R-SCH
@Serializable data object ReportRoute    // R-REP
@Serializable data object ScanRoute      // R-SCN
@Serializable data object YouRoute       // R-YOU

private data class Tab(val route: Any, val routeClass: KClass<*>, val item: BottomNavItem)

/** App frame: the current tab on top, the bottom nav underneath. */
@Composable
fun LimpyoRoot(themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    val nav = rememberNavController()
    val tabs = listOf(
        Tab(HomeRoute, HomeRoute::class, BottomNavItem(stringResource(R.string.tab_home), LimpyoIcons.Home, LimpyoIcons.HomeFilled)),
        Tab(ScheduleRoute, ScheduleRoute::class, BottomNavItem(stringResource(R.string.tab_schedule), LimpyoIcons.Schedule, LimpyoIcons.ScheduleFilled)),
        Tab(ReportRoute, ReportRoute::class, BottomNavItem(stringResource(R.string.tab_report), LimpyoIcons.Report, LimpyoIcons.ReportFilled)),
        Tab(ScanRoute, ScanRoute::class, BottomNavItem(stringResource(R.string.tab_scan), LimpyoIcons.Scan, LimpyoIcons.ScanFilled)),
        Tab(YouRoute, YouRoute::class, BottomNavItem(stringResource(R.string.tab_you), LimpyoIcons.You, LimpyoIcons.YouFilled)),
    )
    val backStack by nav.currentBackStackEntryAsState()
    val selected = tabs.indexOfFirst { tab -> backStack?.destination?.hasRoute(tab.routeClass) == true }
        .coerceAtLeast(0)

    Column(Modifier.fillMaxSize().background(LimpyoTheme.colors.ground)) {
        NavHost(
            navController = nav,
            startDestination = HomeRoute,
            modifier = Modifier.weight(1f),
            // Tab switch = 160 ms crossfade, no slide (docs/DESIGN.md section 7).
            enterTransition = { fadeIn(tweenShort()) },
            exitTransition = { fadeOut(tweenShort()) },
            popEnterTransition = { fadeIn(tweenShort()) },
            popExitTransition = { fadeOut(tweenShort()) },
        ) {
            composable<HomeRoute> { HomeScreen() }
            composable<ScheduleRoute> { ScheduleScreen() }
            composable<ReportRoute> { ReportScreen() }
            composable<ScanRoute> { ScanScreen() }
            composable<YouRoute> { YouScreen(themeMode, onThemeModeChange) }
        }
        LimpyoBottomNav(
            items = tabs.map { it.item },
            selectedIndex = selected,
            onSelect = { index ->
                nav.navigate(tabs[index].route) {
                    // Standard bottom-nav behavior: one copy of each tab, state kept when you come back.
                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        )
    }
}
