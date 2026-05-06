package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import com.company.product.mobile.data.remote.GroupDto
import com.company.product.mobile.data.remote.UserDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun CuratorStudentsScreen(
    repo: AppRepository,
    onIssueVoucher: (Long) -> Unit
) {
    var groups by remember { mutableStateOf<List<GroupDto>>(emptyList()) }
    var students by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var loadingGroups by remember { mutableStateOf(true) }
    var loadingStudents by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf("name") }
    val scope = rememberCoroutineScope()

    fun loadGroups() {
        scope.launch {
            loadingGroups = true
            error = null
            try {
                groups = repo.curatorGroups()
                if (selectedGroupId == null) {
                    selectedGroupId = groups.firstOrNull()?.id
                }
            } catch (e: Exception) {
                error = e.message
            } finally {
                loadingGroups = false
            }
        }
    }

    fun loadStudents(groupId: Long) {
        scope.launch {
            loadingStudents = true
            error = null
            try {
                students = repo.curatorGroupStudents(groupId)
            } catch (e: Exception) {
                error = e.message
                students = emptyList()
            } finally {
                loadingStudents = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadGroups()
    }

    LaunchedEffect(selectedGroupId) {
        selectedGroupId?.let { loadStudents(it) }
    }

    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }
    val visibleStudents = remember(students, query, sortMode) {
        students
            .filter { student ->
                query.isBlank() ||
                    student.fullName.contains(query, ignoreCase = true) ||
                    student.email.contains(query, ignoreCase = true)
            }
            .sortedWith(
                when (sortMode) {
                    "email" -> compareBy { it.email.lowercase() }
                    else -> compareBy<UserDto> { it.fullName.lowercase() }
                }
            )
    }

    ScreenContainer("Студенты группы") {
        SectionCard(
            title = "Группы",
            subtitle = "Выберите одну из групп куратора"
        ) {
            if (loadingGroups) {
                CenterLoading()
            } else if (groups.isEmpty()) {
                EmptyStateCard(
                    title = "Группы не найдены",
                    subtitle = "За вами пока не закреплено ни одной группы"
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        groups.forEach { group ->
                            FilterChip(
                                selected = selectedGroupId == group.id,
                                onClick = {
                                    selectedGroupId = group.id
                                    query = ""
                                },
                                label = { Text(group.name) }
                            )
                        }
                    }
                }
            }
        }

        if (selectedGroup != null) {
            SectionCard(
                title = "Студенты",
                subtitle = "Группа: ${selectedGroup.name}"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Поиск") },
                        placeholder = { Text("Имя или email студента") },
                        leadingIcon = { androidx.compose.material3.Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("name" to "По имени", "email" to "По email").forEach { (value, label) ->
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

        if (loadingStudents) {
            CenterLoading()
        }

        if (error != null) {
            ErrorCard(error!!)
        }

        if (!loadingStudents && error == null) {
            if (selectedGroup == null && !loadingGroups) {
                EmptyStateCard(
                    title = "Выберите группу",
                    subtitle = "После выбора здесь появится список студентов"
                )
            } else if (visibleStudents.isEmpty()) {
                EmptyStateCard(
                    title = "Студенты не найдены",
                    subtitle = if (query.isNotBlank()) {
                        "Ничего не найдено по текущему поиску"
                    } else {
                        "В выбранной группе пока нет студентов"
                    }
                )
            } else {
                visibleStudents.forEach { student ->
                    SectionCard(
                        title = student.fullName,
                        subtitle = student.email,
                        onClick = {
                            onIssueVoucher(student.id)
                        }
                    ) {
                        MediaFrame(
                            url = student.avatarUrl,
                            placeholderTitle = "Фото отсутствует",
                            placeholderSubtitle = "Можно добавить в профиле",
                            aspectRatio = 1f
                        )
                        Button(
                            onClick = { onIssueVoucher(student.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Выдать талон")
                        }
                    }
                }
            }
        }
    }
}
