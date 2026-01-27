package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.Booking
import com.example.myapplication.data.model.BookingStatus
import com.example.myapplication.data.model.GroupClass
import com.example.myapplication.data.model.Subscription
import com.example.myapplication.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

data class UserUiState(
    val remainingDays: Int = 0,
    val subscription: Subscription? = null,
    val groupClasses: List<GroupClass> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val allBookings: List<Booking> = emptyList(), // All bookings for counting places
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UserViewModel(
    private val repository: FitnessRepository,
    private val userId: Long
) : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getRemainingDays(userId).collect { days ->
                _uiState.value = _uiState.value.copy(remainingDays = days)
            }
        }
        
        viewModelScope.launch {
            repository.getActiveSubscription(userId).collect { subscription ->
                _uiState.value = _uiState.value.copy(subscription = subscription)
            }
        }
        
        viewModelScope.launch {
            repository.getAllClasses().collect { classes ->
                _uiState.value = _uiState.value.copy(groupClasses = classes)
            }
        }
        
        viewModelScope.launch {
            repository.getUserBookings(userId).collect { bookings ->
                _uiState.value = _uiState.value.copy(bookings = bookings)
            }
        }
        
        viewModelScope.launch {
            repository.getAllBookings().collect { allBookings ->
                _uiState.value = _uiState.value.copy(allBookings = allBookings)
            }
        }
    }

    suspend fun getBookingStatus(classId: Long, date: Long): BookingStatus {
        return repository.getBookingStatus(classId, date, userId)
    }

    fun bookClass(classId: Long, date: Long) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)
                
                calendar.timeInMillis = date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                // Check if booking is for today
                if (calendar.timeInMillis != today.timeInMillis) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Запись доступна только в день тренировки")
                    return@launch
                }
                
                val status = getBookingStatus(classId, date)
                if (status == BookingStatus.AVAILABLE) {
                    val booking = Booking(userId = userId, classId = classId, date = date)
                    repository.insertBooking(booking)
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Невозможно записаться")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    fun cancelBooking(classId: Long, date: Long) {
        viewModelScope.launch {
            try {
                repository.cancelBooking(userId, classId, date)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    fun freezeSubscription() {
        viewModelScope.launch {
            try {
                val subscription = _uiState.value.subscription
                if (subscription == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Нет активного абонемента")
                    return@launch
                }
                
                if (subscription.freezeUsedThisMonth) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Вы уже использовали заморозку в этом месяце")
                    return@launch
                }
                
                val now = System.currentTimeMillis()
                val weekInMillis = 7 * 24 * 60 * 60 * 1000L
                val updatedSubscription = subscription.copy(
                    isFrozen = true,
                    freezeStartDate = now,
                    freezeEndDate = now + weekInMillis,
                    freezeUsedThisMonth = true
                )
                repository.updateSubscription(updatedSubscription)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun logout() {
        // Clear state if needed
    }
}

