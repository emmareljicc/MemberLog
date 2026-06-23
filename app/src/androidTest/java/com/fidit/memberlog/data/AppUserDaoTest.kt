package com.fidit.memberlog.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fidit.memberlog.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppUserDaoTest {

    private lateinit var db: MemberDatabase
    private lateinit var repo: AppUserRepository

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MemberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = AppUserRepository(db.appUserDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun register_thenAuthenticate_returnsRole() = runBlocking {
        repo.register("admin", "tajna", UserRole.ADMIN)
        assertEquals(UserRole.ADMIN, repo.authenticate("admin", "tajna"))
    }

    @Test
    fun authenticate_wrongPassword_returnsNull() = runBlocking {
        repo.register("ana", "tajna", UserRole.VIEWER)
        assertNull(repo.authenticate("ana", "krivo"))
    }

    @Test
    fun authenticate_unknownUser_returnsNull() = runBlocking {
        assertNull(repo.authenticate("nepostoji", "x"))
    }

    @Test
    fun hasAnyUsers_reflectsState() = runBlocking {
        assertEquals(false, repo.hasAnyUsers())
        repo.register("admin", "tajna", UserRole.ADMIN)
        assertEquals(true, repo.hasAnyUsers())
        assertEquals(1, repo.adminCount())
    }
}
