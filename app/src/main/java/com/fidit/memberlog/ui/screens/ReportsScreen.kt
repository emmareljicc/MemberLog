package com.fidit.memberlog.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.ReportsViewModel
import com.fidit.memberlog.util.PdfReport
import com.fidit.memberlog.util.ReportBuilder
import java.io.File

@Composable
fun ReportsScreen(
    onBack: () -> Unit,
    viewModel: ReportsViewModel = viewModel()
) {
    val data by viewModel.reportData.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Natrag", tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Natrag", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))
        Text("Izvještaji", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Izvezi podatke i sažetak kluba", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Sadržaj", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text("Članova: ${data.totals.members}", fontSize = 13.sp)
                Text("Uplata: ${data.paymentRows.size}", fontSize = 13.sp)
                Text("Događaja: ${data.eventRows.size}", fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        ExportButton(Icons.Default.Description, "Izvezi članove (CSV)") {
            shareText(context, "clanovi.csv", "text/csv", ReportBuilder.membersCsv(data))
        }
        Spacer(Modifier.height(12.dp))
        ExportButton(Icons.Default.Description, "Izvezi uplate (CSV)") {
            shareText(context, "uplate.csv", "text/csv", ReportBuilder.paymentsCsv(data))
        }
        Spacer(Modifier.height(12.dp))
        ExportButton(Icons.Default.PictureAsPdf, "Izvezi izvještaj (PDF)") {
            val file = PdfReport.write(context, data)
            shareFile(context, file, "application/pdf")
        }
    }
}

@Composable
private fun ExportButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

private fun shareText(context: Context, fileName: String, mime: String, content: String) {
    val dir = File(context.cacheDir, "reports").apply { mkdirs() }
    val file = File(dir, fileName)
    file.writeText(content, Charsets.UTF_8)
    shareFile(context, file, mime)
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
    } catch (e: Exception) {
        Toast.makeText(context, "Datoteka spremljena: ${file.name}", Toast.LENGTH_SHORT).show()
    }
}
