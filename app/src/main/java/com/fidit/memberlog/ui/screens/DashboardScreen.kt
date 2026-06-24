package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.DashboardViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.HeroCard
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.components.GrowthChart
import com.fidit.memberlog.ui.components.ProgressRing
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.ui.theme.paidColor
import com.fidit.memberlog.ui.theme.unpaidColor
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.roleColor

@Composable
fun DashboardScreen(
    rolesById: Map<Int, Role>,
    onMemberClick: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val statsState by viewModel.stats.collectAsState()
    val paid = paidColor()
    val unpaid = unpaidColor()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Nadzorna ploča", subtitle = "Pregled stanja kluba")
        Spacer(Modifier.height(Dimens.gap))

        val stats = statsState ?: run {
            LoadingSpinner(Modifier.fillMaxWidth().height(300.dp))
            return@Column
        }
        val fraction = if (stats.totalMembers > 0) stats.paidThisMonth.toFloat() / stats.totalMembers else 0f
        val pct = (fraction * 100).toInt()

        HeroCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(fraction = fraction, ringColor = paid, trackColor = MaterialTheme.colorScheme.outlineVariant) {
                    Text("$pct%", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text("Plaćeno ovaj mjesec", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${stats.paidThisMonth} / ${stats.totalMembers}", style = MaterialTheme.typography.displaySmall)
                    Text("članova podmirilo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
            StatTile(Modifier.weight(1f), "Prikupljeno", money(stats.collectedTotal), paid, "ukupno")
            StatTile(Modifier.weight(1f), "Dugovanje", money(stats.outstandingTotal), if (stats.outstandingTotal > 0.0) unpaid else MaterialTheme.colorScheme.onSurface, "trenutno")
        }

        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Rast članstva", style = MaterialTheme.typography.titleMedium)
            Text("Broj članova kroz vrijeme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Dimens.gap))

            val maxMembers = stats.growth.maxOfOrNull { it.second } ?: 0
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.width(24.dp).height(120.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("$maxMembers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(Dimens.gapSmall))
                GrowthChart(
                    values = stats.growth.map { it.second },
                    lineColor = MaterialTheme.colorScheme.primary,
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f).height(120.dp)
                )
            }
            if (stats.growth.size >= 2) {
                Spacer(Modifier.height(Dimens.gapSmall))
                Row(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.width(32.dp))
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(DateUtils.formatPeriod(stats.growth.first().first), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(DateUtils.formatPeriod(stats.growth.last().first), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Nadolazeća događanja", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimens.gapSmall))
            if (stats.upcomingEvents.isEmpty()) {
                Text("Nema nadolazećih događanja.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                stats.upcomingEvents.take(3).forEach { ev ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(ev.title, style = MaterialTheme.typography.bodyMedium)
                        Text(DateUtils.formatIsoDate(ev.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Dugovanja", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimens.gapSmall))
            if (stats.topDebtors.isEmpty()) {
                Text("Svi članovi su podmireni.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                stats.topDebtors.take(5).forEach { (member, owed) ->
                    val accent = rolesById[member.roleId]?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onMemberClick(member.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MemberAvatar(name = member.name, photoPath = member.photoPath, color = accent, size = 36.dp, fontSize = MaterialTheme.typography.labelMedium.fontSize)
                        Spacer(Modifier.width(12.dp))
                        Text(member.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text(money(owed), style = MaterialTheme.typography.titleSmall, color = unpaid)
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Nedavne uplate", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(Dimens.gapSmall))
            if (stats.recentPayments.isEmpty()) {
                Text("Još nema zabilježenih uplata.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                stats.recentPayments.forEach { p ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${p.memberName}, ${DateUtils.formatPeriod(p.period)}", style = MaterialTheme.typography.bodyMedium)
                        Text(money(p.amount), style = MaterialTheme.typography.titleSmall, color = paid)
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))
    }
}

@Composable
private fun StatTile(modifier: Modifier, label: String, value: String, valueColor: Color, caption: String? = null) {
    AppCard(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor)
        if (caption != null) {
            Text(caption, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"
