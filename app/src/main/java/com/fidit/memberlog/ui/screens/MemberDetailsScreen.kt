package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.Role
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.FeeViewModel
import com.fidit.memberlog.ui.components.FeeHeatmap
import com.fidit.memberlog.ui.theme.FeePaid
import com.fidit.memberlog.ui.theme.FeeUnpaid
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.FeeCalculator
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
    val payments by feeViewModel.paymentsFor(member.id).collectAsState(initial = emptyList())
    val attendedEvents by activitiesViewModel.attendedEvents(member.id).collectAsState(initial = emptyList())

    val monthlyFee = FeeCalculator.monthlyFeeFor(member.monthlyFeeOverride, config.defaultMonthlyFee)
    val statuses = FeeCalculator.computeStatuses(member.joinDate, monthlyFee, payments)
    val owed = FeeCalculator.totalOwed(statuses)
    val owedMonths = FeeCalculator.owedMonthsCount(statuses)

    if (showEditDialog) {
        MemberFormDialog(
            title = "Uredi člana",
            roles = roles,
            existing = member,
            confirmLabel = "Spremi",
            onDismiss = { showEditDialog = false },
            onSubmit = { name, roleId, email, phone, feeOverride ->
                onUpdate(
                    member.copy(
                        name = name,
                        roleId = roleId,
                        email = email,
                        phone = phone,
                        monthlyFeeOverride = feeOverride
                    )
                )
                showEditDialog = false
            }
        )
    }

    recordPeriod?.let { period ->
        RecordPaymentDialog(
            period = period,
            expectedAmount = monthlyFee,
            onDismiss = { recordPeriod = null },
            onConfirm = { amount, dateIso ->
                feeViewModel.recordPayment(member.id, period, amount, dateIso)
                recordPeriod = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Natrag",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Natrag na listu", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    val dijelovi = member.name.split(" ")
                    val inicijali = "${dijelovi[0][0]}${dijelovi.getOrNull(1)?.get(0) ?: ""}"
                    Text(
                        text = inicijali,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(member.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = (role?.name ?: "").uppercase(),
                    fontSize = 14.sp,
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))

                InfoRow(Icons.Default.CalendarMonth, "Član od", DateUtils.formatIsoDate(member.joinDate))
                Spacer(modifier = Modifier.height(16.dp))
                InfoRow(Icons.Default.Email, "E-mail adresa", member.email)
                Spacer(modifier = Modifier.height(16.dp))
                InfoRow(Icons.Default.Phone, "Broj mobitela", member.phone)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Članarina", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Mjesečni iznos: ${money(monthlyFee)}" +
                        if (member.monthlyFeeOverride != null) " (poseban)" else "",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                val owing = owed > 0.0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (owing) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (owing) FeeUnpaid else FeePaid
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (owing) "Duguje: $owedMonths mj = ${money(owed)}" else "Sve podmireno",
                        color = if (owing) FeeUnpaid else FeePaid,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                FeeHeatmap(
                    statuses = statuses,
                    onCellClick = { if (isAdmin) recordPeriod = it },
                    modifier = Modifier.fillMaxWidth()
                )

                if (isAdmin) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { recordPeriod = DateUtils.currentYearMonth() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Zabilježi uplatu")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Dolasci", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Prisustvovao na ${attendedEvents.size} događaja",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (attendedEvents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    attendedEvents.take(5).forEach { ev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ev.title, fontSize = 14.sp)
                            Text(DateUtils.formatIsoDate(ev.date), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uredi")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Obriši")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"
