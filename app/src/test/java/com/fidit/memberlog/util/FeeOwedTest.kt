package com.fidit.memberlog.util

import com.fidit.memberlog.model.FeePayment
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class FeeOwedTest {

    private fun pay(period: String, amount: Double) =
        FeePayment(memberId = 1, periodMonth = period, amount = amount, paidDate = "$period-05")

    @Test
    fun totalOwedAndOwedMonthsCount_areCorrect() {
        val now = YearMonth.of(2026, 6)
        val payments = listOf(pay("2026-01", 10.0), pay("2026-02", 4.0))
        val s = FeeCalculator.computeStatuses("2026-01-01", 10.0, payments, now)
        assertEquals(46.0, FeeCalculator.totalOwed(s), 0.001)
        assertEquals(5, FeeCalculator.owedMonthsCount(s))
    }
}
