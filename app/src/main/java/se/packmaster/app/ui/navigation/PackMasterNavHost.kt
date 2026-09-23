package se.packmaster.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import se.packmaster.app.R
import se.packmaster.app.ui.bags.BagsScreen
import se.packmaster.app.ui.catalog.CatalogScreen
import se.packmaster.app.ui.packing.PackingScreen
import se.packmaster.app.ui.profiles.ProfilesScreen

private enum class TopLevel(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    BAGS("bags", R.string.nav_bags, Icons.Filled.Luggage),
    CATALOG("catalog", R.string.nav_catalog, Icons.Filled.Inventory2),
    PROFILES("profiles", R.string.nav_profiles, Icons.Filled.Groups),
}

private const val BAG_ROUTE = "bag/{bagId}"
const val BAG_ID_ARG = "bagId"

private fun NavHostController.openBag(bagId: Long) = navigate("bag/$bagId")

@Composable
fun PackMasterNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevel.entries.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevel.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
    ) { outerPadding ->
        // Skärmarna har egna toppfält; här tar vi bara hänsyn till bottennavigeringen.
        NavHost(
            navController = navController,
            startDestination = TopLevel.BAGS.route,
            modifier = Modifier.padding(bottom = if (showBottomBar) outerPadding.calculateBottomPadding() else 0.dp),
        ) {
            composable(TopLevel.BAGS.route) {
                BagsScreen(onOpenBag = navController::openBag)
            }
            composable(TopLevel.CATALOG.route) {
                CatalogScreen()
            }
            composable(TopLevel.PROFILES.route) {
                ProfilesScreen()
            }
            composable(
                route = BAG_ROUTE,
                arguments = listOf(navArgument(BAG_ID_ARG) { type = NavType.LongType }),
            ) {
                PackingScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
