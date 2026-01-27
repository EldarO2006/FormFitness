package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "group_classes")
data class GroupClass(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val dayOfWeek: Int, // Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY
    val time: String, // "10:00", "18:00"
    val maxParticipants: Int = 8,
    val trainerName: String? = null
)

enum class BookingStatus {
    AVAILABLE,
    BOOKED,
    FULL
}

