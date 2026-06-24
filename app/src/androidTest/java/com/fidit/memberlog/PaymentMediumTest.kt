package com.fidit.memberlog

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fidit.memberlog.data.FeeRepository
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.data.MemberRepository
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.util.DashboardCalculator
import com.fidit.memberlog.util.FeeCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class PaymentMediumTest {

    private lateinit var db: MemberDatabase
    private lateinit var members: MemberRepository
    private lateinit var fees: FeeRepository

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MemberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        members = MemberRepository(db.memberDao())
        fees = FeeRepository(db.feeDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun recordedPayments_drivePartialAndOutstandingAcrossLayers() = runBlocking {
        val now = YearMonth.of(2025, 7)

        db.roleDao().insert(Role(name = "Član", colorHex = "#6750A4"))
        val roleId = db.roleDao().getAll().first().first().id
        members.insert(
            Member(name = "Ivana Test", roleId = roleId, joinDate = "2025-01-01", email = "ivana@test.com", phone = "099 333-4444")
        )
        val member = members.allMembers.first().first()
        fees.setDefaultFee(10.0)

        listOf(
            "2025-01" to 10.0,
            "2025-02" to 10.0,
            "2025-03" to 5.0,
            "2025-04" to 10.0,
            "2025-06" to 10.0,
            "2025-07" to 10.0
        ).forEach { (period, amount) ->
            fees.recordPayment(FeePayment(memberId = member.id, periodMonth = period, amount = amount, paidDate = "$period-05"))
        }

        val payments = fees.paymentsForMember(member.id).first()
        val statuses = FeeCalculator.computeStatuses(member.joinDate, 10.0, payments, now)
        assertEquals(15.0, FeeCalculator.totalOwed(statuses), 0.001)

        val stats = DashboardCalculator.compute(
            members = listOf(member),
            payments = payments,
            defaultMonthlyFee = 10.0,
            now = now
        )
        assertEquals(1, stats.totalMembers)
        assertEquals(1, stats.paidThisMonth)
        assertEquals(55.0, stats.collectedTotal, 0.001)
        assertEquals(15.0, stats.outstandingTotal, 0.001)
    }
}
