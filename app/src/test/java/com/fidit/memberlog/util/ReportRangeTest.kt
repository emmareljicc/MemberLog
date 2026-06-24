package com.fidit.memberlog.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ReportRangeTest {

    @Test
    fun filterByRange_keepsOnlyRowsInRangeAndRecomputesCollected() {
        val data = ReportData(
            memberRows = emptyList(),
            paymentRows = listOf(
                PaymentRow("Ana", "2026-01", 10.0, "2026-01-05"),
                PaymentRow("Bruno", "2026-03", 5.0, "2026-03-10")
            ),
            eventRows = listOf(
                EventRow("Sastanak", "2026-01-10", 2),
                EventRow("Izlet", "2026-03-20", 1)
            ),
            totals = ReportTotals(members = 0, collected = 15.0, outstanding = 0.0, paidThisMonth = 0)
        )

        val filtered = ReportBuilder.filterByRange(data, "2026-03-01", "2026-03-31")

        assertEquals(1, filtered.paymentRows.size)
        assertEquals("Bruno", filtered.paymentRows.first().memberName)
        assertEquals(1, filtered.eventRows.size)
        assertEquals(5.0, filtered.totals.collected, 0.001)
    }
}
