package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val type: SubscriptionType,
    val startDate: Long, // timestamp
    val endDate: Long, // timestamp
    val isFrozen: Boolean = false,
    val freezeStartDate: Long? = null,
    val freezeEndDate: Long? = null,
    val freezeUsedThisMonth: Boolean = false
)

enum class SubscriptionType(val months: Int) {
    ONE_MONTH(1),
    SIX_MONTHS(6),
    TWELVE_MONTHS(12)
}

