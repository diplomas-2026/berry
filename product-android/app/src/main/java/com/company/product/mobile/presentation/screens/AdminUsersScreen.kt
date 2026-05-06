package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.company.product.mobile.data.remote.UserDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun AdminUsersScreen(
    repo: AppRepository,
    onCreateUser: () -> Unit,
    onEditUser: (Long) -> Unit
) {
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("ALL") }
    var sortMode by remember { mutableStateOf("name") }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                users = repo.adminUsers()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    val visibleUsers = remember(users, query, roleFilter, sortMode) {
        users
            .filter { user ->
                val matchesQuery = query.isBlank() ||
                    user.fullName.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
                val matchesRole = roleFilter == "ALL" || user.role == roleFilter
                matchesQuery && matchesRole
            }
            .sortedWith(
                when (sortMode) {
                    "email" -> compareBy { it.email.lowercase() }
                    else -> compareBy<UserDto> { it.fullName.lowercase() }.thenBy { it.id }
                }
            )
    }

    ScreenContainer("Пользователи") {
        SectionCard(
            title = "Создание пользователя",
            subtitle = "Форма вынесена на отдельный экран"
        ) {
            Button(onClick = onCreateUser, modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать пользователя")
            }
        }

        if (error != null) {
            ErrorCard(error!!)
        }
        if (!message.isNullOrBlank()) {
            EmptyStateCard(title = "Статус", subtitle = message)
        }

        SectionCard(title = "Список пользователей", subtitle = "Можно открыть отдельный экран редактирования") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск") },
                    placeholder = { Text("Имя или email") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Роль", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            listOf("ALL" to "Все", "STUDENT" to "Студенты", "CURATOR" to "Кураторы", "CHEF" to "Повара", "ADMIN" to "Админы").forEach { (value, label) ->
                                FilterChip(
                                    selected = roleFilter == value,
                                    onClick = { roleFilter = value },
                                    label = { Text(label) },
                                    modifier = Modifier.padding(horizontal = 3.dp)
                                )
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Сортировка", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("name" to "По имени", "email" to "По email").forEach { (value, label) ->
                            FilterChip(selected = sortMode == value, onClick = { sortMode = value }, label = { Text(label) })
                        }
                    }
                }
                Button(onClick = { reload() }, modifier = Modifier.fillMaxWidth()) { Text("Обновить список") }
            }

            if (loading) {
                CenterLoading()
            }

            if (visibleUsers.isEmpty() && !loading) {
                EmptyStateCard(
                    title = "Пользователи не найдены",
                    subtitle = if (query.isNotBlank() || roleFilter != "ALL") {
                        "Ничего не найдено по текущему поиску и фильтрам"
                    } else {
                        "Сейчас список пользователей пуст"
                    }
                )
            } else {
                visibleUsers.forEach { user ->
                    SectionCard(
                        title = user.fullName,
                        subtitle = "${user.email} • ${roleLabel(user.role)}"
                    ) {
                        StatusPill(if (user.active) "Активен" else "Отключён")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onEditUser(user.id) }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Редактировать")
                            }
                            TextButton(onClick = {
                                scope.launch {
                                    try {
                                        repo.adminUpdateUser(
                                            id = user.id,
                                            firstName = user.firstName,
                                            lastName = user.lastName,
                                            middleName = user.middleName,
                                            active = !user.active,
                                            role = user.role
                                        )
                                        reload()
                                    } catch (e: Exception) {
                                        error = e.message
                                    }
                                }
                            }) {
                                Text(if (user.active) "Деактивировать" else "Активировать")
                            }
                        }
                    }
                }
            }
        }
    }
}
