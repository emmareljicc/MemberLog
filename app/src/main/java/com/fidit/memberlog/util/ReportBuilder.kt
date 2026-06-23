package com.fidit.memberlog.util

import com.fidit.memberlog.model.Attendance
import com.fidit.memberlog.model.Event
import com.fidit.memberlog.model.FeePayment
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import java.time.LocalDate
import java.time.YearMonth

data class MemberRow(
    val name: String,
    val role: String,
    val joinDate: String,
    val email: String,
    val phone: String,
    val owed: Double
)

data class PaymentRow(
    val memberName: String,
    val period: String,
    val amount: Double,
    val paidDate: String
)

data class EventRow(
    val title: String,
    val date: String,
    val attendees: Int
)

data class ReportTotals(
    val members: Int,
    val collected: Double,
    val outstanding: Double,
    val paidThisMonth: Int
)

data class ReportData(
    val memberRows: List<MemberRow>,
    val paymentRows: List<PaymentRow>,
    val eventRows: List<EventRow>,
    val totals: ReportTotals
)

object ReportBuilder {

    fun buildReportData(
        members: List<Member>,
        rolesById: Map<Int, Role>,
        payments: List<FeePayment>,
        events: List<Event>,
        attendance: List<Attendance>,
        defaultMonthlyFee: Double,
        now: YearMonth = YearMonth.now(),
        today: String = LocalDate.now().toString()
    ): ReportData {
        val paymentsByMember = payments.groupBy { it.memberId }
        val memberRows = members.map { m ->
            val fee = FeeCalculator.monthlyFeeFor(m.monthlyFeeOverride, defaultMonthlyFee)
            val owed = FeeCalculator.totalOwed(
                FeeCalculator.computeStatuses(m.joinDate, fee, paymentsByMember[m.id] ?: emptyList(), now)
            )
            MemberRow(
                name = m.name,
                role = rolesById[m.roleId]?.name ?: "",
                joinDate = m.joinDate,
                email = m.email,
                phone = m.phone,
                owed = owed
            )
        }

        val membersById = members.associateBy { it.id }
        val paymentRows = payments
            .sortedByDescending { it.paidDate }
            .map { p ->
                PaymentRow(
                    memberName = membersById[p.memberId]?.name ?: "",
                    period = p.periodMonth,
                    amount = p.amount,
                    paidDate = p.paidDate
                )
            }

        val attendeesByEvent = attendance.groupingBy { it.eventId }.eachCount()
        val eventRows = events
            .sortedByDescending { it.date }
            .map { e -> EventRow(e.title, e.date, attendeesByEvent[e.id] ?: 0) }

        val stats = DashboardCalculator.compute(members, payments, defaultMonthlyFee, events, today, now)
        val totals = ReportTotals(
            members = stats.totalMembers,
            collected = stats.collectedTotal,
            outstanding = stats.outstandingTotal,
            paidThisMonth = stats.paidThisMonth
        )

        return ReportData(memberRows, paymentRows, eventRows, totals)
    }

    fun membersCsv(data: ReportData): String {
        val sb = StringBuilder()
        sb.append("Ime,Uloga,Datum uclanjenja,E-mail,Telefon,Dugovanje (EUR)\n")
        data.memberRows.forEach { r ->
            sb.append(
                listOf(r.name, r.role, r.joinDate, r.email, r.phone, money(r.owed))
                    .joinToString(",") { csvCell(it) }
            )
            sb.append("\n")
        }
        return sb.toString()
    }

    fun paymentsCsv(data: ReportData): String {
        val sb = StringBuilder()
        sb.append("Clan,Mjesec,Iznos (EUR),Datum\n")
        data.paymentRows.forEach { r ->
            sb.append(
                listOf(r.memberName, r.period, money(r.amount), r.paidDate)
                    .joinToString(",") { csvCell(it) }
            )
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun money(v: Double): String =
        if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)

    private fun csvCell(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
