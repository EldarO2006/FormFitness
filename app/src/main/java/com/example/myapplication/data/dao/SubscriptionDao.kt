package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Subscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId ORDER BY endDate DESC LIMIT 1")
    fun getActiveSubscription(userId: Long): Flow<Subscription?>

    @Query("SELECT * FROM subscriptions WHERE userId = :userId")
    fun getUserSubscriptions(userId: Long): Flow<List<Subscription>>

    @Query("SELECT * FROM subscriptions")
    fun getAllSubscriptions(): Flow<List<Subscription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription): Long

    @Update
    suspend fun updateSubscription(subscription: Subscription)

    @Delete
    suspend fun deleteSubscription(subscription: Subscription)
}

