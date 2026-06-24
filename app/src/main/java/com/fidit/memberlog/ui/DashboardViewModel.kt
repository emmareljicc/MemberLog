package com.fidit.memberlog.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fidit.memberlog.data.MemberDatabase
import com.fidit.memberlog.model.FeeConfig
import com.fidit.memberlog.util.DashboardCalculator
import com.fidit.memberlog.util.DashboardStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class DashboardViewModel(app: Application) : AndroidViewModel(app) {

    private val db = MemberDatabase.getInstance(app)

    val stats: StateFlow<DashboardStats?> = combine(
        db.memberDao().getAll(),
        db.feeDao().allPayments(),
        db.feeDao().getConfig(),
        db.activityDao().allEvents()
    ) { members, payments, config, events ->
        DashboardCalculator.compute(
            members = members,
            payments = payments,
            defaultMonthlyFee = (config ?: FeeConfig()).defaultMonthlyFee,
            events = events,
            today = LocalDate.now().toString()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
