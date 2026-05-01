package com.company.product.mobile.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.company.product.mobile.data.remote.MenuItemDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun StudentMenuScreen(repo: AppRepository) {
    var items by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                items = repo.studentMenu()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    ScreenContainer("Моё меню") {
        if (loading) CenterLoading()
        if (error != null) Text("Ошибка: $error")
        items.forEach {
            Text("${it.mealSlot}: ${it.dish.name}")
            if (!it.dish.description.isNullOrBlank()) Text(it.dish.description)
        }
    }
}
