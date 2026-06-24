package com.fidit.memberlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.ui.theme.paidColor
import com.fidit.memberlog.ui.theme.partialColor
import com.fidit.memberlog.ui.theme.unpaidColor
import com.fidit.memberlog.util.MonthFeeStatus
import com.fidit.memberlog.util.MonthStatus

private val CellGap = 4.dp
private val YearLabelWidth = 34.dp

@Composable
fun FeeHeatmap(
    statuses: List<MonthStatus>,
    onCellClick: (period: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (statuses.isEmpty()) return

    val paid = paidColor()
    val partial = partialColor()
    val unpaid = unpaidColor()
    val futureColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    val byPeriod = statuses.associateBy { it.period }
    val years = statuses.map { it.period.substring(0, 4).toInt() }
    val minYear = years.min()
    val maxYear = years.max()
    BoxWithConstraints(modifier = modifier) {
        val cell = ((maxWidth - YearLabelWidth - CellGap * 11) / 12).coerceIn(16.dp, 28.dp)
        val cellShape = MaterialTheme.shapes.extraSmall

        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(CellGap)) {
                Spacer(Modifier.width(YearLabelWidth))
                (1..12).forEach { m ->
                    Box(Modifier.width(cell), contentAlignment = Alignment.Center) {
                        Text(
                            "$m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    (1..12).forEach { month ->
                        val period = "%04d-%02d".format(year, month)
                        val status = byPeriod[period]
                        val color = when (status?.status) {
                            MonthFeeStatus.PAID -> paid
                            MonthFeeStatus.PARTIAL -> partial
                            MonthFeeStatus.UNPAID -> unpaid
                            else -> futureColor
                        }
                        val clickable = status != null && status.status != MonthFeeStatus.FUTURE
                        Box(
                            modifier = Modifier
                                .size(cell)
                                .clip(cellShape)
                                .background(color)
                                .then(if (clickable) Modifier.clickable { onCellClick(period) } else Modifier)
                        )
                    }
                }
                Spacer(Modifier.size(CellGap))
            }

            Spacer(Modifier.size(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                LegendItem(paid, "Plaćeno")
                LegendItem(partial, "Djelomično")
                LegendItem(unpaid, "Neplaćeno")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
