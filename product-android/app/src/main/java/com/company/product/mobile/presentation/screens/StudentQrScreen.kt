package com.company.product.mobile.presentation.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.QrPayload
import com.company.product.mobile.data.repository.AppRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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
        if (loading) {
            CenterLoading()
        }
        if (error != null) {
            ErrorCard(error!!)
        }
        val payload = qr?.let { buildQrPayload(it) }
        if (qr != null && payload != null) {
            SectionCard(
                title = "Покажите код повару",
                subtitle = "Он содержит данные студента и текущую дату"
            ) {
                QrPreview(payload = payload)
                Text(
                    text = "QR обновляется автоматически и привязан к текущей сессии.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard(
                title = "Payload",
                subtitle = "Можно скопировать строку, если нужно проверить сканирование"
            ) {
                QrPayloadRow(payload = payload)
            }
        }
    }
}

@Composable
private fun QrPreview(payload: String) {
    val qrBitmap = remember(payload) { createQrBitmap(payload) }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR-код студента",
                modifier = Modifier.size(240.dp)
            )
        }
    }
}

@Composable
private fun QrPayloadRow(payload: String) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = payload,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = {
                clipboard.setText(AnnotatedString(payload))
                Toast.makeText(context, "Payload скопирован", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Text("Скопировать payload")
        }
    }
}

private fun buildQrPayload(qr: QrPayload): String {
    return """{"studentId":${qr.studentId},"date":"${qr.date}"}"""
}

private fun createQrBitmap(payload: String): Bitmap {
    val size = 900
    val writer = QRCodeWriter()
    val matrix = writer.encode(payload, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (matrix[x, y]) 0xFF0B1720.toInt() else 0xFFFFFFFF.toInt())
        }
    }
    return bitmap
}
