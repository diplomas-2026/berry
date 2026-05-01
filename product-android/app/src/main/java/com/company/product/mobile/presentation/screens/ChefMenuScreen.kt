package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.MenuItemDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ChefMenuScreen(
    repo: AppRepository,
    onAdd: (String) -> Unit,
    onEdit: (String, Long) -> Unit,
    onDishClick: (Long) -> Unit = {}
) {
    var menuDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var menuItems by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var mealFilter by remember { mutableStateOf("ALL") }
    var sortMode by remember { mutableStateOf("slot") }
    var deleteTarget by remember { mutableStateOf<MenuItemDto?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadDates() {
        menuDates = repo.chefMenuDates()
        if (selectedDate.isBlank()) {
            selectedDate = menuDates.firstOrNull() ?: LocalDate.now().toString()
        }
    }

    suspend fun loadMenu(date: String) {
        menuItems = repo.chefMenu(date)
    }

    suspend fun reload(date: String = selectedDate) {
        loading = true
        error = null
        try {
            loadDates()
            loadMenu(date)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        reload()
        initialized = true
    }

    LaunchedEffect(selectedDate) {
        if (initialized) {
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
        (menuDates + selectedDate).distinct().sortedDescending()
    }

    val visibleItems = remember(menuItems, query, mealFilter, sortMode) {
        menuItems
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
                    else -> compareBy<MenuItemDto> { menuSlotSortIndex(it.mealSlot) }.thenBy { it.dish.name.lowercase() }
                }
            )
    }

    ScreenContainer("Меню повара") {
        SectionCard(
            title = "Меню по датам",
            subtitle = "Выберите день и откройте нужный экран"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        visibleDates.forEach { date ->
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
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
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

                Button(
                    onClick = { onAdd(selectedDate) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить блюдо")
                }
            }
        }

        if (loading) {
            CenterLoading()
        }

        if (error != null) {
            ErrorCard(error!!)
        }

        if (!loading && error == null) {
            if (visibleItems.isEmpty()) {
                EmptyStateCard(
                    title = "На эту дату меню не назначено",
                    subtitle = if (query.isNotBlank() || mealFilter != "ALL") {
                        "Ничего не найдено по текущему поиску и фильтрам"
                    } else {
                        "Нажмите «Добавить блюдо», чтобы создать меню на выбранный день"
                    }
                )
            } else {
                visibleItems.forEach { item ->
                    SectionCard(
                        title = "",
                        subtitle = null,
                        onClick = { onDishClick(item.dish.id) }
                    ) {
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

                        Button(
                            onClick = { onEdit(item.date, item.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Изменить")
                        }
                        TextButton(
                            onClick = { deleteTarget = item },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Удалить из меню")
                        }
                    }
                }
            }
        }

        if (deleteTarget != null) {
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("Удалить блюдо из меню?") },
                text = {
                    Text(
                        text = "Это действие уберёт блюдо ${mealSlotLabel(deleteTarget!!.mealSlot)} на ${formatDate(deleteTarget!!.date)}."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val target = deleteTarget ?: return@TextButton
                            deleteTarget = null
                            scope.launch {
                                try {
                                    repo.chefDeleteMenuItem(target.id)
                                    reload()
                                } catch (e: Exception) {
                                    error = e.message
                                }
                            }
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deleteTarget = null }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

private fun menuSlotSortIndex(slot: String): Int = when (slot) {
    "BREAKFAST" -> 0
    "LUNCH" -> 1
    "DINNER" -> 2
    else -> Int.MAX_VALUE
}
