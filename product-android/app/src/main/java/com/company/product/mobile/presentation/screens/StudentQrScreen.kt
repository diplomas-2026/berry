package com.company.product.mobile.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.company.product.mobile.data.remote.QrPayload
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun StudentQrScreen(repo: AppRepository) {
    var qr by remember { mutableStateOf<QrPayload?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                qr = repo.studentQr()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    ScreenContainer("Мой QR") {
        if (loading) CenterLoading()
        if (error != null) Text("Ошибка: $error")
        if (qr != null) {
            SectionCard(title = "Данные талона", subtitle = "Покажите этот экран повару") {
                Text("studentId: ${qr!!.studentId}")
                Text("date: ${qr!!.date}")
                Text("Эти поля используются как payload для сканирования.")
            }
        }
    }
}
