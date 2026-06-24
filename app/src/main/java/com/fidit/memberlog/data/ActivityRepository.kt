package com.fidit.memberlog.data

import com.fidit.memberlog.model.Attendance
import com.fidit.memberlog.model.Event
import com.fidit.memberlog.model.EventRsvp
import kotlinx.coroutines.flow.Flow

class ActivityRepository(private val dao: ActivityDao) {

    val allEvents: Flow<List<Event>> = dao.allEvents()

    fun upcomingEvents(todayIso: String): Flow<List<Event>> = dao.upcomingEvents(todayIso)

    fun attendeeIds(eventId: Int): Flow<List<Int>> = dao.attendeeIds(eventId)

    fun attendedEvents(memberId: Int): Flow<List<Event>> = dao.attendedEvents(memberId)

    suspend fun addEvent(event: Event) = dao.insertEvent(event)

    suspend fun updateEvent(event: Event) = dao.updateEvent(event)

    suspend fun deleteEvent(event: Event) = dao.deleteEvent(event)

    suspend fun setAttendance(eventId: Int, memberId: Int, present: Boolean) {
        if (present) dao.addAttendance(Attendance(eventId, memberId))
        else dao.removeAttendance(eventId, memberId)
    }

    fun rsvpEventIds(memberId: Int): Flow<List<Int>> = dao.rsvpEventIds(memberId)

    fun rsvpMemberIds(eventId: Int): Flow<List<Int>> = dao.rsvpMemberIds(eventId)

    suspend fun setRsvp(eventId: Int, memberId: Int, coming: Boolean) {
        if (coming) {
            dao.addRsvp(EventRsvp(eventId, memberId))
            dao.addAttendance(Attendance(eventId, memberId))
        } else {
            dao.removeRsvp(eventId, memberId)
            dao.removeAttendance(eventId, memberId)
        }
    }
}
