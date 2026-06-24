package com.fidit.memberlog.util

import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.FeeRate
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToLong

enum class MonthFeeStatus { PAID, PARTIAL, UNPAID, FUTURE }

data class MonthStatus(
    val period: String,
    val expected: Double,
    val paid: Double,
    val status: MonthFeeStatus
)

object FeeCalculator {

    private fun cents(v: Double): Long = (v * 100).roundToLong()

    fun activeMemberRate(memberId: Int, period: String, rates: List<FeeRate>): FeeRate? =
        rates.filter { it.memberId == memberId && it.effectiveFrom <= period }
            .maxByOrNull { it.effectiveFrom }

    fun effectiveFee(memberId: Int, period: String, rates: List<FeeRate>, fallback: Double): Double {
        val memberRate = activeMemberRate(memberId, period, rates)
        if (memberRate?.amount != null) return memberRate.amount
        val globalRate = rates.filter { it.memberId == null && it.effectiveFrom <= period }
            .maxByOrNull { it.effectiveFrom }
        return globalRate?.amount ?: fallback
    }

    fun isOverridden(memberId: Int, period: String, rates: List<FeeRate>): Boolean =
        activeMemberRate(memberId, period, rates)?.amount != null

    fun monthsFrom(joinIso: String, now: YearMonth = YearMonth.now()): List<String> {
        val start = parseStart(joinIso) ?: return emptyList()
        if (start.isAfter(now)) return listOf(fmt(start))
        val list = mutableListOf<String>()
        var ym = start
        while (!ym.isAfter(now)) {
            list.add(fmt(ym))
            ym = ym.plusMonths(1)
        }
        return list
    }

    fun computeStatuses(
        joinIso: String,
        monthlyFee: Double,
        payments: List<FeePayment>,
        now: YearMonth = YearMonth.now()
    ): List<MonthStatus> = computeStatuses(joinIso, { monthlyFee }, payments, now)

    fun computeStatuses(
        joinIso: String,
        feeForPeriod: (String) -> Double,
        payments: List<FeePayment>,
        now: YearMonth = YearMonth.now()
    ): List<MonthStatus> {
        val paidByPeriod = payments.groupBy { it.periodMonth }
            .mapValues { (_, ps) -> ps.sumOf { it.amount } }
        val nowStr = fmt(now)
        return monthsFrom(joinIso, now).map { period ->
            val monthlyFee = feeForPeriod(period)
            val paid = paidByPeriod[period] ?: 0.0
            val status = when {
                period > nowStr -> MonthFeeStatus.FUTURE
                cents(monthlyFee) > 0 && cents(paid) >= cents(monthlyFee) -> MonthFeeStatus.PAID
                cents(paid) <= 0 -> MonthFeeStatus.UNPAID
                else -> MonthFeeStatus.PARTIAL
            }
            MonthStatus(period, monthlyFee, paid, status)
        }
    }

    fun totalOwed(statuses: List<MonthStatus>): Double =
        statuses.filter { it.status != MonthFeeStatus.FUTURE }
            .sumOf { (it.expected - it.paid).coerceAtLeast(0.0) }

    fun owedMonthsCount(statuses: List<MonthStatus>): Int =
        statuses.count { it.status == MonthFeeStatus.UNPAID || it.status == MonthFeeStatus.PARTIAL }

    fun oldestOutstanding(statuses: List<MonthStatus>): MonthStatus? =
        statuses.firstOrNull { it.status == MonthFeeStatus.UNPAID || it.status == MonthFeeStatus.PARTIAL }

    private fun parseStart(joinIso: String): YearMonth? =
        try {
            YearMonth.from(LocalDate.parse(joinIso))
        } catch (e: Exception) {
            try { YearMonth.parse(joinIso) } catch (e2: Exception) { null }
        }

    private fun fmt(ym: YearMonth): String = "%04d-%02d".format(ym.year, ym.monthValue)
}
