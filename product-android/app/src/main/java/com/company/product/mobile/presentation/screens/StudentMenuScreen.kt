package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.MenuItemDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun StudentMenuScreen(repo: AppRepository) {
    var items by remember { mutableStateOf<List<MenuItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
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

    ScreenContainer("Моё меню") {
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
            }
        }

        if (!loading && error == null) {
            if (selectedItems.isEmpty()) {
                EmptyStateCard(
                    title = "На выбранную дату меню нет",
                    subtitle = selectedDate?.let { formatDate(it) } ?: "Дата не выбрана"
                )
            } else {
                selectedItems.forEach {
                    SectionCard(
                        title = "",
                        subtitle = null
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
