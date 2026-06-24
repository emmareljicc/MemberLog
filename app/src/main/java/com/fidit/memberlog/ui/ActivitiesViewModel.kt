package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.ActivityRepository
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ActivitiesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ActivityRepository(MemberDatabase.getInstance(app).activityDao())

    val events: StateFlow<List<Event>?> = repo.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun attendeeIds(eventId: Int): Flow<List<Int>> = repo.attendeeIds(eventId)

    fun attendedEvents(memberId: Int): Flow<List<Event>> = repo.attendedEvents(memberId)

    fun rsvpEventIds(memberId: Int): Flow<List<Int>> = repo.rsvpEventIds(memberId)

    fun rsvpMemberIds(eventId: Int): Flow<List<Int>> = repo.rsvpMemberIds(eventId)

    fun setRsvp(eventId: Int, memberId: Int, coming: Boolean) {
        viewModelScope.launch { repo.setRsvp(eventId, memberId, coming) }
    }

    fun addEvent(title: String, date: String, location: String, notes: String) {
        viewModelScope.launch { repo.addEvent(Event(title = title, date = date, location = location, notes = notes)) }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch { repo.updateEvent(event) }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch { repo.deleteEvent(event) }
    }

    fun setAttendance(eventId: Int, memberId: Int, present: Boolean) {
        viewModelScope.launch { repo.setAttendance(eventId, memberId, present) }
    }
}
