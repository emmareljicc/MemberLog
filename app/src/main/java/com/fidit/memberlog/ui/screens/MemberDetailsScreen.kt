package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.MembershipStatus
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.FeeViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.FeeHeatmap
import com.fidit.memberlog.ui.components.HeroCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.components.StatusPill
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.ui.theme.paidColor
import com.fidit.memberlog.ui.theme.unpaidColor
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.FeeCalculator
import com.fidit.memberlog.util.PasswordHash
import com.fidit.memberlog.util.roleColor

@Composable
fun MemberDetailsScreen(
    member: Member,
    roles: List<Role>,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onUpdate: (Member) -> Unit,
    onDelete: () -> Unit,
    feeViewModel: FeeViewModel = viewModel(),
    activitiesViewModel: ActivitiesViewModel = viewModel()
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var recordPeriod by remember { mutableStateOf<String?>(null) }

    val role = roles.firstOrNull { it.id == member.roleId }
    val accent = role?.let { roleColor(it.colorHex) } ?: MaterialTheme.colorScheme.primary

    val config by feeViewModel.config.collectAsState()
    val payments by feeViewModel.paymentsFor(member.id).collectAsState(initial = null)
    val attendedState by activitiesViewModel.attendedEvents(member.id).collectAsState(initial = null)

    val paymentList = payments
    val attendedEvents = attendedState
    if (paymentList == null || attendedEvents == null) {
        LoadingSpinner()
        return
    }

    val monthlyFee = FeeCalculator.monthlyFeeFor(member.monthlyFeeOverride, config.defaultMonthlyFee)
    val statuses = FeeCalculator.computeStatuses(member.joinDate, monthlyFee, paymentList)
    val owed = FeeCalculator.totalOwed(statuses)
    val owedMonths = FeeCalculator.owedMonthsCount(statuses)
    val owing = owed > 0.0

    val ms = MembershipStatus.from(member.status)
    val statusColor = when (ms) {
        MembershipStatus.ACTIVE -> paidColor()
        MembershipStatus.INACTIVE -> MaterialTheme.colorScheme.onSurfaceVariant
        MembershipStatus.HONORARY -> MaterialTheme.colorScheme.primary
    }

    val rp = recordPeriod
    when {
        showEditDialog -> MemberFormScreen(
            mode = MemberFormMode.ADMIN_EDIT,
            roles = roles,
            existing = member,
            onBack = { showEditDialog = false },
            onSubmit = { name, roleId, email, phone, feeOverride, status, address, notes, photoPath, password ->
                onUpdate(
                    member.copy(
                        name = name, roleId = roleId, email = email, phone = phone,
                        monthlyFeeOverride = feeOverride, status = status, address = address,
                        notes = notes, photoPath = photoPath,
                        passwordHash = password?.let { PasswordHash.sha256(it) } ?: member.passwordHash
                    )
                )
                showEditDialog = false
            }
        )
        rp != null -> RecordPaymentScreen(
            period = rp,
            expectedAmount = monthlyFee,
            onBack = { recordPeriod = null },
            onConfirm = { amount, dateIso -> feeViewModel.recordPayment(member.id, rp, amount, dateIso); recordPeriod = null }
        )
        else -> Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Profil člana", onBack = onBack)
        Spacer(Modifier.height(Dimens.gap))

        HeroCard(modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                MemberAvatar(name = member.name, photoPath = member.photoPath, color = accent, size = Dimens.avatarLarge, fontSize = MaterialTheme.typography.displaySmall.fontSize)
                Spacer(Modifier.height(Dimens.gap))
                Text(member.name, style = MaterialTheme.typography.headlineSmall)
                Text((role?.name ?: "").uppercase(), style = MaterialTheme.typography.labelMedium, color = accent)
                Spacer(Modifier.height(Dimens.gapSmall))
                StatusPill(ms.label, statusColor)
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(20.dp))
                InfoRow(Icons.Default.CalendarMonth, "Član od", DateUtils.formatIsoDate(member.joinDate))
                Spacer(Modifier.height(Dimens.gap))
                InfoRow(Icons.Default.Email, "E-mail adresa", member.email)
                Spacer(Modifier.height(Dimens.gap))
                InfoRow(Icons.Default.Phone, "Broj mobitela", member.phone)
                if (member.address.isNotBlank()) {
                    Spacer(Modifier.height(Dimens.gap)); InfoRow(Icons.Default.Place, "Adresa", member.address)
                }
                if (member.notes.isNotBlank()) {
                    Spacer(Modifier.height(Dimens.gap)); InfoRow(Icons.AutoMirrored.Filled.Notes, "Bilješke", member.notes)
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            Text("Članarina", style = MaterialTheme.typography.titleMedium)
            Text(
                "Mjesečni iznos: ${money(monthlyFee)}" + if (member.monthlyFeeOverride != null) " (poseban)" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Dimens.gap))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (owing) Icons.Default.Error else Icons.Default.CheckCircle, contentDescription = null, tint = if (owing) unpaidColor() else paidColor())
                Spacer(Modifier.width(Dimens.gapSmall))
                Text(
                    if (owing) "Duguje: $owedMonths mj = ${money(owed)}" else "Sve podmireno",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (owing) unpaidColor() else paidColor()
                )
            }
            Spacer(Modifier.height(Dimens.gap))
            FeeHeatmap(statuses = statuses, onCellClick = { if (isAdmin) recordPeriod = it }, modifier = Modifier.fillMaxWidth())
            if (isAdmin) {
                Spacer(Modifier.height(Dimens.gap))
                Button(onClick = { recordPeriod = DateUtils.currentYearMonth() }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(Dimens.gapSmall))
                    Text("Zabilježi uplatu")
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            Text("Dolasci", style = MaterialTheme.typography.titleMedium)
            Text("Zabilježeno dolazaka: ${attendedEvents.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (attendedEvents.isNotEmpty()) {
                Spacer(Modifier.height(Dimens.gap))
                attendedEvents.take(5).forEach { ev ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(ev.title, style = MaterialTheme.typography.bodyMedium)
                        Text(DateUtils.formatIsoDate(ev.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (isAdmin) {
            Spacer(Modifier.height(Dimens.gap))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                OutlinedButton(onClick = { showEditDialog = true }, modifier = Modifier.weight(1f).height(50.dp), shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(Dimens.gapSmall))
                    Text("Uredi")
                }
                Button(onClick = onDelete, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = MaterialTheme.shapes.small) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(Dimens.gapSmall))
                    Text("Obriši")
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"
