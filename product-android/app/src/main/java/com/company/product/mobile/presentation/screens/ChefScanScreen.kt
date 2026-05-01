package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.company.product.mobile.data.remote.ScanResultDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ChefScanScreen(repo: AppRepository) {
    var studentId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var voucherIdForRedeem by remember { mutableStateOf("") }
    var scanResult by remember { mutableStateOf<ScanResultDto?>(null) }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    ScreenContainer("Сканирование и погашение") {
        OutlinedTextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("studentId из QR") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("date из QR (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                scope.launch {
                    loading = true
                    try {
                        scanResult = repo.chefScan(studentId.toLong(), date)
                        message = "Проверка выполнена"
                    } catch (e: Exception) {
                        message = "Ошибка: ${e.message}"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading && studentId.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Проверяем..." else "Проверить QR")
        }

        if (scanResult != null) {
            Text("Студент: ${scanResult!!.student.fullName}")
            Text("Доступные талоны:")
            scanResult!!.activeVouchers.forEach { Text("ID ${it.id} | ${it.mealSlot} | ${it.status}") }
            Text("Блюда к выдаче:")
            scanResult!!.menuItems.forEach { Text("${it.mealSlot}: ${it.dish.name}") }
        }

        OutlinedTextField(
            value = voucherIdForRedeem,
            onValueChange = { voucherIdForRedeem = it },
            label = { Text("ID талона для погашения") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                scope.launch {
                    try {
                        val result = repo.chefRedeem(voucherIdForRedeem.toLong())
                        message = "Погашено: #${result.id} (${result.mealSlot})"
                    } catch (e: Exception) {
                        message = "Ошибка погашения: ${e.message}"
                    }
                }
            },
            enabled = voucherIdForRedeem.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Погасить талон") }

        if (message.isNotBlank()) Text(message)
    }
}
