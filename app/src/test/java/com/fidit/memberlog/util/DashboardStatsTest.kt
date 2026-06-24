package com.fidit.memberlog.util

import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.Member
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class DashboardStatsTest {

    @Test
    fun compute_countsMembersCollectedAndOutstanding() {
        val members = listOf(
            Member(id = 1, name = "Ana", roleId = 1, joinDate = "2026-01-01", email = "ana@test.com", phone = "099")
        )
        val payments = listOf(
            FeePayment(memberId = 1, periodMonth = "2026-01", amount = 10.0, paidDate = "2026-01-05")
        )

        val stats = DashboardCalculator.compute(
            members = members,
            payments = payments,
            defaultMonthlyFee = 10.0,
            now = YearMonth.of(2026, 3)
        )

        assertEquals(1, stats.totalMembers)
        assertEquals(10.0, stats.collectedTotal, 0.001)
        assertEquals(20.0, stats.outstandingTotal, 0.001)
        assertEquals(1, stats.topDebtors.size)
    }
}
