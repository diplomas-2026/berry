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
                composable("student_menu") {
                    StudentMenuScreen(
                        appState.repository(),
                        onDishClick = { dishId -> nav.navigate("dish_details/$dishId") }
                    )
                }
                composable(
                    route = "dish_details/{dishId}",
                    arguments = listOf(navArgument("dishId") { type = NavType.LongType })
                ) { backStackEntry ->
                    DishDetailsScreen(
                        repo = appState.repository(),
                        dishId = backStackEntry.arguments?.getLong("dishId") ?: 0L
                    )
                }
                composable("student_vouchers") { StudentVouchersScreen(appState.repository()) }
                composable("student_qr") { StudentQrScreen(appState.repository()) }
                composable("curator_students") {
                    CuratorStudentsScreen(
                        repo = appState.repository(),
                        onIssueVoucher = { studentId -> nav.navigate("curator_issue/$studentId") }
                    )
                }
                composable(
                    route = "curator_issue/{studentId}",
                    arguments = listOf(navArgument("studentId") { type = NavType.LongType })
                ) { backStackEntry ->
                    CuratorIssueVoucherScreen(
                        repo = appState.repository(),
                        studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L,
                        onDone = { nav.popBackStack() }
                    )
                }
                composable("chef_menu") {
                    ChefMenuScreen(
                        repo = appState.repository(),
                        onAdd = { date -> nav.navigate("chef_menu_add/$date") },
                        onEdit = { date, itemId -> nav.navigate("chef_menu_edit/$date/$itemId") },
                        onDishClick = { dishId -> nav.navigate("dish_details/$dishId") }
                    )
                }
                composable(
                    route = "chef_menu_add/{date}",
                    arguments = listOf(navArgument("date") { type = NavType.StringType })
                ) { backStackEntry ->
                    val date = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()
                    ChefMenuAddScreen(
                        repo = appState.repository(),
                        initialDate = date,
                        onCreateDish = { nav.navigate("chef_dish_create/$date") },
                        onDone = {
                            nav.navigate("chef_menu") {
                                popUpTo("chef_menu") { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    route = "chef_dish_create/{returnDate}",
                    arguments = listOf(navArgument("returnDate") { type = NavType.StringType })
                ) { backStackEntry ->
                    val returnDate = backStackEntry.arguments?.getString("returnDate") ?: LocalDate.now().toString()
                    ChefDishCreateScreen(
                        repo = appState.repository(),
                        onDone = {
                            nav.popBackStack("chef_menu_add/$returnDate", inclusive = true)
                            nav.navigate("chef_menu_add/$returnDate")
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
                composable("chef_scan") {
                    ChefScanScreen(
                        repo = appState.repository(),
                        onDishClick = { dishId -> nav.navigate("dish_details/$dishId") }
                    )
                }
                composable("admin_users") {
                    AdminUsersScreen(
                        repo = appState.repository(),
                        onCreateUser = { nav.navigate("admin_user_create") },
                        onEditUser = { userId -> nav.navigate("admin_user_edit/$userId") }
                    )
                }
                composable("admin_user_create") {
                    AdminUserEditorScreen(
                        repo = appState.repository(),
                        onDone = { nav.popBackStack() }
                    )
                }
                composable(
                    route = "admin_user_edit/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.LongType })
                ) { backStackEntry ->
                    AdminUserEditorScreen(
                        repo = appState.repository(),
                        userId = backStackEntry.arguments?.getLong("userId"),
                        onDone = { nav.popBackStack() }
                    )
                }
                composable("admin_groups") {
                    AdminGroupsScreen(
                        repo = appState.repository(),
                        onCreateGroup = { nav.navigate("admin_group_create") },
                        onEditGroup = { groupId -> nav.navigate("admin_group_edit/$groupId") }
                    )
                }
                composable("admin_group_create") {
                    AdminGroupEditorScreen(
                        repo = appState.repository(),
                        onDone = { nav.popBackStack() }
                    )
                }
                composable(
                    route = "admin_group_edit/{groupId}",
                    arguments = listOf(navArgument("groupId") { type = NavType.LongType })
                ) { backStackEntry ->
                    AdminGroupEditorScreen(
                        repo = appState.repository(),
                        groupId = backStackEntry.arguments?.getLong("groupId"),
                        onDone = { nav.popBackStack() }
                    )
                }
                composable("profile") { ProfileScreen(appState) }
            }
        }
    }
}
