package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.DishDto
import com.company.product.mobile.data.repository.AppRepository

@Composable
fun DishDetailsScreen(repo: AppRepository, dishId: Long) {
    var dish by remember { mutableStateOf<DishDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dishId) {
        try {
            dish = repo.dish(dishId)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    ScreenContainer("Блюдо") {
        if (loading) {
            CenterLoading()
        }
        if (error != null) {
            ErrorCard(error!!)
        }
        dish?.let { current ->
            SectionCard(title = "", subtitle = null) {
                MediaFrame(
                    url = current.photoUrl,
                    placeholderTitle = "Фото блюда отсутствует",
                    placeholderSubtitle = "Повар может добавить его позже",
                    aspectRatio = 16f / 9f
                )

                Text(
                    text = current.name,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
                if (!current.description.isNullOrBlank()) {
                    Text(current.description)
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NutritionLine("Белки", current.proteinsPer100g?.let { "${it} г" })
                    NutritionLine("Жиры", current.fatsPer100g?.let { "${it} г" })
                    NutritionLine("Углеводы", current.carbsPer100g?.let { "${it} г" })
                    NutritionLine("Калорийность", current.caloriesPer100g?.let { "${it} ккал" })
                }
            }
        }
        if (!loading && dish == null && error == null) {
            EmptyStateCard(
                title = "Блюдо не найдено",
                subtitle = "Попробуйте открыть его ещё раз"
            )
        }
    }
}

@Composable
private fun NutritionLine(label: String, value: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label)
        Text(value ?: "Нет данных")
    }
}
