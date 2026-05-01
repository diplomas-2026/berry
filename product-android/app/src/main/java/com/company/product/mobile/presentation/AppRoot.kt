package com.company.product.mobile.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.company.product.mobile.presentation.screens.*
import java.time.LocalDate

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val appState = remember { AppState(context) }
    val nav = rememberNavController()
    var ready by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appState.restoreSession()
        ready = true
    }

    BerryTheme {
        if (!ready) {
            CenterLoading()
        } else {
            val startDestination = if (appState.session != null) "home" else "login"
            NavHost(navController = nav, startDestination = startDestination) {
                composable("login") {
                    LoginScreen(
                        onSuccess = {
                            nav.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        appState = appState
                    )
                }
                composable("home") {
                    HomeScreen(
                        session = appState.session,
                        onOpen = { nav.navigate(it) },
                        onLogout = {
                            appState.logout()
                            nav.navigate("login") {
                                popUpTo(0)
                            }
                        }
                    )
                }
                composable("student_menu") { StudentMenuScreen(appState.repository()) }
                composable("student_vouchers") { StudentVouchersScreen(appState.repository()) }
                composable("student_qr") { StudentQrScreen(appState.repository()) }
                composable("curator_students") { CuratorStudentsScreen(appState.repository()) }
                composable("curator_issue") { CuratorIssueVoucherScreen(appState.repository()) }
                composable("chef_menu") {
                    ChefMenuScreen(
                        repo = appState.repository(),
                        onAdd = { date -> nav.navigate("chef_menu_add/$date") },
                        onEdit = { date, itemId -> nav.navigate("chef_menu_edit/$date/$itemId") }
                    )
                }
                composable(
                    route = "chef_menu_add/{date}",
                    arguments = listOf(navArgument("date") { type = NavType.StringType })
                ) { backStackEntry ->
                    ChefMenuAddScreen(
                        repo = appState.repository(),
                        initialDate = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString(),
                        onDone = {
                            nav.navigate("chef_menu") {
                                popUpTo("chef_menu") { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    route = "chef_menu_edit/{date}/{itemId}",
                    arguments = listOf(
                        navArgument("date") { type = NavType.StringType },
                        navArgument("itemId") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    ChefMenuEditScreen(
                        repo = appState.repository(),
                        date = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString(),
                        itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L,
                        onDone = {
                            nav.navigate("chef_menu") {
                                popUpTo("chef_menu") { inclusive = true }
                            }
                        }
                    )
                }
                composable("chef_scan") { ChefScanScreen(appState.repository()) }
                composable("admin_users") { AdminUsersScreen(appState.repository()) }
                composable("profile") { ProfileScreen(appState) }
            }
        }
    }
}
