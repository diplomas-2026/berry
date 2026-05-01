package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
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
fun AdminUsersScreen(repo: AppRepository) {
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var editingId by remember { mutableStateOf<Long?>(null) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STUDENT") }
    var active by remember { mutableStateOf(true) }
    var query by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("ALL") }
    var sortMode by remember { mutableStateOf("name") }

    val roleOptions = remember {
        listOf(
            "Студент" to "STUDENT",
            "Куратор" to "CURATOR",
            "Повар" to "CHEF",
            "Администратор" to "ADMIN"
        )
    }
    val scope = rememberCoroutineScope()

    fun clearForm() {
        editingId = null
        email = ""
        password = ""
        firstName = ""
        lastName = ""
        middleName = ""
        role = "STUDENT"
        active = true
    }

    fun fillForm(user: UserDto) {
        editingId = user.id
        email = user.email
        password = ""
        firstName = user.firstName
        lastName = user.lastName
        middleName = user.middleName
        role = user.role
        active = user.active
    }

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
        SectionCard(title = "Список пользователей", subtitle = "Можно редактировать ФИО, роль и активность") {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL" to "Все", "STUDENT" to "Студенты", "CURATOR" to "Кураторы", "CHEF" to "Повара", "ADMIN" to "Админы").forEach { (value, label) ->
                            FilterChip(selected = roleFilter == value, onClick = { roleFilter = value }, label = { Text(label) })
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
            if (loading) CenterLoading()
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
                            TextButton(onClick = { fillForm(user) }) {
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

        SectionCard(
            title = if (editingId == null) "Создание пользователя" else "Редактирование пользователя",
            subtitle = "Имя, фамилия и отчество обязательны"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    readOnly = editingId != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (editingId == null) {
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth())
                }
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Фамилия") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = middleName, onValueChange = { middleName = it }, label = { Text("Отчество") }, modifier = Modifier.fillMaxWidth())
                Text("Роль")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    roleOptions.forEach { (label, value) ->
                        FilterChip(
                            selected = role == value,
                            onClick = { role = value },
                            label = { Text(label) }
                        )
                    }
                }
                TextButton(onClick = { active = !active }) {
                    Text(if (active) "Активен" else "Отключён")
                }
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                if (editingId == null) {
                                    repo.adminCreateUser(
                                        email.trim(),
                                        password,
                                        firstName.trim(),
                                        lastName.trim(),
                                        middleName.trim(),
                                        role
                                    )
                                    message = "Пользователь создан"
                                } else {
                                    repo.adminUpdateUser(
                                        id = editingId!!,
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        middleName = middleName.trim(),
                                        active = active,
                                        role = role
                                    )
                                    message = "Пользователь обновлён"
                                }
                                clearForm()
                                reload()
                            } catch (e: Exception) {
                                error = e.message
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = if (editingId == null) Icons.Default.PersonAdd else Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (editingId == null) "Создать пользователя" else "Сохранить изменения")
                }
                if (editingId != null) {
                    TextButton(onClick = { clearForm() }) { Text("Отменить редактирование") }
                }
            }
        }

        if (error != null) {
            ErrorCard(error!!)
        }
        if (!message.isNullOrBlank()) {
            EmptyStateCard(title = "Статус", subtitle = message)
        }
    }
}
