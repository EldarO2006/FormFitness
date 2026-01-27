package com.example.myapplication.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.*
import com.example.myapplication.data.model.*

@Database(
    entities = [User::class, Subscription::class, GroupClass::class, Booking::class],
    version = 1,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun groupClassDao(): GroupClassDao
    abstract fun bookingDao(): BookingDao
}

