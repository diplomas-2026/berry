package com.company.product.mobile.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
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
        if (error != null) ErrorCard(error!!)
        menu.forEach {
            SectionCard(
                title = "",
                subtitle = null
            ) {
                MediaFrame(
                    url = it.dish.photoUrl,
                    placeholderTitle = "Фото блюда отсутствует",
                    placeholderSubtitle = "Повар может загрузить фото позже",
                    aspectRatio = 16f / 9f
                )
                Text(
                    text = "${mealSlotLabel(it.mealSlot)}: ${it.dish.name}",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = formatDate(it.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!it.dish.description.isNullOrBlank()) Text(it.dish.description)
            }
        }
    }
}
