package com.example.myapplication.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.BookingStatus
import com.example.myapplication.data.model.GroupClass
import com.example.myapplication.ui.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    viewModel: UserViewModel,
    onLogout: () -> Unit,
    onThemeToggle: () -> Unit,
    isDarkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Форма Фитнес") },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Тема"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Выход"
                        )
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
                    text = { Text("Абонемент") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Занятия") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Мои записи") }
                )
            }

            when (selectedTab) {
                0 -> SubscriptionTab(uiState.remainingDays, uiState.subscription) {
                    viewModel.freezeSubscription()
                }
                1 -> GroupClassesTab(
                    uiState.groupClasses,
                    uiState.bookings,
                    uiState.allBookings,
                    viewModel::bookClass
                )
                2 -> MyBookingsTab(
                    uiState.bookings,
                    uiState.groupClasses,
                    viewModel::cancelBooking
                )
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
fun SubscriptionTab(
    remainingDays: Int,
    subscription: com.example.myapplication.data.model.Subscription?,
    onFreeze: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                    text = "Абонемент",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (subscription != null) {
                    Text(
                        text = "Осталось дней: $remainingDays",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Тип: ${subscription.type.months} месяцев",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (subscription.isFrozen) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Абонемент заморожен",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Text(
                        text = "Нет активного абонемента",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (subscription != null && !subscription.freezeUsedThisMonth && !subscription.isFrozen) {
            Button(
                onClick = onFreeze,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Заморозить абонемент на неделю")
            }
        }
    }
}

@Composable
fun GroupClassesTab(
    classes: List<GroupClass>,
    bookings: List<com.example.myapplication.data.model.Booking>,
    allBookings: List<com.example.myapplication.data.model.Booking>,
    onBook: (Long, Long) -> Unit
) {
    val today = Calendar.getInstance()
    val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)
    val monday = Calendar.MONDAY
    val wednesday = Calendar.WEDNESDAY
    val friday = Calendar.FRIDAY
    
    val todayClasses = classes.filter { 
        val classDay = when (it.dayOfWeek) {
            Calendar.MONDAY -> Calendar.MONDAY
            Calendar.WEDNESDAY -> Calendar.WEDNESDAY
            Calendar.FRIDAY -> Calendar.FRIDAY
            else -> -1
        }
        dayOfWeek == classDay
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (todayClasses.isEmpty()) {
            item {
                Text(
                    text = "Сегодня нет групповых занятий",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(todayClasses) { groupClass ->
                GroupClassCard(groupClass, bookings, allBookings, onBook)
            }
        }
    }
}

@Composable
fun GroupClassCard(
    groupClass: GroupClass,
    bookings: List<com.example.myapplication.data.model.Booking>,
    allBookings: List<com.example.myapplication.data.model.Booking>,
    onBook: (Long, Long) -> Unit
) {
    val today = Calendar.getInstance()
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)
    val date = today.timeInMillis
    
    // Use allBookings for counting total places taken
    val bookingCount = allBookings.count { 
        it.classId == groupClass.id && it.date == date 
    }
    // Use user's bookings to check if current user is booked
    val isBooked = bookings.any { 
        it.classId == groupClass.id && it.date == date 
    }
    val status = when {
        isBooked -> BookingStatus.BOOKED
        bookingCount >= groupClass.maxParticipants -> BookingStatus.FULL
        else -> BookingStatus.AVAILABLE
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = groupClass.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = groupClass.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${groupClass.time} | Мест: $bookingCount/${groupClass.maxParticipants}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = when (status) {
                        BookingStatus.BOOKED -> "Записан"
                        BookingStatus.FULL -> "Нет мест"
                        BookingStatus.AVAILABLE -> "Доступно"
                    },
                    color = when (status) {
                        BookingStatus.BOOKED -> MaterialTheme.colorScheme.primary
                        BookingStatus.FULL -> MaterialTheme.colorScheme.error
                        BookingStatus.AVAILABLE -> MaterialTheme.colorScheme.tertiary
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (status == BookingStatus.AVAILABLE) {
                Button(
                    onClick = { onBook(groupClass.id, date) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Записаться")
                }
            } else if (status == BookingStatus.BOOKED) {
                OutlinedButton(
                    onClick = { /* Cancel handled in MyBookingsTab */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false
                ) {
                    Text("Вы записаны")
                }
            }
        }
    }
}

@Composable
fun MyBookingsTab(
    bookings: List<com.example.myapplication.data.model.Booking>,
    classes: List<GroupClass>,
    onCancel: (Long, Long) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (bookings.isEmpty()) {
            item {
                Text(
                    text = "У вас нет записей",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(bookings) { booking ->
                val groupClass = classes.find { it.id == booking.classId }
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = groupClass?.name ?: "Неизвестное занятие",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (groupClass != null) {
                            // booking.date contains the date of the class (timestamp at midnight)
                            val classDate = Date(booking.date)
                            
                            Text(
                                text = "Дата занятия: ${dateFormat.format(classDate)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Время: ${groupClass.time}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "День: ${getDayNameForBooking(groupClass.dayOfWeek)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Text(
                                text = "Дата занятия: ${dateFormat.format(Date(booking.date))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onCancel(booking.classId, booking.date) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Отменить запись")
                        }
                    }
                }
            }
        }
    }
}

private fun getDayNameForBooking(dayOfWeek: Int): String {
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

