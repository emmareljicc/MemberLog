package com.fidit.memberlog.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.Member
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeeDaoTest {

    private lateinit var db: MemberDatabase
    private lateinit var memberDao: MemberDao
    private lateinit var feeDao: FeeDao

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MemberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        memberDao = db.memberDao()
        feeDao = db.feeDao()
    }

    @After
    fun tearDown() = db.close()

    private suspend fun seedMember(): Int {
        memberDao.insert(
            Member(name = "Test", role = "Član", joinDate = "2025-01-01", email = "t@t.com", phone = "1")
        )
        return memberDao.getAll().first().first().id
    }

    @Test
    fun insertPayment_thenQueryByMember() = runBlocking {
        val id = seedMember()
        feeDao.insertPayment(FeePayment(memberId = id, periodMonth = "2025-01", amount = 10.0, paidDate = "2025-01-05"))
        feeDao.insertPayment(FeePayment(memberId = id, periodMonth = "2025-02", amount = 5.0, paidDate = "2025-02-05"))
        val payments = feeDao.paymentsForMember(id).first()
        assertEquals(2, payments.size)
        assertEquals(15.0, payments.sumOf { it.amount }, 0.001)
    }

    @Test
    fun deletePayment_removesIt() = runBlocking {
        val id = seedMember()
        feeDao.insertPayment(FeePayment(memberId = id, periodMonth = "2025-01", amount = 10.0, paidDate = "2025-01-05"))
        val payment = feeDao.paymentsForMember(id).first().first()
        feeDao.deletePayment(payment)
        assertTrue(feeDao.paymentsForMember(id).first().isEmpty())
    }

    @Test
    fun upsertConfig_replacesSingleRow() = runBlocking {
        feeDao.upsertConfig(FeeConfig(id = 1, defaultMonthlyFee = 10.0))
        feeDao.upsertConfig(FeeConfig(id = 1, defaultMonthlyFee = 12.5))
        assertEquals(12.5, feeDao.getConfig().first()!!.defaultMonthlyFee, 0.001)
    }
}
