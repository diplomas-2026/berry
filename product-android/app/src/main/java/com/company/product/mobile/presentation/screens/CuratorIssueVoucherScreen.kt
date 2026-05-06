package com.company.product.mobile.presentation.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.UserDto
import com.company.product.mobile.data.remote.VoucherDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun CuratorIssueVoucherScreen(
    repo: AppRepository,
    studentId: Long,
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var student by remember { mutableStateOf<UserDto?>(null) }
    var vouchers by remember { mutableStateOf<List<VoucherDto>>(emptyList()) }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var breakfast by remember { mutableStateOf(true) }
    var lunch by remember { mutableStateOf(true) }
    var dinner by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var voucherQuery by remember { mutableStateOf("") }
    var voucherFilter by remember { mutableStateOf("ALL") }
    var voucherSort by remember { mutableStateOf("date") }
    var deleteTarget by remember { mutableStateOf<VoucherDto?>(null) }

    fun reloadStudentAndVouchers() {
        scope.launch {
            loading = true
            error = null
            try {
                val students = repo.curatorStudents()
                student = students.firstOrNull { it.id == studentId }
                vouchers = repo.curatorStudentVouchers(studentId)
                if (student == null) {
                    error = "Студент не найден"
                }
            } catch (e: Exception) {
                error = e.message
                student = null
                vouchers = emptyList()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(studentId) {
        reloadStudentAndVouchers()
    }

    val visibleVouchers = remember(vouchers, voucherQuery, voucherFilter, voucherSort) {
        vouchers
            .filter { voucher ->
                val matchesQuery = voucherQuery.isBlank() ||
                    voucher.studentName.contains(voucherQuery, ignoreCase = true) ||
                    mealSlotLabel(voucher.mealSlot).contains(voucherQuery, ignoreCase = true) ||
                    voucherStatusLabel(voucher.status).contains(voucherQuery, ignoreCase = true) ||
                    formatDate(voucher.issueDate).contains(voucherQuery, ignoreCase = true)
                val matchesFilter = voucherFilter == "ALL" || voucher.status.uppercase() == voucherFilter
                matchesQuery && matchesFilter
            }
            .sortedWith(
                when (voucherSort) {
                    "status" -> compareBy<VoucherDto> { voucherStatusSortIndex(it.status) }.thenByDescending { it.issueDate }
                    "slot" -> compareBy<VoucherDto> { mealSlotSortIndex(it.mealSlot) }.thenByDescending { it.issueDate }
                    else -> compareByDescending<VoucherDto> { it.issueDate }.thenBy { mealSlotSortIndex(it.mealSlot) }
                }
            )
    }
    val groupedVouchers = remember(visibleVouchers) { groupVouchersByDate(visibleVouchers) }

    ScreenContainer("Выдача талона") {
        if (loading) {
            CenterLoading()
        }

        if (error != null) {
            ErrorCard(error!!, title = "Ошибка выдачи")
        }

        student?.let { current ->
            SectionCard(
                title = "Студент",
                subtitle = "Талон выдаётся выбранному студенту"
            ) {
                MediaFrame(
                    url = current.avatarUrl,
                    placeholderTitle = "Фото отсутствует",
                    placeholderSubtitle = "Можно добавить в профиле",
                    aspectRatio = 1f
                )
                Text(current.fullName)
                Text(current.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            SectionCard(
                title = "Параметры выдачи",
                subtitle = "Дата выбирается через календарь"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = {
                            val currentDate = runCatching { LocalDate.parse(date) }.getOrElse { LocalDate.now() }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    date = LocalDate.of(year, month + 1, day).toString()
                                },
                                currentDate.year,
                                currentDate.monthValue - 1,
                                currentDate.dayOfMonth
                            ).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выбрать дату")
                    }

                    Text("Дата талона: ${formatDate(date)}")

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Приёмы пищи", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BREAKFAST" to "Завтрак", "LUNCH" to "Обед", "DINNER" to "Ужин").forEach { (slot, label) ->
                                val checked = when (slot) {
                                    "BREAKFAST" -> breakfast
                                    "LUNCH" -> lunch
                                    else -> dinner
                                }
                                FilterChip(
                                    selected = checked,
                                    onClick = {
                                        when (slot) {
                                            "BREAKFAST" -> breakfast = !breakfast
                                            "LUNCH" -> lunch = !lunch
                                            else -> dinner = !dinner
                                        }
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                saving = true
                                result = ""
                                try {
                                    val slots = buildList {
                                        if (breakfast) add("BREAKFAST")
                                        if (lunch) add("LUNCH")
                                        if (dinner) add("DINNER")
                                    }
                                    repo.issueVouchers(studentId, date, slots)
                                    result = "Талоны выданы"
                                    vouchers = repo.curatorStudentVouchers(studentId)
                                } catch (e: Exception) {
                                    result = "Ошибка: ${e.message}"
                                } finally {
                                    saving = false
                                }
                            }
                        },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (saving) "Выдаём..." else "Выдать талон")
                    }
                }
            }

            SectionCard(
                title = "Талоны студента",
                subtitle = "Поиск, фильтрация и сортировка по текущим талонам"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = voucherQuery,
                        onValueChange = { voucherQuery = it },
                        label = { Text("Поиск") },
                        placeholder = { Text("Дата, приём пищи или статус") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Фильтр", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("ALL" to "Все", "ISSUED" to "Выдан", "REDEEMED" to "Погашен", "CANCELLED" to "Отменён").forEach { (value, label) ->
                                FilterChip(
                                    selected = voucherFilter == value,
                                    onClick = { voucherFilter = value },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Сортировка", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("date" to "По дате", "slot" to "По приёму", "status" to "По статусу").forEach { (value, label) ->
                                FilterChip(
                                    selected = voucherSort == value,
                                    onClick = { voucherSort = value },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            if (visibleVouchers.isEmpty()) {
                EmptyStateCard(
                    title = "Талоны не найдены",
                    subtitle = if (voucherQuery.isNotBlank() || voucherFilter != "ALL") {
                        "Ничего не найдено по текущему поиску и фильтрам"
                    } else {
                        "У этого студента пока нет талонов"
                    }
                )
            } else {
                groupedVouchers.forEach { group ->
                    VoucherDayCard(
                        group = group,
                        onDeleteVoucher = { voucher -> deleteTarget = voucher }
                    )
                }
            }

            if (result.isNotBlank()) {
                SectionCard(title = "Статус") {
                    Text(result)
                    TextButton(onClick = onDone) {
                        Text("Назад к списку")
                    }
                }
            }
        }

        if (deleteTarget != null) {
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("Удалить талон?") },
                text = {
                    Text(
                        "Будущий талон ${mealSlotLabel(deleteTarget!!.mealSlot)} на ${formatDate(deleteTarget!!.issueDate)} будет удалён."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val target = deleteTarget ?: return@TextButton
                            deleteTarget = null
                            scope.launch {
                                try {
                                    repo.deleteCuratorVoucher(target.id)
                                    vouchers = repo.curatorStudentVouchers(studentId)
                                    result = "Талон удалён"
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

private fun mealSlotSortIndex(slot: String): Int = when (slot) {
    "BREAKFAST" -> 0
    "LUNCH" -> 1
    "DINNER" -> 2
    else -> Int.MAX_VALUE
}

private fun voucherStatusSortIndex(status: String): Int = when (status) {
    "ISSUED" -> 0
    "REDEEMED" -> 1
    "CANCELLED" -> 2
    else -> Int.MAX_VALUE
}
