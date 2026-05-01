package com.company.product.mobile.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun ChefDishCreateScreen(
    repo: AppRepository,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var proteins by remember { mutableStateOf("") }
    var fats by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var selectedPhoto by remember { mutableStateOf<Uri?>(null) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedPhoto = uri }

    ScreenContainer("Создать блюдо") {
        SectionCard(
            title = "Новая карточка блюда",
            subtitle = "Можно заполнить название, БЖУ, калорийность и добавить фото"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MediaFrame(
                    url = null,
                    model = selectedPhoto,
                    placeholderTitle = "Фото блюда не выбрано",
                    placeholderSubtitle = "Добавьте фото позже или сразу сейчас",
                    aspectRatio = 16f / 9f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Выбрать фото")
                    }
                    if (selectedPhoto != null) {
                        TextButton(onClick = { selectedPhoto = null }) {
                            Text("Убрать фото")
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    placeholder = { Text("Например, Омлет с овощами") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    placeholder = { Text("Краткое описание блюда") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Пищевая ценность на 100 г", style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = proteins,
                        onValueChange = { proteins = it },
                        label = { Text("Белки, г") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fats,
                        onValueChange = { fats = it },
                        label = { Text("Жиры, г") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Углеводы, г") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Калорийность, ккал") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        if (name.isBlank()) {
                            error = "Укажите название блюда"
                            return@Button
                        }
                        scope.launch {
                            saving = true
                            error = null
                            try {
                                repo.chefCreateDish(
                                    context = context,
                                    name = name.trim(),
                                    description = description.trim().takeIf { it.isNotBlank() },
                                    proteinsPer100g = proteins.trim().takeIf { it.isNotBlank() },
                                    fatsPer100g = fats.trim().takeIf { it.isNotBlank() },
                                    carbsPer100g = carbs.trim().takeIf { it.isNotBlank() },
                                    caloriesPer100g = calories.trim().takeIf { it.isNotBlank() },
                                    photoUri = selectedPhoto
                                )
                                message = "Блюдо создано"
                                onDone()
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                saving = false
                            }
                        }
                    },
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Создать блюдо")
                }
            }
        }

        if (error != null) {
            ErrorCard(error!!)
        }

        if (!message.isNullOrBlank()) {
            EmptyStateCard(title = "Статус", subtitle = message)
        }
    }
}
