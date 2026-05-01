package com.company.product.mobile.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.company.product.mobile.data.remote.DishDto
import com.company.product.mobile.data.remote.MenuItemDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun ChefMenuEditScreen(
    repo: AppRepository,
    date: String,
    itemId: Long,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var dishes by remember { mutableStateOf<List<DishDto>>(emptyList()) }
    var menuDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var menuItems by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(date) }
    var selectedSlot by remember { mutableStateOf("BREAKFAST") }
    var selectedDishId by remember { mutableStateOf<Long?>(null) }
    var initialized by remember { mutableStateOf(false) }

    suspend fun reload() {
        dishes = repo.chefDishes()
        menuDates = repo.chefMenuDates()
        menuItems = repo.chefMenu(date)
        if (!initialized) {
            val item = menuItems.firstOrNull { it.id == itemId }
                ?: throw IllegalStateException("Блюдо для редактирования не найдено")
            selectedDate = item.date
            selectedSlot = item.mealSlot
            selectedDishId = item.dish.id
            initialized = true
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

    val targetItem = menuItems.firstOrNull { it.id == itemId }

    ChefMenuItemEditor(
        title = "Изменить блюдо",
        subtitle = targetItem?.dish?.name ?: "Редактирование выбранного пункта меню",
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
        submitLabel = "Сохранить изменения",
        onSubmit = {
            val dishId = selectedDishId
            if (dishId == null) {
                error = "Сначала выберите блюдо"
            } else {
                scope.launch {
                    saving = true
                    try {
                        repo.chefUpdateMenuItem(itemId, selectedDate, selectedSlot, dishId)
                        message = "Меню обновлено"
                        onDone()
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        saving = false
                    }
                }
            }
        },
        onDelete = {
            scope.launch {
                saving = true
                try {
                    repo.chefDeleteMenuItem(itemId)
                    message = "Блюдо удалено"
                    onDone()
                } catch (e: Exception) {
                    error = e.message
                } finally {
                    saving = false
                }
            }
        }
    )
}
