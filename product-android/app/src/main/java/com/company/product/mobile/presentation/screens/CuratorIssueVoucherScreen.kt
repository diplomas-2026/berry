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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
    var students by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var student by remember { mutableStateOf<UserDto?>(null) }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var breakfast by remember { mutableStateOf(true) }
    var lunch by remember { mutableStateOf(true) }
    var dinner by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(studentId) {
        try {
            students = repo.curatorStudents()
            student = students.firstOrNull { it.id == studentId }
            if (student == null) {
                error = "Студент не найден"
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

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
                subtitle = "Талон будет выдан выбранному студенту"
            ) {
                MediaFrame(
                    url = current.avatarUrl,
                    placeholderTitle = "Фото отсутствует",
                    placeholderSubtitle = "Можно добавить в профиле",
                    aspectRatio = 1f
                )
                Text(current.fullName)
                Text(current.email, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("Приёмы пищи", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
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

            if (result.isNotBlank()) {
                SectionCard(title = "Статус") {
                    Text(result)
                    TextButton(onClick = onDone) {
                        Text("Назад к списку")
                    }
                }
            }
        }
    }
}
