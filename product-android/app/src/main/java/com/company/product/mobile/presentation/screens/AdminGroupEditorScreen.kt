package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.company.product.mobile.data.remote.GroupDto
import com.company.product.mobile.data.remote.UserDto
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun AdminGroupEditorScreen(
    repo: AppRepository,
    groupId: Long? = null,
    onDone: () -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var editingGroup by remember { mutableStateOf<GroupDto?>(null) }
    var curators by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var curatorId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(groupId) {
        loading = true
        error = null
        try {
            curators = repo.adminUsers().filter { it.role == "CURATOR" }
            if (groupId != null) {
                editingGroup = repo.adminGroups().firstOrNull { it.id == groupId }
                val group = editingGroup ?: throw IllegalStateException("Группа не найдена")
                name = group.name
                curatorId = group.curatorId
            } else {
                editingGroup = null
                name = ""
                curatorId = null
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    ScreenContainer(if (editingGroup == null) "Создание группы" else "Редактирование группы") {
        if (loading) {
            CenterLoading()
        }

        if (error != null) {
            ErrorCard(error!!)
        }

        SectionCard(
            title = if (editingGroup == null) "Новая группа" else "Данные группы",
            subtitle = "Название группы и куратор"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название группы") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Куратор")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = curatorId == null,
                        onClick = { curatorId = null },
                        label = { Text("Без куратора") }
                    )
                    curators.forEach { curator ->
                        FilterChip(
                            selected = curatorId == curator.id,
                            onClick = { curatorId = curator.id },
                            label = { Text(curator.fullName) }
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            saving = true
                            message = null
                            error = null
                            try {
                                if (editingGroup == null) {
                                    repo.adminCreateGroup(name.trim(), curatorId)
                                    message = "Группа создана"
                                } else {
                                    repo.adminUpdateGroup(editingGroup!!.id, name.trim(), curatorId)
                                    message = "Группа обновлена"
                                }
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
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (saving) "Сохраняем..." else "Сохранить")
                }
                TextButton(onClick = onDone) { Text("Отмена") }
            }
        }

        if (!message.isNullOrBlank()) {
            EmptyStateCard(title = "Статус", subtitle = message)
        }
    }
}
