package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.repository.FitnessRepository
import com.example.myapplication.ui.screen.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var currentUser by remember { mutableStateOf<com.example.myapplication.data.model.User?>(null) }
            
            // Инициализация базы данных и репозитория
            val database = remember { (application as FitnessApplication).database }
            val repository = remember {
                FitnessRepository(
                    database.userDao(),
                    database.subscriptionDao(),
                    database.groupClassDao(),
                    database.bookingDao()
                )
            }
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FitnessApp(
                        repository = repository,
                        currentUser = currentUser,
                        onUserChanged = { currentUser = it },
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }
}

@Composable
fun FitnessApp(
    repository: FitnessRepository,
    currentUser: com.example.myapplication.data.model.User?,
    onUserChanged: (com.example.myapplication.data.model.User?) -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    if (currentUser == null) {
        val context = LocalContext.current
        // Create new AuthViewModel each time we return to login screen
        val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(repository))
        val authState by authViewModel.uiState.collectAsState()
        var showRegister by remember { mutableStateOf(false) }
        
        LaunchedEffect(authState.isAuthenticated) {
            if (authState.isAuthenticated) {
                authState.currentUser?.let {
                    onUserChanged(it)
                }
            }
        }
        
        if (showRegister) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    authState.currentUser?.let {
                        onUserChanged(it)
                    }
                },
                onBackToLogin = { showRegister = false }
            )
        } else {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    authState.currentUser?.let {
                        onUserChanged(it)
                    }
                },
                onRegisterClick = { showRegister = true }
            )
        }
    } else {
        when (currentUser.role) {
            com.example.myapplication.data.model.UserRole.USER -> {
                val userViewModel: UserViewModel = viewModel(
                    factory = UserViewModelFactory(repository, currentUser.id)
                )
                
                UserScreen(
                    viewModel = userViewModel,
                    onLogout = { 
                        userViewModel.logout()
                        onUserChanged(null) 
                    },
                    onThemeToggle = onThemeToggle,
                    isDarkTheme = isDarkTheme
                )
            }
            com.example.myapplication.data.model.UserRole.EMPLOYEE -> {
                val employeeViewModel: EmployeeViewModel = viewModel(
                    factory = EmployeeViewModelFactory(repository)
                )
                
                EmployeeScreen(
                    viewModel = employeeViewModel,
                    onLogout = { 
                        employeeViewModel.logout()
                        onUserChanged(null) 
                    },
                    onThemeToggle = onThemeToggle,
                    isDarkTheme = isDarkTheme
                )
            }
            com.example.myapplication.data.model.UserRole.ADMIN -> {
                val adminViewModel: AdminViewModel = viewModel(
                    factory = AdminViewModelFactory(repository)
                )
                
                AdminScreen(
                    viewModel = adminViewModel,
                    onLogout = { 
                        adminViewModel.logout()
                        onUserChanged(null) 
                    },
                    onThemeToggle = onThemeToggle,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

// ViewModel Factories
class AuthViewModelFactory(private val repository: FitnessRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class UserViewModelFactory(
    private val repository: FitnessRepository,
    private val userId: Long
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class EmployeeViewModelFactory(private val repository: FitnessRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmployeeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmployeeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AdminViewModelFactory(private val repository: FitnessRepository) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
