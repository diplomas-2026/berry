package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
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
import com.company.product.mobile.data.remote.VoucherDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun StudentVouchersScreen(repo: AppRepository) {
    var vouchers by remember { mutableStateOf<List<VoucherDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var sortMode by remember { mutableStateOf("date") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                vouchers = repo.studentVouchers()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    val visibleVouchers = remember(vouchers, query, statusFilter, sortMode) {
        vouchers
            .filter { voucher ->
                val matchesQuery = query.isBlank() ||
                    voucher.studentName.contains(query, ignoreCase = true) ||
                    mealSlotLabel(voucher.mealSlot).contains(query, ignoreCase = true) ||
                    voucherStatusLabel(voucher.status).contains(query, ignoreCase = true) ||
                    formatDate(voucher.issueDate).contains(query, ignoreCase = true)
                val matchesFilter = statusFilter == "ALL" || voucher.status == statusFilter
                matchesQuery && matchesFilter
            }
            .sortedWith(
                when (sortMode) {
                    "status" -> compareBy<VoucherDto> { voucherStatusSortIndex(it.status) }.thenByDescending { it.issueDate }
                    else -> compareByDescending<VoucherDto> { it.issueDate }.thenBy { it.id }
                }
            )
    }

    ScreenContainer("Мои талоны") {
        SectionCard(title = "Поиск и фильтры", subtitle = "Можно быстро найти нужный талон") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск") },
                    placeholder = { Text("Дата, приём пищи или статус") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Статус", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL" to "Все", "ISSUED" to "Выданы", "REDEEMED" to "Погашены", "EXPIRED" to "Просрочены", "CANCELLED" to "Отменены").forEach { (value, label) ->
                            FilterChip(selected = statusFilter == value, onClick = { statusFilter = value }, label = { Text(label) })
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Сортировка", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("date" to "По дате", "status" to "По статусу").forEach { (value, label) ->
                            FilterChip(selected = sortMode == value, onClick = { sortMode = value }, label = { Text(label) })
                        }
                    }
                }
            }
        }

        if (loading) CenterLoading()
        if (error != null) ErrorCard(error!!)
        if (visibleVouchers.isEmpty()) {
            EmptyStateCard(
                title = "Талоны не найдены",
                subtitle = if (query.isNotBlank() || statusFilter != "ALL") {
                    "Ничего не найдено по текущему поиску и фильтрам"
                } else {
                    "На текущий момент у вас нет доступных талонов"
                }
            )
        } else {
            visibleVouchers.forEach {
                SectionCard(title = "Талон #${it.id}", subtitle = formatDate(it.issueDate)) {
                    Text("Приём пищи: ${mealSlotLabel(it.mealSlot)}")
                    StatusPill(voucherStatusLabel(it.status))
                }
            }
        }
    }
}

private fun voucherStatusSortIndex(status: String): Int = when (status) {
    "ISSUED" -> 0
    "REDEEMED" -> 1
    "EXPIRED" -> 2
    "CANCELLED" -> 3
    else -> Int.MAX_VALUE
}
