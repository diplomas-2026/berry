package com.company.product.mobile.presentation.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.company.product.mobile.data.remote.VoucherDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun StudentVouchersScreen(repo: AppRepository) {
    var vouchers by remember { mutableStateOf<List<VoucherDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                vouchers = repo.studentVouchers()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    ScreenContainer("Мои талоны") {
        if (loading) CenterLoading()
        if (error != null) ErrorCard(error!!)
        vouchers.forEach {
            SectionCard(title = "Талон #${it.id}", subtitle = formatDate(it.issueDate)) {
                Text("Приём пищи: ${mealSlotLabel(it.mealSlot)}")
                StatusPill(voucherStatusLabel(it.status))
            }
        }
    }
}
