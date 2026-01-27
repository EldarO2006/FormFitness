package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.*
import com.example.myapplication.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class FitnessRepository(
    private val userDao: UserDao,
    private val subscriptionDao: SubscriptionDao,
    private val groupClassDao: GroupClassDao,
    private val bookingDao: BookingDao
) {
    // User operations
    suspend fun login(login: String, password: String): User? = userDao.login(login, password)
    suspend fun getUserByLogin(login: String): User? = userDao.getUserByLogin(login)
    fun getUserById(id: Long): Flow<User?> = userDao.getUserById(id)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    // Subscription operations
    fun getActiveSubscription(userId: Long): Flow<Subscription?> = subscriptionDao.getActiveSubscription(userId)
    fun getUserSubscriptions(userId: Long): Flow<List<Subscription>> = subscriptionDao.getUserSubscriptions(userId)
    fun getAllSubscriptions(): Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()
    suspend fun insertSubscription(subscription: Subscription): Long = subscriptionDao.insertSubscription(subscription)
    suspend fun updateSubscription(subscription: Subscription) = subscriptionDao.updateSubscription(subscription)

    // Group class operations
    fun getAllClasses(): Flow<List<GroupClass>> = groupClassDao.getAllClasses()
    fun getClassesByDay(dayOfWeek: Int): Flow<List<GroupClass>> = groupClassDao.getClassesByDay(dayOfWeek)
    suspend fun insertClass(groupClass: GroupClass): Long = groupClassDao.insertClass(groupClass)
    suspend fun updateClass(groupClass: GroupClass) = groupClassDao.updateClass(groupClass)
    suspend fun deleteClass(groupClass: GroupClass) = groupClassDao.deleteClass(groupClass)

    // Booking operations
    fun getUserBookings(userId: Long): Flow<List<Booking>> = bookingDao.getUserBookings(userId)
    fun getAllBookings(): Flow<List<Booking>> = bookingDao.getAllBookings()
    fun getBookingsForClass(classId: Long, date: Long): Flow<List<Booking>> = bookingDao.getBookingsForClass(classId, date)
    suspend fun getBookingCount(classId: Long, date: Long): Int = bookingDao.getBookingCount(classId, date)
    suspend fun getUserBookingForClass(userId: Long, classId: Long, date: Long): Booking? = 
        bookingDao.getUserBookingForClass(userId, classId, date)
    suspend fun insertBooking(booking: Booking): Long = bookingDao.insertBooking(booking)
    suspend fun cancelBooking(userId: Long, classId: Long, date: Long) = bookingDao.cancelBooking(userId, classId, date)
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)

    // Helper functions
    suspend fun getBookingStatus(classId: Long, date: Long, userId: Long): BookingStatus {
        val bookingCount = getBookingCount(classId, date)
        val userBooking = getUserBookingForClass(userId, classId, date)
        val groupClass = groupClassDao.getClassByIdSync(classId)
        val maxParticipants = groupClass?.maxParticipants ?: 8
        
        return when {
            userBooking != null -> BookingStatus.BOOKED
            bookingCount >= maxParticipants -> BookingStatus.FULL
            else -> BookingStatus.AVAILABLE
        }
    }

    fun getRemainingDays(userId: Long): Flow<Int> {
        return subscriptionDao.getActiveSubscription(userId).map { subscription ->
            if (subscription == null) {
                0
            } else {
                val now = System.currentTimeMillis()
                val endDate = if (subscription.isFrozen && subscription.freezeEndDate != null) {
                    subscription.freezeEndDate
                } else {
                    subscription.endDate
                }
                val diff = endDate - now
                maxOf(0, (diff / (1000 * 60 * 60 * 24)).toInt())
            }
        }
    }
}

