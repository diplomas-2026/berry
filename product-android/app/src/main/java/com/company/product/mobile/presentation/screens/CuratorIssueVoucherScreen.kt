package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun CuratorIssueVoucherScreen(repo: AppRepository) {
    var studentId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var breakfast by remember { mutableStateOf(true) }
    var lunch by remember { mutableStateOf(true) }
    var dinner by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ScreenContainer("Выдача талонов") {
        OutlinedTextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("ID студента") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Дата (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Row { Checkbox(checked = breakfast, onCheckedChange = { breakfast = it }); Text("BREAKFAST") }
        Row { Checkbox(checked = lunch, onCheckedChange = { lunch = it }); Text("LUNCH") }
        Row { Checkbox(checked = dinner, onCheckedChange = { dinner = it }); Text("DINNER") }
        Button(
            onClick = {
                scope.launch {
                    loading = true
                    try {
                        val slots = buildList {
                            if (breakfast) add("BREAKFAST")
                            if (lunch) add("LUNCH")
                            if (dinner) add("DINNER")
                        }
                        val issued = repo.issueVouchers(studentId.toLong(), date, slots)
                        result = "Готово. Последние записи: ${issued.size}"
                    } catch (e: Exception) {
                        result = "Ошибка: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && studentId.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Выдача..." else "Выдать талоны")
        }
        if (result.isNotBlank()) Text(result)
    }
}
