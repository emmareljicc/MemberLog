package com.fidit.memberlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidit.memberlog.ui.theme.FeePaid
import com.fidit.memberlog.ui.theme.FeePartial
import com.fidit.memberlog.ui.theme.FeeUnpaid
import com.fidit.memberlog.util.MonthFeeStatus
import com.fidit.memberlog.util.MonthStatus

private val CellSize = 24.dp
private val CellGap = 4.dp
private val YearLabelWidth = 40.dp

@Composable
fun FeeHeatmap(
    statuses: List<MonthStatus>,
    onCellClick: (period: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (statuses.isEmpty()) return

    val byPeriod = statuses.associateBy { it.period }
    val futureColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val years = statuses.map { it.period.substring(0, 4).toInt() }
    val minYear = years.min()
    val maxYear = years.max()
    val totalCells = (maxYear - minYear + 1) * 12

    val reveal = remember { Animatable(0f) }
    LaunchedEffect(statuses) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, animationSpec = tween(700))
    }

    Column(modifier = modifier.horizontalScroll(rememberScrollState())) {

        Row(horizontalArrangement = Arrangement.spacedBy(CellGap)) {
            Spacer(Modifier.width(YearLabelWidth))
            (1..12).forEach { m ->
                Box(Modifier.size(CellSize), contentAlignment = Alignment.Center) {
                    Text("$m", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.size(CellGap))

        (minYear..maxYear).forEach { year ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(CellGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.width(YearLabelWidth), contentAlignment = Alignment.CenterStart) {
                    Text(
                        "$year",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                (1..12).forEach { month ->
                    val period = "%04d-%02d".format(year, month)
                    val status = byPeriod[period]
                    val color = when (status?.status) {
                        MonthFeeStatus.PAID -> FeePaid
                        MonthFeeStatus.PARTIAL -> FeePartial
                        MonthFeeStatus.UNPAID -> FeeUnpaid
                        else -> futureColor
                    }
                    val clickable = status != null && status.status != MonthFeeStatus.FUTURE
                    val ordinal = (year - minYear) * 12 + (month - 1)
                    val cellAlpha = (reveal.value * (totalCells + 8) - ordinal).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .size(CellSize)
                            .alpha(cellAlpha)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color)
                            .then(
                                if (clickable) Modifier.clickable { onCellClick(period) } else Modifier
                            )
                    )
                }
            }
            Spacer(Modifier.size(CellGap))
        }

        Spacer(Modifier.size(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            LegendItem(FeePaid, "Plaćeno")
            LegendItem(FeePartial, "Djelomično")
            LegendItem(FeeUnpaid, "Neplaćeno")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
