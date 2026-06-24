package com.fidit.memberlog

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fidit.memberlog.data.FeeRepository
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.model.FeeRate
import com.fidit.memberlog.util.FeeCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class FeeRateMediumTest {

    private lateinit var db: MemberDatabase
    private lateinit var fees: FeeRepository

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, MemberDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fees = FeeRepository(db.feeDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun changingDefaultFee_doesNotChangePastDues_onlyFutureMonths() = runBlocking {
        val join = "2024-01-01"
        db.feeDao().insertRate(FeeRate(memberId = null, effectiveFrom = "1970-01", amount = 10.0))

        val ratesBefore = fees.allRates.first()
        val owedBefore = FeeCalculator.totalOwed(
            FeeCalculator.computeStatuses(join, { p -> FeeCalculator.effectiveFee(1, p, ratesBefore, 10.0) }, emptyList())
        )
        assertTrue(owedBefore > 0.0)

        fees.setDefaultFee(20.0)
        val ratesAfter = fees.allRates.first()
        val owedAfter = FeeCalculator.totalOwed(
            FeeCalculator.computeStatuses(join, { p -> FeeCalculator.effectiveFee(1, p, ratesAfter, 10.0) }, emptyList())
        )

        assertEquals(owedBefore, owedAfter, 0.001)

        val next = YearMonth.now().plusMonths(1)
        val nextStr = "%04d-%02d".format(next.year, next.monthValue)
        assertEquals(20.0, FeeCalculator.effectiveFee(1, nextStr, ratesAfter, 10.0), 0.001)
    }
}
