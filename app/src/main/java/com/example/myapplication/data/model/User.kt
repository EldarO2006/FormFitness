package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val login: String,
    val password: String,
    val name: String,
    val role: UserRole,
    val phone: String? = null,
    val email: String? = null
)

enum class UserRole {
    USER,
    EMPLOYEE,
    ADMIN
}

