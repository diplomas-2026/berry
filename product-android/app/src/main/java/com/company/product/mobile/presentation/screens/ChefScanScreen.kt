package com.company.product.mobile.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.company.product.mobile.data.remote.ScanResultDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun ChefScanScreen(repo: AppRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraActive by remember { mutableStateOf(true) }
    var scanResult by remember { mutableStateOf<ScanResultDto?>(null) }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var scannedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
        message = if (granted) "" else "Для сканирования нужен доступ к камере"
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    ScreenContainer("Сканирование и погашение") {
        if (!cameraPermissionGranted) {
            EmptyStateCard(
                title = "Нет доступа к камере",
                subtitle = "Разрешите камеру, чтобы сканировать QR-код студента"
            )
            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Разрешить камеру")
            }
        } else {
            SectionCard(
                title = "Сканер QR",
                subtitle = "Наведите камеру на QR-код студента"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp)
                    ) {
                        QrCameraScanner(
                            enabled = cameraActive && !scannedOnce,
                            onQrScanned = { raw ->
                                if (scannedOnce || loading) return@QrCameraScanner
                                val payload = runCatching { JSONObject(raw) }.getOrNull()
                                if (payload == null) {
                                    scannedOnce = true
                                    cameraActive = false
                                    message = "QR-код не содержит ожидаемые данные"
                                    return@QrCameraScanner
                                }
                                val studentId = payload.optLong("studentId", -1L)
                                val date = payload.optString("date", "")
                                if (studentId <= 0 || date.isBlank()) {
                                    scannedOnce = true
                                    cameraActive = false
                                    message = "QR-код имеет неверный формат"
                                    return@QrCameraScanner
                                }

                                scannedOnce = true
                                cameraActive = false
                                scope.launch {
                                    loading = true
                                    try {
                                        scanResult = repo.chefScan(studentId, date)
                                        message = "QR-код успешно считан"
                                    } catch (e: Exception) {
                                        message = "Ошибка: ${e.message}"
                                    } finally {
                                        loading = false
                                    }
                                }
                            }
                        )
                    }

                    if (loading) {
                        CenterLoading()
                    }

                    Button(
                        onClick = {
                            scannedOnce = false
                            cameraActive = true
                            scanResult = null
                            message = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Сканировать ещё раз")
                    }
                }
            }
        }

        if (scanResult != null) {
            SectionCard(title = "Студент", subtitle = scanResult!!.student.fullName) {
                Text("Доступные талоны:")
                if (scanResult!!.activeVouchers.isEmpty()) {
                    Text("Активных талонов нет")
                } else {
                    scanResult!!.activeVouchers.forEach { voucher ->
                        SectionCard(
                            title = "Талон #${voucher.id}",
                            subtitle = "${mealSlotLabel(voucher.mealSlot)} • ${voucherStatusLabel(voucher.status)} • ${formatDate(voucher.issueDate)}"
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        loading = true
                                        try {
                                            val result = repo.chefRedeem(voucher.id)
                                            message = "Погашено: #${result.id} (${mealSlotLabel(result.mealSlot)})"
                                        } catch (e: Exception) {
                                            message = "Ошибка погашения: ${e.message}"
                                        } finally {
                                            loading = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Погасить талон")
                            }
                        }
                    }
                }
                Text("Блюда к выдаче:")
                if (scanResult!!.menuItems.isEmpty()) {
                    Text("Для этого студента меню на выбранную дату не найдено")
                } else {
                    scanResult!!.menuItems.forEach {
                        Text("${mealSlotLabel(it.mealSlot)}: ${it.dish.name}")
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            if (message.startsWith("Ошибка")) {
                ErrorCard(message)
            } else {
                EmptyStateCard(title = "Статус", subtitle = message)
            }
        }
    }
}
