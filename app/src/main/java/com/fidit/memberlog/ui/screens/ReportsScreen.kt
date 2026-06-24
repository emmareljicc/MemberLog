package com.fidit.memberlog.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.ReportsViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.components.SectionLabel
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.DateUtils
import com.fidit.memberlog.util.PdfReport
import com.fidit.memberlog.util.ReportBuilder
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

private enum class ExportPeriod(val label: String) {
    MONTH("Ovaj mjesec"), YEAR("Ova godina"), ALL("Sve"), CUSTOM("Prilagođeno")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = viewModel()
) {
    val allData by viewModel.reportData.collectAsState()
    val context = LocalContext.current

    var period by remember { mutableStateOf(ExportPeriod.MONTH) }
    var customFrom by remember { mutableStateOf("") }
    var customTo by remember { mutableStateOf("") }

    val ym = YearMonth.now()
    val yr = LocalDate.now().year
    val (fromIso, toIso) = when (period) {
        ExportPeriod.MONTH -> Pair(ym.atDay(1).toString(), ym.atEndOfMonth().toString())
        ExportPeriod.YEAR -> Pair("$yr-01-01", "$yr-12-31")
        ExportPeriod.ALL -> Pair(null, null)
        ExportPeriod.CUSTOM -> Pair(customFrom.ifBlank { null }, customTo.ifBlank { null })
    }

    val data = remember(allData, fromIso, toIso) { allData?.let { ReportBuilder.filterByRange(it, fromIso, toIso) } }

    val periodLabel = when (period) {
        ExportPeriod.MONTH -> DateUtils.formatPeriod(ym.toString())
        ExportPeriod.YEAR -> "$yr"
        ExportPeriod.ALL -> "Sve"
        ExportPeriod.CUSTOM -> if (fromIso != null || toIso != null) "od ${fromIso ?: "…"} do ${toIso ?: "…"}" else "Sve"
    }
    val suffix = when (period) {
        ExportPeriod.MONTH -> ym.toString()
        ExportPeriod.YEAR -> "$yr"
        ExportPeriod.ALL -> "sve"
        ExportPeriod.CUSTOM -> "${fromIso ?: "x"}_${toIso ?: "x"}"
    }

    val totalDebt = remember(allData) {
        allData?.memberRows?.sumOf { it.owed } ?: 0.0
    }
    val debtorCount = remember(allData) {
        allData?.memberRows?.count { it.owed > 0.0 } ?: 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Izvještaji", subtitle = "Izvezi podatke i sažetak kluba", onBack = onBack)
        Spacer(Modifier.height(Dimens.sectionGap))

        if (data == null) {
            LoadingSpinner()
            return@Column
        }

        SectionLabel("RAZDOBLJE")
        Spacer(Modifier.height(Dimens.gapSmall))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.gapSmall)
            ) {
                ExportPeriod.entries.forEach { p ->
                    FilterChip(selected = period == p, onClick = { period = p }, label = { Text(p.label) })
                }
            }
            if (period == ExportPeriod.CUSTOM) {
                Spacer(Modifier.height(Dimens.gap))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.gap)) {
                    OutlinedTextField(value = customFrom, onValueChange = { customFrom = it }, label = { Text("Od (GGGG-MM-DD)") }, singleLine = true, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = customTo, onValueChange = { customTo = it }, label = { Text("Do (GGGG-MM-DD)") }, singleLine = true, shape = MaterialTheme.shapes.small, modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(Dimens.gap))
            Text(
                "${data.paymentRows.size} uplata · ${data.eventRows.size} događanja · ${money(data.totals.collected)} prikupljeno",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(Dimens.sectionGap))
        SectionLabel("IZVOZ")
        Spacer(Modifier.height(Dimens.gapSmall))

        ExportRow(Icons.Default.Group, "Članovi (CSV)", "${data.memberRows.size} članova") {
            shareText(context, "clanovi_$suffix.csv", ReportBuilder.membersCsv(data))
        }
        Spacer(Modifier.height(Dimens.gapSmall))
        ExportRow(Icons.Default.Payments, "Uplate (CSV)", "${data.paymentRows.size} uplata u razdoblju") {
            shareText(context, "uplate_$suffix.csv", ReportBuilder.paymentsCsv(data))
        }
        Spacer(Modifier.height(Dimens.gapSmall))
        ExportRow(Icons.Default.PictureAsPdf, "Cijeli izvještaj (PDF)", "Sažetak, dugovanja i događanja") {
            val file = PdfReport.write(context, data, periodLabel)
            shareFile(context, file, "application/pdf")
        }

        Spacer(Modifier.height(Dimens.sectionGap))
        SectionLabel("STANJE DUGOVANJA")
        Spacer(Modifier.height(Dimens.gapSmall))
        AppCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Ukupno potraživanje", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(money(totalDebt), style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                Text("$debtorCount članova s nepodmirenim dugovanjima", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SendDebtorsEmailButton(reportData = allData)
        }

        Spacer(Modifier.height(Dimens.gap))
    }
}

@Composable
private fun ExportRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onClick, contentPadding = 16.dp) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun money(v: Double): String =
    (if (v % 1.0 == 0.0) v.toInt().toString() else "%.2f".format(v)) + " €"

private fun shareText(context: Context, fileName: String, content: String) {
    val dir = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
    val file = File(dir, fileName)
    file.writeText(content, Charsets.UTF_8)
    shareFile(context, file, "text/csv")
}

private fun shareFile(context: Context, file: File, mime: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "Podijeli izvještaj").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(chooser)
    } catch (_: Exception) {
        Toast.makeText(context, "Datoteka spremljena: ${file.name}", Toast.LENGTH_SHORT).show()
    }
}
