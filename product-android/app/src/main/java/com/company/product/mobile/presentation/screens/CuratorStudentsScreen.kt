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
import com.company.product.mobile.data.remote.UserDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun CuratorStudentsScreen(repo: AppRepository) {
    var students by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf("name") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                students = repo.curatorStudents()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    val visibleStudents = remember(students, query, sortMode) {
        students
            .filter { student ->
                query.isBlank() ||
                    student.fullName.contains(query, ignoreCase = true) ||
                    student.email.contains(query, ignoreCase = true) ||
                    student.role.contains(query, ignoreCase = true)
            }
            .sortedWith(
                when (sortMode) {
                    "email" -> compareBy { it.email.lowercase() }
                    else -> compareBy<UserDto> { it.fullName.lowercase() }.thenBy { it.id }
                }
            )
    }

    ScreenContainer("Студенты группы") {
        SectionCard(title = "Поиск и сортировка", subtitle = "Можно быстро найти студента") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск") },
                    placeholder = { Text("Имя, email или роль") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("name" to "По имени", "email" to "По email").forEach { (value, label) ->
                        FilterChip(selected = sortMode == value, onClick = { sortMode = value }, label = { Text(label) })
                    }
                }
            }
        }

        if (loading) CenterLoading()
        if (error != null) ErrorCard(error!!)
        if (visibleStudents.isEmpty()) {
            EmptyStateCard(
                title = "Студенты не найдены",
                subtitle = if (query.isNotBlank()) "Ничего не найдено по текущему поиску" else "В этой группе пока нет студентов"
            )
        } else {
            visibleStudents.forEach {
                SectionCard(title = it.fullName, subtitle = it.email) {
                    Text("ID: ${it.id}")
                }
            }
        }
    }
}
