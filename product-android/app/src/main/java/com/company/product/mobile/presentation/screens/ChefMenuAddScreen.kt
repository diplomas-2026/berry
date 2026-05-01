package com.company.product.mobile.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.company.product.mobile.data.remote.DishDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun ChefMenuAddScreen(
    repo: AppRepository,
    initialDate: String,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var dishes by remember { mutableStateOf<List<DishDto>>(emptyList()) }
    var menuDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedSlot by remember { mutableStateOf("BREAKFAST") }
    var selectedDishId by remember { mutableStateOf<Long?>(null) }

    suspend fun reload() {
        dishes = repo.chefDishes()
        menuDates = repo.chefMenuDates()
        if (selectedDishId == null) {
            selectedDishId = dishes.firstOrNull()?.id
        }
    }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            reload()
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    ChefMenuItemEditor(
        title = "Добавить блюдо",
        subtitle = "Выберите дату меню этого блюда",
        selectedDate = selectedDate,
        dateOptions = menuDates,
        onDateSelected = { selectedDate = it },
        selectedMealSlot = selectedSlot,
        onMealSlotSelected = { selectedSlot = it },
        dishes = dishes,
        selectedDishId = selectedDishId,
        onDishSelected = { selectedDishId = it },
        loading = loading,
        saving = saving,
        error = error,
        message = message,
        submitLabel = "Добавить блюдо",
        onSubmit = {
            val dishId = selectedDishId
            if (dishId == null) {
                error = "Сначала выберите блюдо"
            } else {
                scope.launch {
                    saving = true
                    try {
                        repo.chefAddMenuItem(selectedDate, selectedSlot, dishId)
                        message = "Блюдо добавлено в меню"
                        onDone()
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        saving = false
                    }
                }
            }
        }
    )
}
