package com.fidit.memberlog.util

import com.fidit.memberlog.model.FeeRate
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class FeeHistoryTest {

    @Test
    fun feeChange_appliesFromEffectiveMonthOnly_notRetroactively() {
        val rates = listOf(
            FeeRate(memberId = null, effectiveFrom = "1970-01", amount = 10.0),
            FeeRate(memberId = null, effectiveFrom = "2026-04", amount = 20.0),
            FeeRate(memberId = 1, effectiveFrom = "2026-05", amount = 5.0),
            FeeRate(memberId = 1, effectiveFrom = "2026-06", amount = null)
        )

        assertEquals(10.0, FeeCalculator.effectiveFee(1, "2026-03", rates, 10.0), 0.001)
        assertEquals(20.0, FeeCalculator.effectiveFee(1, "2026-04", rates, 10.0), 0.001)
        assertEquals(5.0, FeeCalculator.effectiveFee(1, "2026-05", rates, 10.0), 0.001)
        assertEquals(20.0, FeeCalculator.effectiveFee(1, "2026-06", rates, 10.0), 0.001)

        val statuses = FeeCalculator.computeStatuses(
            "2026-03-01",
            { p -> FeeCalculator.effectiveFee(1, p, rates, 10.0) },
            emptyList(),
            YearMonth.of(2026, 4)
        )
        assertEquals(10.0, statuses.first { it.period == "2026-03" }.expected, 0.001)
        assertEquals(20.0, statuses.first { it.period == "2026-04" }.expected, 0.001)
    }
}
