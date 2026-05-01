package com.company.product.mobile.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.company.product.mobile.data.remote.UserDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun CuratorStudentsScreen(repo: AppRepository) {
    var students by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
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

    ScreenContainer("Студенты группы") {
        if (loading) CenterLoading()
        if (error != null) Text("Ошибка: $error")
        students.forEach {
            SectionCard(title = it.fullName, subtitle = it.email) {
                Text("ID: ${it.id}")
            }
        }
    }
}
