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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onDelete: (() -> Unit)? = null,
    onCancelEdit: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var dishMenuExpanded by remember { mutableStateOf(false) }
    val selectedDish = dishes.firstOrNull { it.id == selectedDishId }

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

                ExposedDropdownMenuBox(
                    expanded = dishMenuExpanded,
                    onExpandedChange = { dishMenuExpanded = !dishMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedDish?.name.orEmpty(),
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
                                    onDishSelected(dish.id)
                                    dishMenuExpanded = false
                                }
                            )
                        }
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
}
