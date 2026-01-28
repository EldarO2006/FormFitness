package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.viewmodel.AdminViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    onThemeToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Форма Фитнес - Администратор") },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Тема"
                        )
                    }
                    TextButton(onClick = onLogout) {
                        Text("Выйти в авторизацию")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Пользователи") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Группы") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Статистика") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Абонементы") }
                )
            }

            when (selectedTab) {
                0 -> UsersTab(
                    users = uiState.users,
                    onDelete = { viewModel.deleteUser(it) },
                    onUpdate = { viewModel.updateUser(it) }
                )
                1 -> GroupsTab(
                    groupClasses = uiState.groupClasses,
                    onAdd = { viewModel.addClass(it) },
                    onUpdate = { viewModel.updateClass(it) },
                    onDelete = { viewModel.deleteClass(it) }
                )
                2 -> StatisticsTab(statistics = uiState.statistics)
                3 -> SubscriptionsTab(subscriptions = uiState.subscriptions)
            }
        }
    }

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@Composable
fun UsersTab(
    users: List<com.example.myapplication.data.model.User>,
    onDelete: (com.example.myapplication.data.model.User) -> Unit,
    onUpdate: (com.example.myapplication.data.model.User) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Логин: ${user.login} | Роль: ${user.role.name}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (user.phone != null) {
                            Text(
                                text = "Телефон: ${user.phone}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (user.email != null) {
                            Text(
                                text = "Email: ${user.email}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    IconButton(
                        onClick = { onDelete(user) },
                        modifier = Modifier
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroupsTab(
    groupClasses: List<com.example.myapplication.data.model.GroupClass>,
    onAdd: (com.example.myapplication.data.model.GroupClass) -> Unit,
    onUpdate: (com.example.myapplication.data.model.GroupClass) -> Unit,
    onDelete: (com.example.myapplication.data.model.GroupClass) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить группу")
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groupClasses) { groupClass ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = groupClass.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = groupClass.description,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "День: ${getDayNameAdmin(groupClass.dayOfWeek)} | Время: ${groupClass.time} | Макс: ${groupClass.maxParticipants}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { onDelete(groupClass) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddGroupDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { groupClass ->
                onAdd(groupClass)
                showAddDialog = false
            }
        )
    }
}

private fun getDayNameAdmin(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        Calendar.MONDAY -> "Понедельник"
        Calendar.TUESDAY -> "Вторник"
        Calendar.WEDNESDAY -> "Среда"
        Calendar.THURSDAY -> "Четверг"
        Calendar.FRIDAY -> "Пятница"
        Calendar.SATURDAY -> "Суббота"
        Calendar.SUNDAY -> "Воскресенье"
        else -> "Неизвестно"
    }
}

@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (com.example.myapplication.data.model.GroupClass) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf(java.util.Calendar.MONDAY) }
    var time by remember { mutableStateOf("10:00") }
    var maxParticipants by remember { mutableStateOf("8") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить группу") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                var selectedDay by remember { mutableStateOf(dayOfWeek) }
                Text("День недели:")
                Row {
                    listOf(
                        java.util.Calendar.MONDAY to "Пн",
                        java.util.Calendar.WEDNESDAY to "Ср",
                        java.util.Calendar.FRIDAY to "Пт"
                    ).forEach { (day, label) ->
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { 
                                selectedDay = day
                                dayOfWeek = day
                            },
                            label = { Text(label) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Время (HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxParticipants,
                    onValueChange = { maxParticipants = it },
                    label = { Text("Макс. участников") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        com.example.myapplication.data.model.GroupClass(
                            name = name,
                            description = description,
                            dayOfWeek = dayOfWeek,
                            time = time,
                            maxParticipants = maxParticipants.toIntOrNull() ?: 8
                        )
                    )
                },
                enabled = name.isNotBlank() && description.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun StatisticsTab(
    statistics: com.example.myapplication.ui.viewmodel.Statistics
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            title = "Всего пользователей",
            value = statistics.totalUsers.toString()
        )
        StatCard(
            title = "Активных абонементов",
            value = statistics.activeSubscriptions.toString()
        )
        StatCard(
            title = "Всего записей",
            value = statistics.totalBookings.toString()
        )
        StatCard(
            title = "Всего групп",
            value = statistics.totalClasses.toString()
        )
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SubscriptionsTab(
    subscriptions: List<com.example.myapplication.data.model.Subscription>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subscriptions) { subscription ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Абонемент #${subscription.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Тип: ${subscription.type.months} месяцев",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Пользователь ID: ${subscription.userId}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (subscription.isFrozen) {
                        Text(
                            text = "Заморожен",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

