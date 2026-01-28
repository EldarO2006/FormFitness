package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.GroupClass
import com.example.myapplication.data.model.SubscriptionType
import com.example.myapplication.data.repository.FitnessRepository
import com.example.myapplication.ui.viewmodel.EmployeeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreen(
    viewModel: EmployeeViewModel,
    onLogout: () -> Unit,
    onThemeToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddClassDialog by remember { mutableStateOf(false) }
    var showAssignSubscriptionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Форма Фитнес - Сотрудник") },
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
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { showAddClassDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить")
                }
                1 -> FloatingActionButton(
                    onClick = { showAssignSubscriptionDialog = true }
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Привязать абонемент")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Расписание") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Абонементы") }
                )
            }

            when (selectedTab) {
                0 -> ScheduleTab(
                    classes = uiState.groupClasses,
                    onUpdate = { viewModel.updateClass(it) },
                    onDelete = { viewModel.deleteClass(it) }
                )
                1 -> SubscriptionManagementTab(
                    users = uiState.users,
                    repository = viewModel.repository,
                    onAssign = { userId, type -> viewModel.assignSubscription(userId, type) }
                )
            }
        }
    }

    if (showAddClassDialog) {
        AddClassDialog(
            onDismiss = { showAddClassDialog = false },
            onSave = { groupClass ->
                viewModel.addClass(groupClass)
                showAddClassDialog = false
            }
        )
    }

    if (showAssignSubscriptionDialog) {
        AssignSubscriptionDialog(
            users = uiState.users,
            onDismiss = { showAssignSubscriptionDialog = false },
            onAssign = { userId, type ->
                viewModel.assignSubscription(userId, type)
                showAssignSubscriptionDialog = false
            }
        )
    }

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@Composable
fun ScheduleTab(
    classes: List<GroupClass>,
    onUpdate: (GroupClass) -> Unit,
    onDelete: (GroupClass) -> Unit
) {
    var selectedDayFilter by remember { mutableStateOf<Int?>(null) }
    val dayChipsScroll = rememberScrollState()
    
    val filteredClasses = if (selectedDayFilter != null) {
        classes.filter { it.dayOfWeek == selectedDayFilter }
    } else {
        classes
    }
    
    val sortedClasses = filteredClasses.sortedWith(compareBy({ it.dayOfWeek }, { it.time }))
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips for days of week
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(dayChipsScroll)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedDayFilter == null,
                onClick = { selectedDayFilter = null },
                label = { Text("Все") }
            )
            FilterChip(
                selected = selectedDayFilter == Calendar.MONDAY,
                onClick = { 
                    selectedDayFilter = if (selectedDayFilter == Calendar.MONDAY) null else Calendar.MONDAY
                },
                label = { Text("Пн") }
            )
            FilterChip(
                selected = selectedDayFilter == Calendar.WEDNESDAY,
                onClick = { 
                    selectedDayFilter = if (selectedDayFilter == Calendar.WEDNESDAY) null else Calendar.WEDNESDAY
                },
                label = { Text("Ср") }
            )
            FilterChip(
                selected = selectedDayFilter == Calendar.FRIDAY,
                onClick = { 
                    selectedDayFilter = if (selectedDayFilter == Calendar.FRIDAY) null else Calendar.FRIDAY
                },
                label = { Text("Пт") }
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (sortedClasses.isEmpty()) {
                item {
                    Text(
                        text = "Нет занятий для выбранного дня",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(
                    items = sortedClasses,
                    key = { it.id }
                ) { groupClass: GroupClass ->
                    ScheduleItemCard(
                        groupClass = groupClass,
                        onUpdate = onUpdate,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleItemCard(
    groupClass: GroupClass,
    onUpdate: (GroupClass) -> Unit,
    onDelete: (GroupClass) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
                text = "День: ${getDayName(groupClass.dayOfWeek)} | Время: ${groupClass.time} | Макс: ${groupClass.maxParticipants}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showEditDialog = true }) {
                    Text("Изменить")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = { onDelete(groupClass) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            }
        }
    }

    if (showEditDialog) {
        EditClassDialog(
            groupClass = groupClass,
            onDismiss = { showEditDialog = false },
            onSave = {
                onUpdate(it)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun SubscriptionManagementTab(
    users: List<com.example.myapplication.data.model.User>,
    repository: FitnessRepository,
    onAssign: (Long, SubscriptionType) -> Unit
) {
    var selectedUser by remember { mutableStateOf<com.example.myapplication.data.model.User?>(null) }
    var subscription by remember { mutableStateOf<com.example.myapplication.data.model.Subscription?>(null) }
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (users.isEmpty()) {
            item {
                Text("Нет пользователей", modifier = Modifier.padding(16.dp))
            }
        } else {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        selectedUser = user
                        scope.launch {
                            subscription = repository.getActiveSubscription(user.id).firstOrNull()
                        }
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Логин: ${user.login}",
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
                }
            }
        }
    }
    
    selectedUser?.let { user ->
        UserDetailsDialog(
            user = user,
            subscription = subscription,
            onDismiss = { 
                selectedUser = null
                subscription = null
            },
            onAssign = { type ->
                onAssign(user.id, type)
                selectedUser = null
                subscription = null
            }
        )
    }
}

@Composable
fun UserDetailsDialog(
    user: com.example.myapplication.data.model.User,
    subscription: com.example.myapplication.data.model.Subscription?,
    onDismiss: () -> Unit,
    onAssign: (SubscriptionType) -> Unit
) {
    var selectedType by remember { mutableStateOf(SubscriptionType.ONE_MONTH) }
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Информация о клиенте") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Имя: ${user.name}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Логин: ${user.login}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (user.phone != null) {
                    Text(
                        text = "Телефон: ${user.phone}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (user.email != null) {
                    Text(
                        text = "Email: ${user.email}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                if (subscription != null) {
                    val now = System.currentTimeMillis()
                    val isActive = subscription.endDate > now && !subscription.isFrozen
                    val remainingDays = if (subscription.endDate > now) {
                        ((subscription.endDate - now) / (1000 * 60 * 60 * 24)).toInt()
                    } else {
                        0
                    }
                    
                    Text(
                        text = "Абонемент: ${if (isActive) "Активен" else "Неактивен"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Тип: ${subscription.type.months} месяцев",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Начало: ${dateFormat.format(Date(subscription.startDate))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Окончание: ${dateFormat.format(Date(subscription.endDate))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (isActive) {
                        Text(
                            text = "Осталось дней: $remainingDays",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (subscription.isFrozen) {
                        Text(
                            text = "Абонемент заморожен",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Text(
                        text = "Абонемент: Нет активного абонемента",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text("Назначить новый абонемент:")
                SubscriptionType.values().forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${type.months} месяцев")
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAssign(selectedType) }
            ) {
                Text("Назначить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
fun AddClassDialog(
    onDismiss: () -> Unit,
    onSave: (GroupClass) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf(Calendar.MONDAY) }
    var time by remember { mutableStateOf("10:00") }
    var maxParticipants by remember { mutableStateOf(8) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить занятие") },
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
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                var selectedDay by remember { mutableStateOf(0) }
                val chipsScroll = rememberScrollState()
                Row(modifier = Modifier.horizontalScroll(chipsScroll)) {
                    listOf("Пн", "Ср", "Пт").forEachIndexed { index, day ->
                        FilterChip(
                            selected = selectedDay == index,
                            onClick = {
                                selectedDay = index
                                dayOfWeek = when (index) {
                                    0 -> Calendar.MONDAY
                                    1 -> Calendar.WEDNESDAY
                                    else -> Calendar.FRIDAY
                                }
                            },
                            label = { Text(day) },
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
                    value = maxParticipants.toString(),
                    onValueChange = { maxParticipants = it.toIntOrNull() ?: 8 },
                    label = { Text("Макс. участников") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && description.isNotBlank()) {
                        onSave(
                            GroupClass(
                                name = name,
                                description = description,
                                dayOfWeek = dayOfWeek,
                                time = time,
                                maxParticipants = maxParticipants
                            )
                        )
                    }
                }
            ) {
                Text("Сохранить")
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
fun EditClassDialog(
    groupClass: GroupClass,
    onDismiss: () -> Unit,
    onSave: (GroupClass) -> Unit
) {
    var name by remember { mutableStateOf(groupClass.name) }
    var description by remember { mutableStateOf(groupClass.description) }
    var dayOfWeek by remember { mutableStateOf(groupClass.dayOfWeek) }
    var time by remember { mutableStateOf(groupClass.time) }
    var maxParticipants by remember { mutableStateOf(groupClass.maxParticipants) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редактировать занятие") },
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
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                var selectedDay by remember { mutableStateOf(
                    when (dayOfWeek) {
                        Calendar.MONDAY -> 0
                        Calendar.WEDNESDAY -> 1
                        Calendar.FRIDAY -> 2
                        else -> 0
                    }
                ) }
                val chipsScroll = rememberScrollState()
                Row(modifier = Modifier.horizontalScroll(chipsScroll)) {
                    listOf("Пн", "Ср", "Пт").forEachIndexed { index, day ->
                        FilterChip(
                            selected = selectedDay == index,
                            onClick = {
                                selectedDay = index
                                dayOfWeek = when (index) {
                                    0 -> Calendar.MONDAY
                                    1 -> Calendar.WEDNESDAY
                                    else -> Calendar.FRIDAY
                                }
                            },
                            label = { Text(day) },
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
                    value = maxParticipants.toString(),
                    onValueChange = { maxParticipants = it.toIntOrNull() ?: 8 },
                    label = { Text("Макс. участников") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        groupClass.copy(
                            name = name,
                            description = description,
                            dayOfWeek = dayOfWeek,
                            time = time,
                            maxParticipants = maxParticipants
                        )
                    )
                }
            ) {
                Text("Сохранить")
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
fun AssignSubscriptionDialog(
    users: List<com.example.myapplication.data.model.User>,
    onDismiss: () -> Unit,
    onAssign: (Long, SubscriptionType) -> Unit
) {
    var selectedUser by remember { mutableStateOf<com.example.myapplication.data.model.User?>(null) }
    var selectedType by remember { mutableStateOf(SubscriptionType.ONE_MONTH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Привязать абонемент") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Выберите пользователя:")
                users.forEach { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(user.name)
                        RadioButton(
                            selected = selectedUser?.id == user.id,
                            onClick = { selectedUser = user }
                        )
                    }
                }
                
                Divider()
                
                Text("Тип абонемента:")
                SubscriptionType.values().forEach { type ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${type.months} месяцев")
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedUser?.let {
                        onAssign(it.id, selectedType)
                    }
                },
                enabled = selectedUser != null
            ) {
                Text("Привязать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

fun getDayName(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        Calendar.MONDAY -> "Понедельник"
        Calendar.WEDNESDAY -> "Среда"
        Calendar.FRIDAY -> "Пятница"
        else -> "Неизвестно"
    }
}

