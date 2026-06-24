package com.fidit.memberlog

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fidit.memberlog.data.AuthRepository
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.data.MemberRepository
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.util.PasswordHash
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthMediumTest {

    private lateinit var db: MemberDatabase
    private lateinit var members: MemberRepository
    private lateinit var auth: AuthRepository

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MemberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        members = MemberRepository(db.memberDao())
        auth = AuthRepository(db.memberDao(), db.roleDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun login_byRoleAndPassword_resolvesAdminAndRejectsBadCredentials() = runBlocking {
        db.roleDao().insert(Role(name = "Blagajnik", colorHex = "#2E9E6B", grantsAdmin = true))
        val roleId = db.roleDao().getAll().first().first().id
        members.insert(
            Member(
                name = "Ana Blagajnik",
                roleId = roleId,
                joinDate = "2025-01-01",
                email = "blagajnik@test.com",
                phone = "099 111-2222",
                passwordHash = PasswordHash.sha256("tajna")
            )
        )

        assertEquals(true, auth.login("blagajnik@test.com", "tajna")?.isAdmin)
        assertNull(auth.login("blagajnik@test.com", "kriva"))
        assertNull(auth.login("nepostojeci@test.com", "tajna"))
    }
}
