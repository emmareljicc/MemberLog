package com.fidit.memberlog.util

import com.fidit.memberlog.model.FeePayment
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class FeeCalculatorTest {

    private val now = YearMonth.of(2026, 6)

    private fun pay(period: String, amount: Double) =
        FeePayment(memberId = 1, periodMonth = period, amount = amount, paidDate = "$period-05")

    @Test
    fun monthsFrom_inclusiveCount() {

        assertEquals(6, FeeCalculator.monthsFrom("2026-01-01", now).size)
        assertEquals("2026-01", FeeCalculator.monthsFrom("2026-01-10", now).first())
        assertEquals("2026-06", FeeCalculator.monthsFrom("2026-01-10", now).last())
    }

    @Test
    fun fullPayment_isPaid_partial_isPartial_none_isUnpaid() {
        val payments = listOf(pay("2026-01", 10.0), pay("2026-02", 4.0))
        val s = FeeCalculator.computeStatuses("2026-01-01", 10.0, payments, now)
        assertEquals(MonthFeeStatus.PAID, s.first { it.period == "2026-01" }.status)
        assertEquals(MonthFeeStatus.PARTIAL, s.first { it.period == "2026-02" }.status)
        assertEquals(MonthFeeStatus.UNPAID, s.first { it.period == "2026-03" }.status)
    }

    @Test
    fun splitPaymentsSummed_toFullMonth_arePaid() {
        val payments = listOf(pay("2026-01", 6.0), pay("2026-01", 4.0))
        val s = FeeCalculator.computeStatuses("2026-01-01", 10.0, payments, now)
        assertEquals(MonthFeeStatus.PAID, s.first { it.period == "2026-01" }.status)
    }

    @Test
    fun totalOwed_and_owedCount_areCorrect() {

        val payments = listOf(pay("2026-01", 10.0), pay("2026-02", 4.0))
        val s = FeeCalculator.computeStatuses("2026-01-01", 10.0, payments, now)
        assertEquals(46.0, FeeCalculator.totalOwed(s), 0.001)
        assertEquals(5, FeeCalculator.owedMonthsCount(s))
    }

    @Test
    fun monthlyFeeFor_prefersOverride() {
        assertEquals(15.0, FeeCalculator.monthlyFeeFor(15.0, 10.0), 0.001)
        assertEquals(10.0, FeeCalculator.monthlyFeeFor(null, 10.0), 0.001)
    }
}
