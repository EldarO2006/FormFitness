package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.GroupClass
import com.example.myapplication.data.model.Subscription
import com.example.myapplication.data.model.SubscriptionType
import com.example.myapplication.data.model.User
import com.example.myapplication.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

data class EmployeeUiState(
    val groupClasses: List<GroupClass> = emptyList(),
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class EmployeeViewModel(val repository: FitnessRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(EmployeeUiState())
    val uiState: StateFlow<EmployeeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllClasses().collect { classes ->
                _uiState.value = _uiState.value.copy(groupClasses = classes)
            }
        }
        
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _uiState.value = _uiState.value.copy(users = users.filter { it.role == com.example.myapplication.data.model.UserRole.USER })
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

    fun assignSubscription(userId: Long, type: SubscriptionType) {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val monthsInMillis = type.months * 30L * 24 * 60 * 60 * 1000
                val subscription = Subscription(
                    userId = userId,
                    type = type,
                    startDate = now,
                    endDate = now + monthsInMillis
                )
                repository.insertSubscription(subscription)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Ошибка: ${e.message}")
            }
        }
    }

    suspend fun getUserSubscription(userId: Long): com.example.myapplication.data.model.Subscription? {
        return repository.getActiveSubscription(userId).firstOrNull()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun logout() {
        // Clear state if needed
    }
}

