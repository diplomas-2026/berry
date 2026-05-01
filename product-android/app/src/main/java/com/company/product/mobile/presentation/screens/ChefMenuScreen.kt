package com.company.product.mobile.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.DishDto
import com.company.product.mobile.data.remote.MenuItemDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

private val mealSlots = listOf("BREAKFAST", "LUNCH", "DINNER")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefMenuScreen(repo: AppRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var menuDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var dishes by remember { mutableStateOf<List<DishDto>>(emptyList()) }
    var menuItems by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<MenuItemDto?>(null) }
    var selectedSlot by remember { mutableStateOf("BREAKFAST") }
    var selectedDishId by remember { mutableStateOf<Long?>(null) }
    var dishMenuExpanded by remember { mutableStateOf(false) }
    var deleteCandidate by remember { mutableStateOf<MenuItemDto?>(null) }
    var initialSelectionSet by remember { mutableStateOf(false) }

    fun resetForm(date: String = selectedDate) {
        editingItem = null
        selectedDate = date
        selectedSlot = "BREAKFAST"
        selectedDishId = dishes.firstOrNull()?.id
    }

    suspend fun loadDatesAndDishes(): String {
        dishes = repo.chefDishes()
        if (selectedDishId == null) {
            selectedDishId = dishes.firstOrNull()?.id
        }
        menuDates = repo.chefMenuDates()
        if (!initialSelectionSet) {
            selectedDate = menuDates.firstOrNull() ?: LocalDate.now().toString()
            initialSelectionSet = true
        }
        return selectedDate
    }

    suspend fun loadMenu(date: String) {
        menuItems = repo.chefMenu(date)
    }

    suspend fun reloadAll(date: String = selectedDate) {
        loading = true
        error = null
        try {
            val targetDate = loadDatesAndDishes().ifBlank { date }
            loadMenu(targetDate)
            if (selectedDishId == null) {
                selectedDishId = dishes.firstOrNull()?.id
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        reloadAll()
    }

    LaunchedEffect(selectedDate) {
        if (initialSelectionSet && !loading) {
            try {
                loading = true
                error = null
                loadMenu(selectedDate)
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    val visibleDates = remember(menuDates, selectedDate) {
        (menuDates + selectedDate).distinct()
            .sortedDescending()
    }
    val selectedMenuItems = remember(menuItems) {
        menuItems.sortedBy { mealSlots.indexOf(it.mealSlot).let { index -> if (index == -1) Int.MAX_VALUE else index } }
    }
    val editingMode = editingItem != null

    if (deleteCandidate != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Удалить блюдо из меню?") },
            text = {
                Text(
                    "Блюдо ${mealSlotLabel(deleteCandidate!!.mealSlot)} будет удалено из меню на ${formatDate(deleteCandidate!!.date)}."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val candidate = deleteCandidate ?: return@Button
                        deleteCandidate = null
                        scope.launch {
                            saving = true
                            try {
                                repo.chefDeleteMenuItem(candidate.id)
                                message = "Блюдо удалено"
                                if (editingItem?.id == candidate.id) {
                                    editingItem = null
                                }
                                reloadAll(selectedDate)
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                saving = false
                            }
                        }
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    ScreenContainer("Меню повара") {
        SectionCard(
            title = "Выберите дату",
            subtitle = "Карточки сверху показывают все дни, где уже есть меню"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        visibleDates.forEach { date ->
                            FilterChip(
                                selected = selectedDate == date,
                                onClick = {
                                    selectedDate = date
                                    editingItem = null
                                },
                                label = { Text(formatDate(date)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
                TextButton(
                    onClick = {
                        val current = runCatching { LocalDate.parse(selectedDate) }.getOrElse { LocalDate.now() }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDate = LocalDate.of(year, month + 1, day).toString()
                                editingItem = null
                            },
                            current.year,
                            current.monthValue - 1,
                            current.dayOfMonth
                        ).show()
                    }
                ) {
                    Text("Выбрать новый день")
                }
            }
        }

        SectionCard(
            title = if (editingMode) "Редактировать блюдо" else "Добавить блюдо",
            subtitle = formatDate(selectedDate)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Дата меню: ${formatDate(selectedDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Приём пищи", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        mealSlots.forEach { slot ->
                            FilterChip(
                                selected = selectedSlot == slot,
                                onClick = { selectedSlot = slot },
                                label = { Text(mealSlotLabel(slot)) }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = dishMenuExpanded,
                    onExpandedChange = { dishMenuExpanded = !dishMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = dishes.firstOrNull { it.id == selectedDishId }?.name.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Блюдо") },
                        placeholder = { Text("Выберите блюдо") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dishMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dishMenuExpanded,
                        onDismissRequest = { dishMenuExpanded = false }
                    ) {
                        dishes.forEach { dish ->
                            DropdownMenuItem(
                                text = { Text(dish.name) },
                                onClick = {
                                    selectedDishId = dish.id
                                    dishMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val dishId = selectedDishId
                        if (dishId == null) {
                            error = "Сначала выберите блюдо"
                            return@Button
                        }
                        scope.launch {
                            saving = true
                            try {
                                if (editingItem == null) {
                                    repo.chefAddMenuItem(selectedDate, selectedSlot, dishId)
                                    message = "Блюдо добавлено в меню"
                                } else {
                                    repo.chefUpdateMenuItem(editingItem!!.id, selectedDate, selectedSlot, dishId)
                                    message = "Меню обновлено"
                                }
                                reloadAll(selectedDate)
                                resetForm(selectedDate)
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving && dishes.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (editingMode) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (editingMode) "Сохранить изменения" else "Добавить в меню")
                }

                if (editingMode) {
                    TextButton(
                        onClick = {
                            editingItem = null
                            selectedSlot = "BREAKFAST"
                            selectedDishId = dishes.firstOrNull()?.id
                        }
                    ) {
                        Text("Отменить редактирование")
                    }
                }
            }
        }

        if (loading) {
            CenterLoading()
        }

        if (error != null) {
            ErrorCard(error!!)
        }

        if (selectedMenuItems.isEmpty() && !loading && error == null) {
            EmptyStateCard(
                title = "На эту дату меню не назначено",
                subtitle = "Добавьте блюда выше или выберите другой день"
            )
        } else {
            selectedMenuItems.forEach { item ->
                SectionCard(title = "", subtitle = null) {
                    MediaFrame(
                        url = item.dish.photoUrl,
                        placeholderTitle = "Фото блюда отсутствует",
                        placeholderSubtitle = "Можно добавить позже",
                        aspectRatio = 16f / 9f
                    )
                    Text(
                        text = "${mealSlotLabel(item.mealSlot)} • ${item.dish.name}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = formatDate(item.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!item.dish.description.isNullOrBlank()) {
                        Text(item.dish.description)
                    }
                    val nutrition = listOfNotNull(
                        item.dish.proteinsPer100g?.let { v -> "Б: ${v}г" },
                        item.dish.fatsPer100g?.let { v -> "Ж: ${v}г" },
                        item.dish.carbsPer100g?.let { v -> "У: ${v}г" },
                        item.dish.caloriesPer100g?.let { v -> "ккал: $v" }
                    ).joinToString(" • ")
                    if (nutrition.isNotBlank()) {
                        Text(nutrition, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(
                            onClick = {
                                editingItem = item
                                selectedDate = item.date
                                selectedSlot = item.mealSlot
                                selectedDishId = item.dish.id
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Изменить")
                        }
                        TextButton(
                            onClick = { deleteCandidate = item }
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Удалить")
                        }
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            EmptyStateCard(title = "Статус", subtitle = message)
        }
    }
}
