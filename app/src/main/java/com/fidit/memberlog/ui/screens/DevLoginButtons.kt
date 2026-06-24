package com.fidit.memberlog.ui.screens

// DEV ONLY — ukloniti prije predaje projekta (obrisati ovu datoteku + poziv u LoginScreen).

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.ui.AuthViewModel

private const val DEV_ADMIN_EMAIL = "ivan.horvat@email.com"
private const val DEV_MEMBER_EMAIL = "petra.petrovic@email.com"
private const val DEV_PASSWORD = "lozinka"

@Composable
fun DevLoginButtons(
    authViewModel: AuthViewModel,
    onLoginSuccess: (memberId: Int, isAdmin: Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "DEV — ukloniti prije predaje",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { authViewModel.login(DEV_ADMIN_EMAIL, DEV_PASSWORD) { it?.let { r -> onLoginSuccess(r.memberId, r.isAdmin) } } },
                modifier = Modifier.weight(1f).height(46.dp)
            ) { Text("Prijava: Admin") }
            OutlinedButton(
                onClick = { authViewModel.login(DEV_MEMBER_EMAIL, DEV_PASSWORD) { it?.let { r -> onLoginSuccess(r.memberId, r.isAdmin) } } },
                modifier = Modifier.weight(1f).height(46.dp)
            ) { Text("Prijava: Član") }
        }
    }
}
