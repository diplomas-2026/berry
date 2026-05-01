package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.company.product.mobile.presentation.AppState
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(appState: AppState, onSuccess: () -> Unit) {
    var email by remember { mutableStateOf("student1@pgk.local") }
    var password by remember { mutableStateOf("student123") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ScreenContainer("Вход") {
        SectionCard(
            title = "Питание ПГК",
            subtitle = "Система управления талонами и меню"
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Быстрый доступ для студентов, поваров, кураторов и администраторов")
            }
        }

        SectionCard(
            title = "Авторизация",
            subtitle = "Введите данные учётной записи"
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
            Button(
                onClick = {
                    scope.launch {
                        loading = true
                        error = null
                        try {
                            appState.login(email.trim(), password)
                            onSuccess()
                        } catch (e: Exception) {
                            error = e.message ?: "Ошибка входа"
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (loading) "Входим..." else "Войти")
            }
        }
    }
}
