package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.Subscription
import com.example.myapplication.data.model.GroupClass
import com.example.myapplication.data.model.Booking
import com.example.myapplication.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AdminUiState(
    val users: List<User> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val groupClasses: List<GroupClass> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val statistics: Statistics = Statistics(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class Statistics(
    val totalUsers: Int = 0,
    val activeSubscriptions: Int = 0,
    val totalBookings: Int = 0,
    val totalClasses: Int = 0
)

class AdminViewModel(private val repository: FitnessRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getAllUsers(),
                repository.getAllSubscriptions(),
                repository.getAllClasses(),
                repository.getAllBookings()
            ) { users, subscriptions, classes, bookings ->
                val activeSubscriptions = subscriptions.count { subscription ->
                    subscription.endDate > System.currentTimeMillis() && !subscription.isFrozen 
                }
                
                // Remove duplicates by using distinctBy
                val distinctBookings = bookings.distinctBy { it.id }
                val distinctUsers = users.distinctBy { it.id }
                val distinctSubscriptions = subscriptions.distinctBy { it.id }
                val distinctClasses = classes.distinctBy { it.id }
                
                _uiState.value = _uiState.value.copy(
                    users = distinctUsers,
                    subscriptions = distinctSubscriptions,
                    groupClasses = distinctClasses,
                    bookings = distinctBookings,
                    statistics = Statistics(
                        totalUsers = distinctUsers.size,
                        activeSubscriptions = activeSubscriptions,
                        totalBookings = distinctBookings.size,
                        totalClasses = distinctClasses.size
                    )
                )
            }.collect {}
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            try {
                repository.deleteUser(user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                repository.updateUser(user)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    fun addClass(groupClass: GroupClass) {
        viewModelScope.launch {
            try {
                repository.insertClass(groupClass)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    fun updateClass(groupClass: GroupClass) {
        viewModelScope.launch {
            try {
                repository.updateClass(groupClass)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    fun deleteClass(groupClass: GroupClass) {
        viewModelScope.launch {
            try {
                repository.deleteClass(groupClass)
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

