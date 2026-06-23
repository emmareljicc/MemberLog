package com.fidit.memberlog.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fidit.memberlog.model.Attendance
import com.fidit.memberlog.model.Event
import com.fidit.memberlog.model.EventRsvp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityDaoTest {

    private lateinit var db: MemberDatabase
    private lateinit var activityDao: ActivityDao
    private lateinit var memberDao: MemberDao
    private lateinit var roleDao: RoleDao

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MemberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        activityDao = db.activityDao()
        memberDao = db.memberDao()
        roleDao = db.roleDao()
    }

    @After
    fun tearDown() = db.close()

    private suspend fun seedMember(): Int {
        roleDao.insert(Role(name = "Član", colorHex = "#6750A4"))
        val roleId = roleDao.getAll().first().first().id
        memberDao.insert(Member(name = "Ana", roleId = roleId, joinDate = "2025-01-01", email = "a@t.com", phone = "1"))
        return memberDao.getAll().first().first().id
    }

    private suspend fun seedEvent(date: String): Int {
        activityDao.insertEvent(Event(title = "Događaj $date", date = date, location = "L", notes = ""))
        return activityDao.allEvents().first().first { it.date == date }.id
    }

    @Test
    fun insertEvent_thenAllEvents() = runBlocking {
        seedEvent("2025-06-01")
        assertEquals(1, activityDao.allEvents().first().size)
    }

    @Test
    fun attendance_addAndRemove() = runBlocking {
        val memberId = seedMember()
        val eventId = seedEvent("2025-06-01")
        activityDao.addAttendance(Attendance(eventId, memberId))
        assertEquals(listOf(memberId), activityDao.attendeeIds(eventId).first())
        activityDao.removeAttendance(eventId, memberId)
        assertTrue(activityDao.attendeeIds(eventId).first().isEmpty())
    }

    @Test
    fun attendedEvents_returnsEventsForMember() = runBlocking {
        val memberId = seedMember()
        val eventId = seedEvent("2025-06-01")
        activityDao.addAttendance(Attendance(eventId, memberId))
        val attended = activityDao.attendedEvents(memberId).first()
        assertEquals(1, attended.size)
        assertEquals(eventId, attended.first().id)
    }

    @Test
    fun rsvp_addAndRemove() = runBlocking {
        val memberId = seedMember()
        val eventId = seedEvent("2025-06-01")
        activityDao.addRsvp(EventRsvp(eventId, memberId))
        assertEquals(listOf(memberId), activityDao.rsvpMemberIds(eventId).first())
        assertEquals(listOf(eventId), activityDao.rsvpEventIds(memberId).first())
        activityDao.removeRsvp(eventId, memberId)
        assertTrue(activityDao.rsvpMemberIds(eventId).first().isEmpty())
    }

    @Test
    fun upcomingEvents_filtersAndSorts() = runBlocking {
        seedEvent("2025-01-01")
        seedEvent("2025-12-31")
        seedEvent("2025-09-15")
        val upcoming = activityDao.upcomingEvents("2025-06-01").first()
        assertEquals(listOf("2025-09-15", "2025-12-31"), upcoming.map { it.date })
    }
}
