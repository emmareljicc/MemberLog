package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.DashboardViewModel
import com.fidit.memberlog.ui.components.GrowthChart
import com.fidit.memberlog.ui.components.ProgressRing
import com.fidit.memberlog.ui.theme.DisplayFont
import com.fidit.memberlog.ui.theme.FeePaid
import com.fidit.memberlog.ui.theme.FeeUnpaid
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.roleColor

@Composable
fun DashboardScreen(
    rolesById: Map<Int, Role>,
    onMemberClick: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val fraction = if (stats.totalMembers > 0) stats.paidThisMonth.toFloat() / stats.totalMembers else 0f
    val pct = (fraction * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Nadzorna ploča", fontFamily = DisplayFont, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Pregled stanja kluba", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProgressRing(
                    fraction = fraction,
                    ringColor = FeePaid,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                ) {
                    Text("$pct%", fontFamily = DisplayFont, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text("Plaćeno ovaj mjesec", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${stats.paidThisMonth} / ${stats.totalMembers}",
                        fontFamily = DisplayFont,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("članova podmirilo", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Prikupljeno",
                value = money(stats.collectedTotal),
                valueColor = FeePaid
            )
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Dugovanje",
                value = money(stats.outstandingTotal),
                valueColor = if (stats.outstandingTotal > 0.0) FeeUnpaid else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Rast članstva", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("Ukupno ${stats.totalMembers} članova", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                GrowthChart(
                    values = stats.growth.map { it.second },
                    lineColor = MaterialTheme.colorScheme.primary,
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Nadolazeći događaji", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (stats.upcomingEvents.isEmpty()) {
                    Text("Nema nadolazećih događaja.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    stats.upcomingEvents.take(3).forEach { ev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ev.title, fontSize = 14.sp)
                            Text(DateUtils.formatIsoDate(ev.date), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Treba pozornost", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (stats.topDebtors.isEmpty()) {
                    Text("Svi članovi su podmireni.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    stats.topDebtors.take(5).forEach { (member, owed) ->
                        val accent = rolesById[member.roleId]?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMemberClick(member.id) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials(member.name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(member.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text(money(owed), color = FeeUnpaid, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Nedavne aktivnosti", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                if (stats.recentPayments.isEmpty()) {
                    Text("Još nema zabilježenih uplata.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    stats.recentPayments.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${p.memberName} — ${DateUtils.formatPeriod(p.period)}", fontSize = 14.sp)
                            Text(money(p.amount), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = FeePaid)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatTile(modifier: Modifier, label: String, value: String, valueColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, fontFamily = DisplayFont, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

private fun initials(name: String): String {
    val parts = name.split(" ")
    return if (parts.size > 1) "${parts[0][0]}${parts[1][0]}" else name.take(1)
}

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"
