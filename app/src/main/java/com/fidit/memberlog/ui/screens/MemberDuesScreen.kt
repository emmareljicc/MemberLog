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
import com.fidit.memberlog.ui.FeeViewModel
import com.fidit.memberlog.ui.components.FeeHeatmap
import com.fidit.memberlog.ui.theme.DisplayFont
import com.fidit.memberlog.ui.theme.FeePaid
import com.fidit.memberlog.ui.theme.FeeUnpaid
import com.fidit.memberlog.util.FeeCalculator

@Composable
fun MemberDuesScreen(
    member: Member,
    feeViewModel: FeeViewModel
) {
    val config by feeViewModel.config.collectAsState()
    val payments by feeViewModel.paymentsFor(member.id).collectAsState(initial = emptyList())

    val fee = FeeCalculator.monthlyFeeFor(member.monthlyFeeOverride, config.defaultMonthlyFee)
    val statuses = FeeCalculator.computeStatuses(member.joinDate, fee, payments)
    val owed = FeeCalculator.totalOwed(statuses)
    val owedMonths = FeeCalculator.owedMonthsCount(statuses)
    val owing = owed > 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Moja članarina",
            fontFamily = DisplayFont,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Mjesečni iznos: ${money(fee)}" + if (member.monthlyFeeOverride != null) " (poseban)" else "",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(20.dp)) {
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
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
                FeeHeatmap(
                    statuses = statuses,
                    onCellClick = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"
