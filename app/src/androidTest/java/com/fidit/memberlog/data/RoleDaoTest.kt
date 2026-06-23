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
class RoleDaoTest {

    private lateinit var db: MemberDatabase
    private lateinit var roleDao: RoleDao
    private lateinit var memberDao: MemberDao

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MemberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        roleDao = db.roleDao()
        memberDao = db.memberDao()
    }

    @After
    fun tearDown() = db.close()

    private suspend fun addRole(name: String): Int {
        roleDao.insert(Role(name = name, colorHex = "#6750A4"))
        return roleDao.getAll().first().first { it.name == name }.id
    }

    private suspend fun addMember(name: String, roleId: Int) {
        memberDao.insert(Member(name = name, roleId = roleId, joinDate = "2025-01-01", email = "$name@t.com", phone = "1"))
    }

    @Test
    fun insertUpdateDelete_roles() = runBlocking {
        val id = addRole("Tajnik")
        var role = roleDao.getAll().first().first { it.id == id }
        roleDao.update(role.copy(name = "Voditelj", colorHex = "#1E88E5"))
        role = roleDao.getAll().first().first { it.id == id }
        assertEquals("Voditelj", role.name)
        roleDao.delete(role)
        assertTrue(roleDao.getAll().first().none { it.id == id })
    }

    @Test
    fun countMembersWithRole_isCorrect() = runBlocking {
        val r = addRole("Član")
        addMember("Ana", r)
        addMember("Marko", r)
        assertEquals(2, roleDao.countMembersWithRole(r))
    }

    @Test
    fun reassignMembers_movesThemAndAllowsDelete() = runBlocking {
        val from = addRole("Stara")
        val to = addRole("Nova")
        addMember("Ana", from)
        addMember("Marko", from)

        roleDao.reassignMembers(from, to)

        assertEquals(0, roleDao.countMembersWithRole(from))
        assertEquals(2, roleDao.countMembersWithRole(to))
        roleDao.delete(roleDao.getAll().first().first { it.id == from })
        assertTrue(roleDao.getAll().first().none { it.id == from })
    }
}
