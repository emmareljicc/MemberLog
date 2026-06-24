package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.util.ReportBuilder
import com.fidit.memberlog.util.ReportData
import com.fidit.memberlog.util.ReportTotals
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ReportsViewModel(app: Application) : AndroidViewModel(app) {

    private val db = MemberDatabase.getInstance(app)

    private val base = combine(
        db.memberDao().getAll(),
        db.feeDao().allPayments(),
        db.roleDao().getAll()
    ) { members, payments, roles -> Triple(members, payments, roles) }

    private val activity = combine(
        db.activityDao().allEvents(),
        db.activityDao().allAttendance()
    ) { events, attendance -> events to attendance }

    val reportData: StateFlow<ReportData?> = combine(base, activity, db.feeDao().getConfig()) { (members, payments, roles), (events, attendance), config ->
        ReportBuilder.buildReportData(
            members = members,
            rolesById = roles.associateBy { it.id },
            payments = payments,
            events = events,
            attendance = attendance,
            defaultMonthlyFee = (config ?: FeeConfig()).defaultMonthlyFee
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
