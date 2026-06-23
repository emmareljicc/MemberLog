package com.fidit.memberlog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fidit.memberlog.model.Attendance
import com.fidit.memberlog.model.Event
import com.fidit.memberlog.model.EventRsvp
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {

    @Query("SELECT * FROM events ORDER BY date DESC")
    fun allEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE date >= :todayIso ORDER BY date ASC")
    fun upcomingEvents(todayIso: String): Flow<List<Event>>

    @Insert
    suspend fun insertEvent(event: Event)

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("SELECT memberId FROM attendance WHERE eventId = :eventId")
    fun attendeeIds(eventId: Int): Flow<List<Int>>

    @Query("SELECT * FROM attendance")
    fun allAttendance(): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAttendance(attendance: Attendance)

    @Query("DELETE FROM attendance WHERE eventId = :eventId AND memberId = :memberId")
    suspend fun removeAttendance(eventId: Int, memberId: Int)

    @Query("SELECT * FROM events WHERE id IN (SELECT eventId FROM attendance WHERE memberId = :memberId) ORDER BY date DESC")
    fun attendedEvents(memberId: Int): Flow<List<Event>>

    @Query("SELECT eventId FROM event_rsvp WHERE memberId = :memberId")
    fun rsvpEventIds(memberId: Int): Flow<List<Int>>

    @Query("SELECT memberId FROM event_rsvp WHERE eventId = :eventId")
    fun rsvpMemberIds(eventId: Int): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRsvp(rsvp: EventRsvp)

    @Query("DELETE FROM event_rsvp WHERE eventId = :eventId AND memberId = :memberId")
    suspend fun removeRsvp(eventId: Int, memberId: Int)
}
