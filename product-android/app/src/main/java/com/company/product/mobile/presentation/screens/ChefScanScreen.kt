package com.company.product.mobile.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
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
fun ChefScanScreen(repo: AppRepository, onDishClick: (Long) -> Unit = {}) {
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
    var query by remember { mutableStateOf("") }
    var voucherFilter by remember { mutableStateOf("ALL") }
    var sortMode by remember { mutableStateOf("slot") }

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
            val visibleVouchers = scanResult!!.activeVouchers
                .filter { voucher ->
                    val matchesQuery = query.isBlank() ||
                        voucher.studentName.contains(query, ignoreCase = true) ||
                        mealSlotLabel(voucher.mealSlot).contains(query, ignoreCase = true) ||
                        voucherStatusLabel(voucher.status).contains(query, ignoreCase = true)
                    val matchesFilter = voucherFilter == "ALL" || voucher.status == voucherFilter
                    matchesQuery && matchesFilter
                }
                .sortedWith(
                    when (sortMode) {
                        "status" -> compareBy<com.company.product.mobile.data.remote.VoucherDto> { voucherStatusSortIndex(it.status) }.thenByDescending { it.issueDate }
                        else -> compareByDescending<com.company.product.mobile.data.remote.VoucherDto> { it.issueDate }.thenBy { it.id }
                    }
                )
            val visibleMenuItems = scanResult!!.menuItems
                .filter { item ->
                    query.isBlank() ||
                        item.dish.name.contains(query, ignoreCase = true) ||
                        item.dish.description.orEmpty().contains(query, ignoreCase = true) ||
                        mealSlotLabel(item.mealSlot).contains(query, ignoreCase = true)
                }
                .sortedBy { chefMenuScanSortIndex(it.mealSlot) }

            SectionCard(title = "Студент", subtitle = scanResult!!.student.fullName) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск") },
                    placeholder = { Text("Талон или блюдо") },
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Фильтр талонов")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ALL" to "Все", "ISSUED" to "Выданы", "REDEEMED" to "Погашены").forEach { (value, label) ->
                            FilterChip(selected = voucherFilter == value, onClick = { voucherFilter = value }, label = { Text(label) })
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Сортировка")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("slot" to "По приёму", "status" to "По статусу").forEach { (value, label) ->
                            FilterChip(selected = sortMode == value, onClick = { sortMode = value }, label = { Text(label) })
                        }
                    }
                }

                Text("Доступные талоны:")
                if (visibleVouchers.isEmpty()) {
                    Text("Активных талонов нет")
                } else {
                    visibleVouchers.forEach { voucher ->
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
                if (visibleMenuItems.isEmpty()) {
                    Text("Для этого студента меню на выбранную дату не найдено")
                } else {
                    visibleMenuItems.forEach {
                        SectionCard(
                            title = "${mealSlotLabel(it.mealSlot)}: ${it.dish.name}",
                            subtitle = formatDate(it.date),
                            onClick = { onDishClick(it.dish.id) }
                        ) {
                            Text(it.dish.description ?: "Нет описания")
                        }
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

private fun chefMenuScanSortIndex(slot: String): Int = when (slot) {
    "BREAKFAST" -> 0
    "LUNCH" -> 1
    "DINNER" -> 2
    else -> Int.MAX_VALUE
}

private fun voucherStatusSortIndex(status: String): Int = when (status) {
    "ISSUED" -> 0
    "REDEEMED" -> 1
    "EXPIRED" -> 2
    "CANCELLED" -> 3
    else -> Int.MAX_VALUE
}
