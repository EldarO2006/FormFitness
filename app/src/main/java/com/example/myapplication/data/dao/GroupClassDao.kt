package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.GroupClass
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupClassDao {
    @Query("SELECT * FROM group_classes ORDER BY dayOfWeek, time")
    fun getAllClasses(): Flow<List<GroupClass>>

    @Query("SELECT * FROM group_classes WHERE dayOfWeek = :dayOfWeek ORDER BY time")
    fun getClassesByDay(dayOfWeek: Int): Flow<List<GroupClass>>

    @Query("SELECT * FROM group_classes WHERE id = :id")
    fun getClassById(id: Long): Flow<GroupClass?>
    
    @Query("SELECT * FROM group_classes WHERE id = :id LIMIT 1")
    suspend fun getClassByIdSync(id: Long): GroupClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(groupClass: GroupClass): Long

    @Update
    suspend fun updateClass(groupClass: GroupClass)

    @Delete
    suspend fun deleteClass(groupClass: GroupClass)
}

