package com.company.product.mobile.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.company.product.mobile.data.remote.MenuItemDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun ChefMenuScreen(repo: AppRepository) {
    var menu by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                menu = repo.chefMenu()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    ScreenContainer("Текущее меню") {
        if (loading) CenterLoading()
        if (error != null) Text("Ошибка: $error")
        menu.forEach { Text("${it.mealSlot}: ${it.dish.name}") }
    }
}
