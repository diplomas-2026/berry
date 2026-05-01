package com.company.product.mobile.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.company.product.mobile.presentation.screens.*

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
                composable("chef_menu") { ChefMenuScreen(appState.repository()) }
                composable("chef_scan") { ChefScanScreen(appState.repository()) }
                composable("admin_users") { AdminUsersScreen(appState.repository()) }
                composable("profile") { ProfileScreen(appState) }
            }
        }
    }
}
