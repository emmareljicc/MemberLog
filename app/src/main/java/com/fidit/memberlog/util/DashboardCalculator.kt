package com.fidit.memberlog.util

import com.fidit.memberlog.model.Event
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.FeeRate
import com.fidit.memberlog.model.Member
import java.time.LocalDate
import java.time.YearMonth

data class RecentPayment(
    val memberName: String,
    val amount: Double,
    val period: String,
    val paidDate: String
)

data class DashboardStats(
    val totalMembers: Int,
    val paidThisMonth: Int,
    val collectedTotal: Double,
    val outstandingTotal: Double,
    val growth: List<Pair<String, Int>>,
    val topDebtors: List<Pair<Member, Double>>,
    val recentPayments: List<RecentPayment>,
    val upcomingEvents: List<Event>
)

object DashboardCalculator {

    fun compute(
        members: List<Member>,
        payments: List<FeePayment>,
        defaultMonthlyFee: Double,
        rates: List<FeeRate> = emptyList(),
        events: List<Event> = emptyList(),
        today: String = LocalDate.now().toString(),
        now: YearMonth = YearMonth.now()
    ): DashboardStats {
        val paymentsByMember = payments.groupBy { it.memberId }
        val nowStr = "%04d-%02d".format(now.year, now.monthValue)

        var paidThisMonth = 0
        var outstanding = 0.0
        val debtors = mutableListOf<Pair<Member, Double>>()

        members.forEach { m ->
            val statuses = FeeCalculator.computeStatuses(
                m.joinDate,
                { p -> FeeCalculator.effectiveFee(m.id, p, rates, defaultMonthlyFee) },
                paymentsByMember[m.id] ?: emptyList(),
                now
            )
            val owed = FeeCalculator.totalOwed(statuses)
            outstanding += owed
            if (owed > 0.0) debtors.add(m to owed)
            val current = statuses.firstOrNull { it.period == nowStr }
            if (current != null && current.status == MonthFeeStatus.PAID) paidThisMonth++
        }

        val collectedTotal = payments.sumOf { it.amount }

        val joinMonths = members.mapNotNull { joinMonth(it.joinDate) }
        val growth = (0..11).map { i ->
            val ym = now.minusMonths((11 - i).toLong())
            val key = "%04d-%02d".format(ym.year, ym.monthValue)
            key to joinMonths.count { it <= key }
        }

        val recent = payments.sortedByDescending { it.paidDate }.take(6).map { p ->
            RecentPayment(
                memberName = members.firstOrNull { it.id == p.memberId }?.name ?: "?",
                amount = p.amount,
                period = p.periodMonth,
                paidDate = p.paidDate
            )
        }

        val upcoming = events.filter { it.date >= today }.sortedBy { it.date }

        return DashboardStats(
            totalMembers = members.size,
            paidThisMonth = paidThisMonth,
            collectedTotal = collectedTotal,
            outstandingTotal = outstanding,
            growth = growth,
            topDebtors = debtors.sortedByDescending { it.second },
            recentPayments = recent,
            upcomingEvents = upcoming
        )
    }

    private fun joinMonth(iso: String): String? = try {
        val d = LocalDate.parse(iso)
        "%04d-%02d".format(d.year, d.monthValue)
    } catch (e: Exception) {
        try { YearMonth.parse(iso).toString() } catch (e2: Exception) { null }
    }
}
