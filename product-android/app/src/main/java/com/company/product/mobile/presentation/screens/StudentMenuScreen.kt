package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.MenuItemDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun StudentMenuScreen(repo: AppRepository, onDishClick: (Long) -> Unit = {}) {
    var items by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var mealFilter by remember { mutableStateOf("ALL") }
    var sortMode by remember { mutableStateOf("slot") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                items = repo.studentMenu()
                selectedDate = items.firstOrNull()?.date
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    val groupedByDate = remember(items) { items.groupBy { it.date } }
    val availableDates = remember(items) { items.map { it.date }.distinct() }
    val selectedItems = selectedDate?.let { groupedByDate[it].orEmpty() }.orEmpty()
    val visibleItems = remember(selectedItems, query, mealFilter, sortMode) {
        selectedItems
            .filter { item ->
                val matchesQuery = query.isBlank() ||
                    item.dish.name.contains(query, ignoreCase = true) ||
                    item.dish.description.orEmpty().contains(query, ignoreCase = true) ||
                    mealSlotLabel(item.mealSlot).contains(query, ignoreCase = true)
                val matchesFilter = mealFilter == "ALL" || item.mealSlot == mealFilter
                matchesQuery && matchesFilter
            }
            .sortedWith(
                when (sortMode) {
                    "name" -> compareBy { it.dish.name.lowercase() }
                    else -> compareBy<MenuItemDto> { mealSlotSortIndex(it.mealSlot) }.thenBy { it.dish.name.lowercase() }
                }
            )
    }

    ScreenContainer("Меню") {
        if (loading) CenterLoading()
        if (error != null) ErrorCard(error!!)

        if (!loading && error == null && availableDates.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Выберите дату", style = MaterialTheme.typography.titleMedium)
                Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableDates.forEach { date ->
                            FilterChip(
                                selected = selectedDate == date,
                                onClick = { selectedDate = date },
                                label = { Text(formatDate(date)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск") },
                    placeholder = { Text("Название блюда или приём пищи") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Фильтр", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL" to "Все", "BREAKFAST" to "Завтрак", "LUNCH" to "Обед", "DINNER" to "Ужин").forEach { (value, label) ->
                        FilterChip(
                            selected = mealFilter == value,
                            onClick = { mealFilter = value },
                            label = { Text(label) }
                        )
                    }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Сортировка", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("slot" to "По приёму", "name" to "По названию").forEach { (value, label) ->
                            FilterChip(
                                selected = sortMode == value,
                                onClick = { sortMode = value },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        }

        if (!loading && error == null) {
            if (visibleItems.isEmpty()) {
                EmptyStateCard(
                    title = "На выбранную дату меню нет",
                    subtitle = if (query.isNotBlank() || mealFilter != "ALL") {
                        "Ничего не найдено по текущему поиску и фильтрам"
                    } else {
                        selectedDate?.let { formatDate(it) } ?: "Дата не выбрана"
                    }
                )
            } else {
                visibleItems.forEach {
                    SectionCard(
                        title = "",
                        subtitle = null,
                        onClick = { onDishClick(it.dish.id) }
                    ) {
                        MediaFrame(
                            url = it.dish.photoUrl,
                            placeholderTitle = "Фото блюда отсутствует",
                            placeholderSubtitle = "",
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
                        if (!it.dish.description.isNullOrBlank()) {
                            Text(it.dish.description)
                        }
                        val nutrition = listOfNotNull(
                            it.dish.proteinsPer100g?.let { v -> "Б: ${v}г" },
                            it.dish.fatsPer100g?.let { v -> "Ж: ${v}г" },
                            it.dish.carbsPer100g?.let { v -> "У: ${v}г" },
                            it.dish.caloriesPer100g?.let { v -> "ккал: $v" }
                        ).joinToString(" • ")
                        if (nutrition.isNotBlank()) {
                            Text(nutrition)
                        }
                    }
                }
            }
        }
    }
}

private fun mealSlotSortIndex(slot: String): Int = when (slot) {
    "BREAKFAST" -> 0
    "LUNCH" -> 1
    "DINNER" -> 2
    else -> Int.MAX_VALUE
}
