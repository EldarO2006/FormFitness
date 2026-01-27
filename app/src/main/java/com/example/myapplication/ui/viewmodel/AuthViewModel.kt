package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.User
import com.example.myapplication.data.model.UserRole
import com.example.myapplication.data.repository.FitnessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(private val repository: FitnessRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        // Initialize with test users in background
        viewModelScope.launch(Dispatchers.IO) {
            try {
                initializeTestData()
                _isInitialized.value = true
            } catch (e: Exception) {
                // Even if initialization fails, mark as initialized to allow login attempts
                _isInitialized.value = true
            }
        }
    }

    private suspend fun initializeTestData() = withContext(Dispatchers.IO) {
        // Create test users if they don't exist
        val testUsers = listOf(
            User(login = "user", password = "user123", name = "Клиент", role = UserRole.USER),
            User(login = "staff", password = "staff123", name = "Сотрудник", role = UserRole.EMPLOYEE),
            User(login = "admin", password = "admin123", name = "Администратор", role = UserRole.ADMIN)
        )
        
        testUsers.forEach { newUser ->
            try {
                val existingUser = repository.getUserByLogin(newUser.login)
                if (existingUser != null) {
                    // Update existing user with new password and name
                    repository.updateUser(newUser.copy(id = existingUser.id))
                } else {
                    // Insert new user
                    repository.insertUser(newUser)
                }
            } catch (e: Exception) {
                // Handle any errors
            }
        }
        
        // Create test subscriptions for users
        try {
            val testUser = repository.getUserByLogin("user")
            if (testUser != null) {
                val existingSubscriptions = repository.getUserSubscriptions(testUser.id).firstOrNull()
                if (existingSubscriptions == null || existingSubscriptions.isEmpty()) {
                    val now = System.currentTimeMillis()
                    val oneMonthInMillis = 30L * 24 * 60 * 60 * 1000
                    val subscription = com.example.myapplication.data.model.Subscription(
                        userId = testUser.id,
                        type = com.example.myapplication.data.model.SubscriptionType.ONE_MONTH,
                        startDate = now,
                        endDate = now + oneMonthInMillis
                    )
                    repository.insertSubscription(subscription)
                }
            }
        } catch (e: Exception) {
            // Handle any errors
        }
        
        // Initialize test group classes (only if empty)
        val existingClasses = repository.getAllClasses().firstOrNull()
        if (existingClasses == null || existingClasses.isEmpty()) {
            val testClasses = listOf(
                com.example.myapplication.data.model.GroupClass(
                    name = "Йога",
                    description = "Расслабляющая практика для тела и ума",
                    dayOfWeek = java.util.Calendar.MONDAY,
                    time = "10:00",
                    maxParticipants = 8
                ),
                com.example.myapplication.data.model.GroupClass(
                    name = "Силовая тренировка",
                    description = "Интенсивная тренировка для развития силы",
                    dayOfWeek = java.util.Calendar.MONDAY,
                    time = "18:00",
                    maxParticipants = 8
                ),
                com.example.myapplication.data.model.GroupClass(
                    name = "Пилатес",
                    description = "Укрепление мышц кора и улучшение гибкости",
                    dayOfWeek = java.util.Calendar.WEDNESDAY,
                    time = "10:00",
                    maxParticipants = 8
                ),
                com.example.myapplication.data.model.GroupClass(
                    name = "Кардио",
                    description = "Высокоинтенсивная кардиотренировка",
                    dayOfWeek = java.util.Calendar.WEDNESDAY,
                    time = "18:00",
                    maxParticipants = 8
                ),
                com.example.myapplication.data.model.GroupClass(
                    name = "Стретчинг",
                    description = "Растяжка и улучшение гибкости",
                    dayOfWeek = java.util.Calendar.FRIDAY,
                    time = "10:00",
                    maxParticipants = 8
                ),
                com.example.myapplication.data.model.GroupClass(
                    name = "Функциональный тренинг",
                    description = "Тренировка для повседневной активности",
                    dayOfWeek = java.util.Calendar.FRIDAY,
                    time = "18:00",
                    maxParticipants = 8
                )
            )
            
            testClasses.forEach { groupClass ->
                try {
                    repository.insertClass(groupClass)
                } catch (e: Exception) {
                    // Class might already exist
                }
            }
        }
    }

    fun login(login: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Wait for initialization if not ready (max 5 seconds)
                var waitTime = 0
                while (!_isInitialized.value && waitTime < 5000) {
                    kotlinx.coroutines.delay(100)
                    waitTime += 100
                }
                
                // Small delay to ensure database is ready
                kotlinx.coroutines.delay(500)
                
                // Try to get user by login first to verify user exists
                val userByLogin = repository.getUserByLogin(login)
                if (userByLogin == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Неверный логин или пароль"
                    )
                    return@launch
                }
                
                val user = repository.login(login, password)
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        currentUser = user,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Неверный логин или пароль"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ошибка входа: ${e.message}"
                )
            }
        }
    }

    fun register(login: String, password: String, name: String, phone: String? = null, email: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Wait for initialization if not ready (max 5 seconds)
                var waitTime = 0
                while (!_isInitialized.value && waitTime < 5000) {
                    kotlinx.coroutines.delay(100)
                    waitTime += 100
                }
                
                // Small delay to ensure database is ready
                kotlinx.coroutines.delay(200)
                
                // Check if user already exists
                val existingUser = repository.getUserByLogin(login)
                if (existingUser != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Пользователь с таким логином уже существует"
                    )
                    return@launch
                }
                
                // Create new user
                val newUser = User(
                    login = login,
                    password = password,
                    name = name,
                    role = UserRole.USER,
                    phone = phone,
                    email = email
                )
                
                val userId = repository.insertUser(newUser)
                val createdUser = repository.getUserByLogin(login)
                
                if (createdUser != null) {
                    _uiState.value = _uiState.value.copy(
                        isAuthenticated = true,
                        currentUser = createdUser,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Ошибка регистрации"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ошибка регистрации: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        // Reset state completely
        _uiState.value = AuthUiState(
            isAuthenticated = false,
            currentUser = null,
            isLoading = false,
            errorMessage = null
        )
        _isInitialized.value = false
    }
}

