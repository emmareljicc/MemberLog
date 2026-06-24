package com.fidit.memberlog.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class FeeMonthsTest {

    @Test
    fun monthsFrom_isInclusiveOfStartAndCurrentMonth() {
        val now = YearMonth.of(2026, 6)
        val months = FeeCalculator.monthsFrom("2026-01-10", now)
        assertEquals(6, months.size)
        assertEquals("2026-01", months.first())
        assertEquals("2026-06", months.last())
    }
}
