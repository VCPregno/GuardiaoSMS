package com.tcc.guardiaosms.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tcc.guardiaosms.R
import com.tcc.guardiaosms.di.AppContainer
import com.tcc.guardiaosms.di.ViewModelFactory
import com.tcc.guardiaosms.ui.history.HistoryScreen
import com.tcc.guardiaosms.ui.history.HistoryViewModel
import com.tcc.guardiaosms.ui.home.HomeScreen
import com.tcc.guardiaosms.ui.home.HomeViewModel
import com.tcc.guardiaosms.ui.settings.SettingsScreen
import com.tcc.guardiaosms.ui.settings.SettingsViewModel
import com.tcc.guardiaosms.ui.test.TestScreen
import com.tcc.guardiaosms.ui.test.TestViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

private sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object History : Destination("history")
    data object Test : Destination("test")
    data object Settings : Destination("settings")
}

@Composable
fun GuardiaoSmsNavGraph(
    container: AppContainer,
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit
) {
    val navController = rememberNavController()
    val factory = ViewModelFactory(container)

    Scaffold(
        bottomBar = { GuardiaoSmsBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Home.route) {
                val viewModel: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    hasSmsPermission = hasSmsPermission,
                    onRequestPermission = onRequestPermission,
                    viewModel = viewModel
                )
            }
            composable(Destination.History.route) {
                val viewModel: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(viewModel = viewModel)
            }
            composable(Destination.Test.route) {
                val viewModel: TestViewModel = viewModel(factory = factory)
                TestScreen(viewModel = viewModel)
            }
            composable(Destination.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun GuardiaoSmsBottomBar(navController: androidx.navigation.NavController) {
    val items = listOf(
        Triple(Destination.Home.route, Icons.Filled.Home, R.string.nav_home),
        Triple(Destination.History.route, Icons.Filled.History, R.string.nav_history),
        Triple(Destination.Test.route, Icons.Filled.Search, R.string.nav_test),
        Triple(Destination.Settings.route, Icons.Filled.Settings, R.string.nav_settings)
    )
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        items.forEach { (route, icon, labelRes) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = stringResource(labelRes)) },
                label = { Text(stringResource(labelRes)) }
            )
        }
    }
}
