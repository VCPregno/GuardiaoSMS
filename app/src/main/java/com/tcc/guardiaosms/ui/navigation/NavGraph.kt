package com.tcc.guardiaosms.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tcc.guardiaosms.R
import com.tcc.guardiaosms.di.AppContainer
import com.tcc.guardiaosms.di.ViewModelFactory
import com.tcc.guardiaosms.ui.detail.AlertDetailScreen
import com.tcc.guardiaosms.ui.detail.AlertDetailViewModel
import com.tcc.guardiaosms.ui.history.HistoryScreen
import com.tcc.guardiaosms.ui.history.HistoryViewModel
import com.tcc.guardiaosms.ui.home.HomeScreen
import com.tcc.guardiaosms.ui.home.HomeViewModel
import com.tcc.guardiaosms.ui.settings.SettingsScreen
import com.tcc.guardiaosms.ui.settings.SettingsViewModel
import com.tcc.guardiaosms.ui.test.TestScreen
import com.tcc.guardiaosms.ui.test.TestViewModel

private sealed class Destination(val route: String) {
    data object Home : Destination("home")
    data object History : Destination("history")
    data object Test : Destination("test")
    data object Settings : Destination("settings")
    data object Detail : Destination("detail/{recordId}") {
        const val ARG_RECORD_ID = "recordId"
        fun routeFor(recordId: Long) = "detail/$recordId"
    }
}

private val topLevelRoutes = setOf(
    Destination.Home.route,
    Destination.History.route,
    Destination.Test.route,
    Destination.Settings.route
)

/**
 * @param openRecordId id de uma mensagem a abrir no detalhe (vindo do toque na
 *   notificação); depois de navegar, [onOpenRecordConsumed] é chamado.
 */
@Composable
fun GuardiaoSmsNavGraph(
    container: AppContainer,
    hasSmsPermission: Boolean,
    onRequestPermission: () -> Unit,
    openRecordId: Long?,
    onOpenRecordConsumed: () -> Unit
) {
    val navController = rememberNavController()
    val factory = ViewModelFactory(container)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Sem launchSingleTop de propósito: abrir o detalhe de outra mensagem estando
    // num detalhe reaproveitaria a entrada (e o ViewModel) da mensagem anterior.
    val openDetail: (Long) -> Unit = { id -> navController.navigate(Destination.Detail.routeFor(id)) }

    LaunchedEffect(openRecordId) {
        if (openRecordId != null) {
            openDetail(openRecordId)
            onOpenRecordConsumed()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        // A tela de detalhe é focada (tem botão Voltar); a barra inferior só aparece nas 4 abas.
        bottomBar = { if (currentRoute == null || currentRoute in topLevelRoutes) GuardiaoSmsBottomBar(navController, currentRoute) }
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
                    onOpenRecord = openDetail,
                    viewModel = viewModel
                )
            }
            composable(Destination.History.route) {
                val viewModel: HistoryViewModel = viewModel(factory = factory)
                HistoryScreen(viewModel = viewModel, onOpenRecord = openDetail)
            }
            composable(Destination.Test.route) {
                val viewModel: TestViewModel = viewModel(factory = factory)
                TestScreen(viewModel = viewModel)
            }
            composable(Destination.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(factory = factory)
                SettingsScreen(viewModel = viewModel)
            }
            composable(
                route = Destination.Detail.route,
                arguments = listOf(navArgument(Destination.Detail.ARG_RECORD_ID) { type = NavType.LongType })
            ) { entry ->
                val recordId = entry.arguments?.getLong(Destination.Detail.ARG_RECORD_ID) ?: -1L
                val viewModel: AlertDetailViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { AlertDetailViewModel(container.database.smsRecordDao(), recordId) }
                    }
                )
                AlertDetailScreen(
                    viewModel = viewModel,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate(Destination.Home.route)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GuardiaoSmsBottomBar(navController: NavController, currentRoute: String?) {
    val items = listOf(
        Triple(Destination.Home.route, Icons.Filled.Home, R.string.nav_home),
        Triple(Destination.History.route, Icons.Filled.History, R.string.nav_history),
        Triple(Destination.Test.route, Icons.Filled.Search, R.string.nav_test),
        Triple(Destination.Settings.route, Icons.Filled.Settings, R.string.nav_settings)
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
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
                // O rótulo já descreve o item; o ícone é decorativo para leitores de tela.
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(stringResource(labelRes), style = MaterialTheme.typography.labelMedium) },
                alwaysShowLabel = true
            )
        }
    }
}
