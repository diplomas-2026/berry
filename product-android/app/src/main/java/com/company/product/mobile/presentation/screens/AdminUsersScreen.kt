package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.company.product.mobile.data.remote.UserDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun AdminUsersScreen(repo: AppRepository) {
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STUDENT") }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            loading = true
            try {
                users = repo.adminUsers()
            } catch (e: Exception) {
                message = "Ошибка: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    ScreenContainer("Пользователи") {
        SectionCard(title = "Список пользователей") {
            Button(onClick = { reload() }, modifier = Modifier.fillMaxWidth()) { Text("Обновить список") }
            if (loading) CenterLoading()
            users.forEach { user ->
                SectionCard(title = user.fullName, subtitle = "${user.email} • ${user.role}") {
                    StatusPill(if (user.active) "Активен" else "Отключён")
                    Button(onClick = {
                        scope.launch {
                            try {
                                repo.adminSetActive(user.id, !user.active)
                                reload()
                            } catch (e: Exception) {
                                message = "Ошибка: ${e.message}"
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (user.active) "Деактивировать" else "Активировать")
                    }
                }
            }
        }

        SectionCard(title = "Создание пользователя") {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("ФИО") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Роль (ADMIN/CURATOR/CHEF/STUDENT)") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = {
                scope.launch {
                    try {
                        repo.adminCreateUser(email.trim(), password, fullName, role.trim().uppercase())
                        message = "Пользователь создан"
                        reload()
                    } catch (e: Exception) {
                        message = "Ошибка: ${e.message}"
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Создать пользователя") }
        }
        if (message.isNotBlank()) SectionCard(title = "Статус") { Text(message) }
    }
}
