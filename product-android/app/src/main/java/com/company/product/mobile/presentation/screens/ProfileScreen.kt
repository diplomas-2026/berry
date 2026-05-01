package com.company.product.mobile.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.company.product.mobile.presentation.AppState
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(appState: AppState) {
    val repo = appState.repository()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var avatarUrl by remember { mutableStateOf(appState.session?.avatarUrl) }
    var message by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedUri = uri }

    ScreenContainer("Профиль") {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(model = avatarUrl, contentDescription = "Аватар", modifier = Modifier.fillMaxWidth())
        } else {
            Text("Аватар не установлен")
        }

        Button(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.fillMaxWidth()) {
            Text("Выбрать фото")
        }
        Button(
            onClick = {
                val uri = selectedUri ?: return@Button
                scope.launch {
                    try {
                        val updated = repo.uploadAvatar(context, uri)
                        avatarUrl = updated.avatarUrl
                        message = "Аватар обновлен"
                    } catch (e: Exception) {
                        message = "Ошибка загрузки: ${e.message}"
                    }
                }
            },
            enabled = selectedUri != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Загрузить аватар") }
        Button(onClick = {
            scope.launch {
                try {
                    val updated = repo.deleteAvatar()
                    avatarUrl = updated.avatarUrl
                    message = "Аватар удалён"
                } catch (e: Exception) {
                    message = "Ошибка: ${e.message}"
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Удалить аватар")
        }
        if (message.isNotBlank()) Text(message)
    }
}
