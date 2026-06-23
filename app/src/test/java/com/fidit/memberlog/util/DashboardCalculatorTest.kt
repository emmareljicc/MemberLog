package com.fidit.memberlog.util

import com.fidit.memberlog.model.Event
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.Member
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class DashboardCalculatorTest {

    private val now = YearMonth.of(2026, 6)

    private fun member(id: Int, name: String, joinIso: String) =
        Member(id = id, name = name, roleId = 1, joinDate = joinIso, email = "$name@t.com", phone = "1")

    @Test
    fun computesAggregatesCorrectly() {
        val members = listOf(
            member(1, "Ana", "2026-01-01"),
            member(2, "Bruno", "2026-03-01")
        )
        val payments = listOf(
            FeePayment(memberId = 1, periodMonth = "2026-06", amount = 10.0, paidDate = "2026-06-05")
        )

        val stats = DashboardCalculator.compute(members, payments, defaultMonthlyFee = 10.0, now = now)

        assertEquals(2, stats.totalMembers)
        assertEquals(1, stats.paidThisMonth)
        assertEquals(10.0, stats.collectedTotal, 0.001)
        assertEquals(90.0, stats.outstandingTotal, 0.001)
        assertEquals("Ana", stats.topDebtors.first().first.name)
        assertEquals(50.0, stats.topDebtors.first().second, 0.001)
        assertEquals(listOf("2026-01" to 1, "2026-03" to 2), stats.growth)
        assertEquals(1, stats.recentPayments.size)
        assertEquals("Ana", stats.recentPayments.first().memberName)
    }

    @Test
    fun upcomingEvents_areFutureSortedAscending() {
        val events = listOf(
            Event(id = 1, title = "Prošli", date = "2026-05-01", location = "", notes = ""),
            Event(id = 2, title = "Kasnije", date = "2026-07-10", location = "", notes = ""),
            Event(id = 3, title = "Skoro", date = "2026-06-25", location = "", notes = "")
        )
        val stats = DashboardCalculator.compute(
            members = emptyList(),
            payments = emptyList(),
            defaultMonthlyFee = 10.0,
            events = events,
            today = "2026-06-23",
            now = now
        )
        assertEquals(listOf("Skoro", "Kasnije"), stats.upcomingEvents.map { it.title })
    }
}
