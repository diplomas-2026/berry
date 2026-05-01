package com.company.product.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
private fun App() {
    MaterialTheme {
        val nav = rememberNavController()
        var session by remember { mutableStateOf<UserSession?>(null) }
        NavHost(
            navController = nav,
            startDestination = if (session == null) "login" else "home"
        ) {
            composable("login") {
                LoginScreen(
                    onLogin = { email, password ->
                        // Temporary mock to keep project runnable; wire to Retrofit service next.
                        session = when {
                            email.contains("admin") -> UserSession(email, "ADMIN")
                            email.contains("curator") -> UserSession(email, "CURATOR")
                            email.contains("chef") -> UserSession(email, "CHEF")
                            else -> UserSession(email, "STUDENT")
                        }
                        nav.navigate("home")
                    }
                )
            }
            composable("home") {
                HomeScreen(session = session, onLogout = {
                    session = null
                    nav.navigate("login")
                })
            }
        }
    }
}

data class UserSession(val email: String, val role: String)

@Composable
private fun LoginScreen(onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("student1@pgk.local") }
    var password by remember { mutableStateOf("student123") }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        Text("Вход", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onLogin(email, password) }, modifier = Modifier.fillMaxWidth()) { Text("Войти") }
    }
}

@Composable
private fun HomeScreen(session: UserSession?, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Главная", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Пользователь: ${session?.email ?: "-"}")
        Text("Роль: ${session?.role ?: "-"}")
        Spacer(Modifier.height(16.dp))
        when (session?.role) {
            "STUDENT" -> Text("Студент: Моё меню, Мои талоны, Мой QR, Профиль")
            "CURATOR" -> Text("Куратор: Студенты группы, Выдача талонов, Профиль")
            "CHEF" -> Text("Повар: Меню, Сканирование QR, Загрузка фото блюд, Профиль")
            "ADMIN" -> Text("Админ: Пользователи, Группы, Отчёты, Профиль")
            else -> Text("Нет роли")
        }
        Spacer(Modifier.height(20.dp))
        OutlinedButton(onClick = onLogout) { Text("Выйти") }
    }
}
