package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
fun AdminUserEditorScreen(
    repo: AppRepository,
    userId: Long? = null,
    onDone: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var editingUser by remember { mutableStateOf<UserDto?>(null) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STUDENT") }
    var active by remember { mutableStateOf(true) }
    val roleOptions = remember {
        listOf(
            "Студент" to "STUDENT",
            "Куратор" to "CURATOR",
            "Повар" to "CHEF",
            "Администратор" to "ADMIN"
        )
    }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        loading = true
        error = null
            try {
                if (userId != null) {
                    editingUser = repo.adminUsers().firstOrNull { it.id == userId }
                    val user = editingUser ?: throw IllegalStateException("Пользователь не найден")
                    email = user.email
                    firstName = user.firstName
                    lastName = user.lastName
                middleName = user.middleName
                role = user.role
                active = user.active
            } else {
                editingUser = null
                email = ""
                password = ""
                firstName = ""
                lastName = ""
                middleName = ""
                role = "STUDENT"
                active = true
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    ScreenContainer(if (editingUser == null) "Создание пользователя" else "Редактирование пользователя") {
        if (loading) {
            CenterLoading()
        }

        if (error != null) {
            ErrorCard(error!!)
        }

        SectionCard(
            title = if (editingUser == null) "Новый пользователь" else "Данные пользователя",
            subtitle = "Имя, фамилия и отчество обязательны"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    readOnly = editingUser != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (editingUser == null) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Пароль") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Фамилия") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("Имя") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = middleName, onValueChange = { middleName = it }, label = { Text("Отчество") }, modifier = Modifier.fillMaxWidth())

                Text("Роль")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        roleOptions.forEach { (label, value) ->
                            FilterChip(
                                selected = role == value,
                                onClick = { role = value },
                                label = { Text(label) },
                                modifier = Modifier.padding(horizontal = 3.dp)
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Активен", modifier = Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }

                Button(
                    onClick = {
                        scope.launch {
                            saving = true
                            message = null
                            error = null
                            try {
                                if (editingUser == null) {
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
                                        id = editingUser!!.id,
                                        firstName = firstName.trim(),
                                        lastName = lastName.trim(),
                                        middleName = middleName.trim(),
                                        active = active,
                                        role = role
                                    )
                                    message = "Пользователь обновлён"
                                }
                                onDone()
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (saving) "Сохраняем..." else "Сохранить")
                }
                TextButton(onClick = onDone) { Text("Отмена") }
            }
        }

        if (!message.isNullOrBlank()) {
            EmptyStateCard(title = "Статус", subtitle = message)
        }
    }
}
