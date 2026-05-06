package com.company.product.mobile.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import com.company.product.mobile.data.repository.AppRepository
import kotlinx.coroutines.launch

@Composable
fun AdminGroupsScreen(
    repo: AppRepository,
    onCreateGroup: () -> Unit,
    onEditGroup: (Long) -> Unit
) {
    var groups by remember { mutableStateOf<List<GroupDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf("name") }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            loading = true
            error = null
            try {
                groups = repo.adminGroups()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { reload() }

    val visibleGroups = remember(groups, query, sortMode) {
        groups
            .filter { group ->
                query.isBlank() ||
                    group.name.contains(query, ignoreCase = true) ||
                    group.curatorName.orEmpty().contains(query, ignoreCase = true)
            }
            .sortedWith(
                when (sortMode) {
                    "curator" -> compareBy<GroupDto> { it.curatorName.orEmpty().lowercase() }.thenBy { it.name.lowercase() }
                    else -> compareBy { it.name.lowercase() }
                }
            )
    }

    ScreenContainer("Группы") {
        SectionCard(
            title = "Создание группы",
            subtitle = "Форма вынесена на отдельный экран"
        ) {
            Button(onClick = onCreateGroup, modifier = Modifier.fillMaxWidth()) {
                Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Создать группу")
            }
        }

        SectionCard(title = "Список групп", subtitle = "Можно открыть отдельный экран редактирования") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Поиск") },
                    placeholder = { Text("Название группы или куратор") },
                    leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("name" to "По названию", "curator" to "По куратору").forEach { (value, label) ->
                        FilterChip(selected = sortMode == value, onClick = { sortMode = value }, label = { Text(label) })
                    }
                }
                Button(onClick = { reload() }, modifier = Modifier.fillMaxWidth()) { Text("Обновить список") }
            }
            if (loading) CenterLoading()
            if (visibleGroups.isEmpty() && !loading) {
                EmptyStateCard(
                    title = "Группы не найдены",
                    subtitle = if (query.isNotBlank()) "Ничего не найдено по текущему поиску" else "Сейчас список групп пуст"
                )
            } else {
                visibleGroups.forEach { group ->
                    SectionCard(
                        title = group.name,
                        subtitle = group.curatorName ?: "Куратор не назначен"
                    ) {
                        StatusPill("ID: ${group.id}")
                        TextButton(onClick = { onEditGroup(group.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Редактировать")
                        }
                    }
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
