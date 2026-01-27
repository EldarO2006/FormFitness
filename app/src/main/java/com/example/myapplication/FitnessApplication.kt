package com.example.myapplication

import android.app.Application
import androidx.room.Room
import com.example.myapplication.data.database.FitnessDatabase

class FitnessApplication : Application() {
    val database: FitnessDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            FitnessDatabase::class.java,
            "fitness_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}

