package com.example.myapplication.data.dao

import androidx.room.*
import com.example.myapplication.data.model.Booking
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY date DESC")
    fun getUserBookings(userId: Long): Flow<List<Booking>>

    @Query("SELECT * FROM bookings ORDER BY date DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE classId = :classId AND date = :date")
    fun getBookingsForClass(classId: Long, date: Long): Flow<List<Booking>>

    @Query("SELECT COUNT(*) FROM bookings WHERE classId = :classId AND date = :date")
    suspend fun getBookingCount(classId: Long, date: Long): Int

    @Query("SELECT * FROM bookings WHERE userId = :userId AND classId = :classId AND date = :date LIMIT 1")
    suspend fun getUserBookingForClass(userId: Long, classId: Long, date: Long): Booking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Delete
    suspend fun deleteBooking(booking: Booking)

    @Query("DELETE FROM bookings WHERE userId = :userId AND classId = :classId AND date = :date")
    suspend fun cancelBooking(userId: Long, classId: Long, date: Long)
}

