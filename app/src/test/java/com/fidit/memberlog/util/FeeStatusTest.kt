package com.fidit.memberlog.util

import com.fidit.memberlog.model.FeePayment
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class FeeStatusTest {

    private fun pay(period: String, amount: Double) =
        FeePayment(memberId = 1, periodMonth = period, amount = amount, paidDate = "$period-05")

    @Test
    fun computeStatuses_marksPaidPartialAndUnpaid() {
        val now = YearMonth.of(2026, 6)
        val payments = listOf(pay("2026-01", 10.0), pay("2026-02", 4.0))
        val s = FeeCalculator.computeStatuses("2026-01-01", 10.0, payments, now)
        assertEquals(MonthFeeStatus.PAID, s.first { it.period == "2026-01" }.status)
        assertEquals(MonthFeeStatus.PARTIAL, s.first { it.period == "2026-02" }.status)
        assertEquals(MonthFeeStatus.UNPAID, s.first { it.period == "2026-03" }.status)
    }
}
