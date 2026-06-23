package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.FeeViewModel
import com.fidit.memberlog.ui.theme.DisplayFont
import com.fidit.memberlog.ui.theme.FeePaid
import com.fidit.memberlog.ui.theme.FeeUnpaid
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.FeeCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberHomeScreen(
    member: Member,
    feeViewModel: FeeViewModel,
    activitiesViewModel: ActivitiesViewModel,
    onOpenDues: () -> Unit
) {
    val config by feeViewModel.config.collectAsState()
    val payments by feeViewModel.paymentsFor(member.id).collectAsState(initial = emptyList())
    val events by activitiesViewModel.events.collectAsState()
    val rsvpIds by activitiesViewModel.rsvpEventIds(member.id).collectAsState(initial = emptyList())
    val attended by activitiesViewModel.attendedEvents(member.id).collectAsState(initial = emptyList())

    val fee = FeeCalculator.monthlyFeeFor(member.monthlyFeeOverride, config.defaultMonthlyFee)
    val statuses = FeeCalculator.computeStatuses(member.joinDate, fee, payments)
    val owed = FeeCalculator.totalOwed(statuses)
    val owedMonths = FeeCalculator.owedMonthsCount(statuses)
    val owing = owed > 0.0

    val today = DateUtils.todayIso()
    val nextEvent = events.filter { it.date >= today }.minByOrNull { it.date }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Pozdrav,", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            member.name,
            fontFamily = DisplayFont,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Moja članarina", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (owing) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (owing) FeeUnpaid else FeePaid
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (owing) "Duguješ: $owedMonths mj = ${money(owed)}" else "Sve podmireno",
                        color = if (owing) FeeUnpaid else FeePaid,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onOpenDues,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Pogledaj članarinu") }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Sljedeći događaj", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                if (nextEvent == null) {
                    Text("Nema nadolazećih događaja.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(nextEvent.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${DateUtils.formatIsoDate(nextEvent.date)} • ${nextEvent.location}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    val coming = rsvpIds.contains(nextEvent.id)
                    FilterChip(
                        selected = coming,
                        onClick = { activitiesViewModel.setRsvp(nextEvent.id, member.id, !coming) },
                        label = { Text(if (coming) "Dolazim ✓" else "Dolazim?") }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Moji dolasci", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                if (attended.isEmpty()) {
                    Text("Još niste zabilježeni ni na jednom događaju.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    attended.take(3).forEach { ev ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ev.title, fontSize = 14.sp)
                            Text(DateUtils.formatIsoDate(ev.date), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"
