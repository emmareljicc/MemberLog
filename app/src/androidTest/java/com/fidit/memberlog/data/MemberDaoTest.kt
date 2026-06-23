package com.fidit.memberlog.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
class MemberDaoTest {

    private lateinit var db: MemberDatabase
    private lateinit var dao: MemberDao
    private var roleId = 0

    private fun member(name: String) = Member(
        name = name, roleId = roleId, joinDate = "2024-01-01",
        email = "$name@test.com", phone = "091/000-0000"
    )

    @Before
    fun setUp() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MemberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.memberDao()
        db.roleDao().insert(Role(name = "Član", colorHex = "#6750A4"))
        roleId = db.roleDao().getAll().first().first().id
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insert_thenGetAll_returnsInsertedMemberWithGeneratedId() = runBlocking {
        dao.insert(member("Ivo"))
        val all = dao.getAll().first()
        assertEquals(1, all.size)
        assertEquals("Ivo", all[0].name)
        assertTrue("autoGenerate should assign a non-zero id", all[0].id != 0)
    }

    @Test
    fun update_changesPersistedFields() = runBlocking {
        dao.insert(member("Ivo"))
        val stored = dao.getAll().first().first()
        dao.update(stored.copy(monthlyFeeOverride = 15.0))
        val updated = dao.getAll().first().first()
        assertEquals(15.0, updated.monthlyFeeOverride!!, 0.001)
    }

    @Test
    fun delete_removesMember() = runBlocking {
        dao.insert(member("Ivo"))
        val stored = dao.getAll().first().first()
        dao.delete(stored)
        assertTrue(dao.getAll().first().isEmpty())
    }

    @Test
    fun getAll_ordersByName() = runBlocking {
        dao.insert(member("Marko"))
        dao.insert(member("Ana"))
        val names = dao.getAll().first().map { it.name }
        assertEquals(listOf("Ana", "Marko"), names)
    }
}
