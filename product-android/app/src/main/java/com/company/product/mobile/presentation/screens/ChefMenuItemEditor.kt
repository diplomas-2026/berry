package com.company.product.mobile.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.DishDto
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefMenuItemEditor(
    title: String,
    subtitle: String,
    selectedDate: String,
    dateOptions: List<String>,
    onDateSelected: (String) -> Unit,
    selectedMealSlot: String,
    onMealSlotSelected: (String) -> Unit,
    dishes: List<DishDto>,
    selectedDishId: Long?,
    onDishSelected: (Long) -> Unit,
    loading: Boolean,
    saving: Boolean,
    error: String?,
    message: String?,
    submitLabel: String,
    onSubmit: () -> Unit,
    onCreateDish: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onCancelEdit: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var dishSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedDish = dishes.firstOrNull { it.id == selectedDishId }
    val visibleDishes = remember(dishes) { dishes.sortedBy { it.name.lowercase() } }

    ScreenContainer(title) {
        SectionCard(title = subtitle, subtitle = null) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (dateOptions + selectedDate).distinct().sortedDescending().forEach { date ->
                            FilterChip(
                                selected = selectedDate == date,
                                onClick = { onDateSelected(date) },
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
                                onDateSelected(LocalDate.of(year, month + 1, day).toString())
                            },
                            current.year,
                            current.monthValue - 1,
                            current.dayOfMonth
                        ).show()
                    }
                ) {
                    Text("Выбрать дату меню блюда")
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Приём пищи", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("BREAKFAST", "LUNCH", "DINNER").forEach { slot ->
                            FilterChip(
                                selected = selectedMealSlot == slot,
                                onClick = { onMealSlotSelected(slot) },
                                label = { Text(mealSlotLabel(slot)) }
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dishSheetOpen = true },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedDish != null) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (selectedDish != null) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Блюдо", style = MaterialTheme.typography.labelLarge)
                                Text(
                                    text = selectedDish?.name ?: "Выберите блюдо из списка",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (selectedDish != null) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (onCreateDish != null) {
                    TextButton(
                        onClick = onCreateDish,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать новое блюдо")
                    }
                }

                Button(
                    onClick = onSubmit,
                    enabled = !saving && dishes.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (submitLabel.contains("измен", ignoreCase = true)) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(submitLabel)
                }

                if (onCancelEdit != null) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Отменить редактирование")
                    }
                }
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Удалить блюдо")
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

        if (message != null && message.isNotBlank()) {
            EmptyStateCard(title = "Статус", subtitle = message)
        }
    }

    if (dishSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { dishSheetOpen = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Выберите блюдо", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Нажмите на блюдо, чтобы выбрать его",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { dishSheetOpen = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Закрыть")
                    }
                }

                if (visibleDishes.isEmpty()) {
                    Text(
                        text = "Список блюд пока пуст",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(visibleDishes) { dish ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDishSelected(dish.id)
                                        dishSheetOpen = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(dish.name, style = MaterialTheme.typography.titleMedium)
                                    if (!dish.description.isNullOrBlank()) {
                                        Text(
                                            text = dish.description,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
