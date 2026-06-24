package com.fidit.memberlog.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.fidit.memberlog.util.ReportData

@Composable
fun SendDebtorsEmailButton(reportData: ReportData?) {
    val context = LocalContext.current

    val debtorEmails = reportData?.memberRows?.filter { row ->
        row.owed > 0.0 && !row.email.isNullOrBlank()
    }?.mapNotNull { it.email }.orEmpty()

    if (debtorEmails.isNotEmpty()) {
        Button(
            onClick = {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_BCC, debtorEmails.toTypedArray())
                    putExtra(Intent.EXTRA_SUBJECT, "Podsjetnik za nepodmirenu članarinu")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Poštovani,\n\novim putem vas želimo podsjetiti na nepodmirena dugovanja prema udruzi. Molimo vas da podmirite dugovanje u najkraćem mogućem roku.\n\nSrdačan pozdrav,"
                    )
                }
                context.startActivity(Intent.createChooser(emailIntent, "Pošalji e-mail putem..."))
            }
        ) {
            Text("Obavijesti dužnike (${debtorEmails.size})")
        }
    }
}