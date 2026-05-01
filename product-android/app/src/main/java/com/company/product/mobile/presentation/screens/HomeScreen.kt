package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.company.product.mobile.presentation.SessionUi

@Composable
fun HomeScreen(session: SessionUi?, onOpen: (String) -> Unit, onLogout: () -> Unit) {
    ScreenContainer("Главная") {
        SectionCard(
            title = session?.fullName ?: "Пользователь",
            subtitle = session?.email ?: "-"
        ) {
            StatusPill("Роль: ${session?.role ?: "-"}")
        }

        SectionCard(title = "Разделы") {
            when (session?.role) {
                "STUDENT" -> {
                    Button(onClick = { onOpen("student_menu") }, modifier = Modifier.fillMaxWidth()) { Text("Моё меню") }
                    Button(onClick = { onOpen("student_vouchers") }, modifier = Modifier.fillMaxWidth()) { Text("Мои талоны") }
                    Button(onClick = { onOpen("student_qr") }, modifier = Modifier.fillMaxWidth()) { Text("Мой QR") }
                }
                "CURATOR" -> {
                    Button(onClick = { onOpen("curator_students") }, modifier = Modifier.fillMaxWidth()) { Text("Студенты группы") }
                    Button(onClick = { onOpen("curator_issue") }, modifier = Modifier.fillMaxWidth()) { Text("Выдача талонов") }
                }
                "CHEF" -> {
                    Button(onClick = { onOpen("chef_menu") }, modifier = Modifier.fillMaxWidth()) { Text("Текущее меню") }
                    Button(onClick = { onOpen("chef_scan") }, modifier = Modifier.fillMaxWidth()) { Text("Сканирование / погашение") }
                }
                "ADMIN" -> {
                    Button(onClick = { onOpen("admin_users") }, modifier = Modifier.fillMaxWidth()) { Text("Пользователи") }
                }
            }
        }

        SectionCard(title = "Аккаунт") {
            OutlinedButton(onClick = { onOpen("profile") }, modifier = Modifier.fillMaxWidth()) {
                Text("Профиль")
            }
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Выйти")
            }
        }
    }
}
