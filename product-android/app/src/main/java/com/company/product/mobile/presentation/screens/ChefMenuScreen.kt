package com.company.product.mobile.presentation.screens

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.MenuItemDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

private val chefMealOrder = listOf("BREAKFAST", "LUNCH", "DINNER")

@Composable
fun ChefMenuScreen(
    repo: AppRepository,
    onAdd: (String) -> Unit,
    onEdit: (String, Long) -> Unit
) {
    val context = LocalContext.current

    var menuDates by remember { mutableStateOf<List<String>>(emptyList()) }
    var menuItems by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

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

                TextButton(
                    onClick = {
                        val current = runCatching { LocalDate.parse(selectedDate) }.getOrElse { LocalDate.now() }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                selectedDate = LocalDate.of(year, month + 1, day).toString()
                            },
                            current.year,
                            current.monthValue - 1,
                            current.dayOfMonth
                        ).show()
                    }
                ) {
                    Text("Выбрать другой день")
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
            if (menuItems.isEmpty()) {
                EmptyStateCard(
                    title = "На эту дату меню не назначено",
                    subtitle = "Нажмите «Добавить блюдо», чтобы создать меню на выбранный день"
                )
            } else {
                menuItems.sortedBy { chefMealOrder.indexOf(it.mealSlot).let { index -> if (index == -1) Int.MAX_VALUE else index } }.forEach { item ->
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
                        Button(
                            onClick = { onEdit(item.date, item.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Изменить")
                        }
                    }
                }
            }
        }
    }
}
