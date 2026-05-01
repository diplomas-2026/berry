package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
        Text("Авторизуйтесь в системе питания", style = MaterialTheme.typography.bodyMedium)
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Входим..." else "Войти")
        }
    }
}
