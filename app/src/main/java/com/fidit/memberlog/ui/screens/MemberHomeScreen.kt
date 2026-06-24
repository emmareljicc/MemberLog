package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.FeeViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.FeeHeatmap
import com.fidit.memberlog.ui.components.HeroCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.SectionLabel
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.ui.theme.paidColor
import com.fidit.memberlog.ui.theme.unpaidColor
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.FeeCalculator
import com.fidit.memberlog.util.Format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberHomeScreen(
    member: Member,
    feeViewModel: FeeViewModel,
    activitiesViewModel: ActivitiesViewModel,
    onOpenEvents: () -> Unit
) {
    val config by feeViewModel.config.collectAsState()
    val rates by feeViewModel.rates.collectAsState()
    val payments by feeViewModel.paymentsFor(member.id).collectAsState(initial = null)
    val events by activitiesViewModel.events.collectAsState()
    val rsvpIds by activitiesViewModel.rsvpEventIds(member.id).collectAsState(initial = emptyList())
    val attended by activitiesViewModel.attendedEvents(member.id).collectAsState(initial = null)

    val eventList = events
    val paymentList = payments
    val attendedList = attended
    if (eventList == null || paymentList == null || attendedList == null) {
        LoadingSpinner(Modifier.fillMaxSize())
        return
    }

    val currentMonth = DateUtils.currentYearMonth()
    val fee = FeeCalculator.effectiveFee(member.id, currentMonth, rates, config.defaultMonthlyFee)
    val isSpecialFee = FeeCalculator.isOverridden(member.id, currentMonth, rates)
    val statuses = FeeCalculator.computeStatuses(
        member.joinDate,
        { p -> FeeCalculator.effectiveFee(member.id, p, rates, config.defaultMonthlyFee) },
        paymentList
    )
    val owed = FeeCalculator.totalOwed(statuses)
    val owedMonths = FeeCalculator.owedMonthsCount(statuses)
    val owing = owed > 0.0

    val today = DateUtils.todayIso()
    val nextEvent = eventList.filter { it.date >= today }.minByOrNull { it.date }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screenPadding)
    ) {
        Text("Pozdrav,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(member.name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(Dimens.gap))

        HeroCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            SectionLabel("MOJA ČLANARINA")
            Text(
                "Mjesečni iznos: ${money(fee)}" + if (isSpecialFee) " (poseban)" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Dimens.gap))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (owing) Icons.Default.Error else Icons.Default.CheckCircle, contentDescription = null, tint = if (owing) unpaidColor() else paidColor())
                Spacer(Modifier.width(Dimens.gapSmall))
                Text(
                    if (owing) "Duguješ: $owedMonths mj = ${money(owed)}" else "Sve podmireno",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (owing) unpaidColor() else paidColor()
                )
            }
            Spacer(Modifier.height(Dimens.gap))
            FeeHeatmap(statuses = statuses, onCellClick = {}, modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenEvents) {
            SectionLabel("NADOLAZEĆE DOGAĐANJE")
            Spacer(Modifier.height(Dimens.gapSmall))
            if (nextEvent == null) {
                Text("Nema nadolazećih događanja.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(nextEvent.title, style = MaterialTheme.typography.titleMedium)
                Text("${DateUtils.formatIsoDate(nextEvent.date)} • ${nextEvent.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(Dimens.gapSmall))
                val coming = rsvpIds.contains(nextEvent.id)
                FilterChip(selected = coming, onClick = { activitiesViewModel.setRsvp(nextEvent.id, member.id, !coming) }, label = { Text(if (coming) "Dolazim" else "Dolazim?") })
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth()) {
            SectionLabel("ZABILJEŽENI DOLASCI")
            Spacer(Modifier.height(Dimens.gapSmall))
            if (attendedList.isEmpty()) {
                Text("Još nema zabilježenih dolazaka.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                attendedList.take(3).forEach { ev ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(ev.title, style = MaterialTheme.typography.bodyMedium)
                        Text(DateUtils.formatIsoDate(ev.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))
    }
}

private fun money(v: Double): String = Format.eur(v)
