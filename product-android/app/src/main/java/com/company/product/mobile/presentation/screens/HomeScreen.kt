package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.weight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Dining
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.company.product.mobile.presentation.SessionUi

@Composable
fun HomeScreen(session: SessionUi?, onOpen: (String) -> Unit, onLogout: () -> Unit) {
    ScreenContainer("Главная") {
        SectionCard(
            title = session?.fullName ?: "Пользователь",
            subtitle = session?.email ?: "-"
        ) {
            StatusPill("Роль: ${roleLabel(session?.role)}")
        }

        SectionCard(
            title = "Разделы",
            subtitle = "Откройте нужный экран",
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f)
        ) {
            val entries = when (session?.role) {
                "STUDENT" -> listOf(
                    HomeTile("Моё меню", Icons.Default.RestaurantMenu) { onOpen("student_menu") },
                    HomeTile("Мои талоны", Icons.Default.Badge) { onOpen("student_vouchers") },
                    HomeTile("Мой QR", Icons.Default.QrCode2) { onOpen("student_qr") }
                )
                "CURATOR" -> listOf(
                    HomeTile("Студенты группы", Icons.Default.People) { onOpen("curator_students") },
                    HomeTile("Выдача талонов", Icons.Default.Badge) { onOpen("curator_issue") }
                )
                "CHEF" -> listOf(
                    HomeTile("Текущее меню", Icons.Default.RestaurantMenu) { onOpen("chef_menu") },
                    HomeTile("Сканирование", Icons.Default.Dining) { onOpen("chef_scan") }
                )
                "ADMIN" -> listOf(
                    HomeTile("Пользователи", Icons.Default.People) { onOpen("admin_users") }
                )
                else -> emptyList()
            }

            TileGrid(entries)
        }

        SectionCard(
            title = "Аккаунт",
            subtitle = "Профиль и выход",
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f)
        ) {
            TileGrid(
                listOf(
                    HomeTile("Профиль", Icons.Default.Person) { onOpen("profile") },
                    HomeTile("Выйти", Icons.Default.Logout) { onLogout() }
                )
            )
        }
    }
}

private data class HomeTile(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
private fun TileGrid(items: List<HomeTile>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    TileCard(
                        title = item.title,
                        icon = item.icon,
                        modifier = Modifier.weight(1f)
                    ) { item.onClick() }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
