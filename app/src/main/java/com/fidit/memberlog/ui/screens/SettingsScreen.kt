package com.fidit.memberlog.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.BuildConfig
import com.fidit.memberlog.LoginActivity
import com.fidit.memberlog.ui.FeeViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.components.SectionLabel
import com.fidit.memberlog.ui.theme.Dimens

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    isAdmin: Boolean,
    userName: String,
    userRole: String,
    onOpenMyProfile: () -> Unit,
    onManageRoles: () -> Unit,
    onOpenReports: () -> Unit,
    onNavigateToExchangeRates: () -> Unit,
    feeViewModel: FeeViewModel = viewModel()
) {
    val context = LocalContext.current
    val config by feeViewModel.config.collectAsState()
    var feeText by remember(config.defaultMonthlyFee) {
        mutableStateOf(
            if (config.defaultMonthlyFee % 1.0 == 0.0) config.defaultMonthlyFee.toInt().toString()
            else config.defaultMonthlyFee.toString()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Postavke", subtitle = "Prilagodi izgled i funkcionalnosti")
        Spacer(Modifier.height(Dimens.sectionGap))

        SectionLabel("RAČUN")
        Spacer(Modifier.height(Dimens.gapSmall))
        SettingsLinkCard("Moj profil", "Moja članarina, događanja i uređivanje profila", onOpenMyProfile)
        Spacer(Modifier.height(Dimens.gap))

        SectionLabel("IZGLED")
        Spacer(Modifier.height(Dimens.gapSmall))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Tamni način rada", style = MaterialTheme.typography.titleMedium)
                    Text("Uključi ili isključi tamni izgled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isDarkMode, onCheckedChange = { onThemeChanged(it) })
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        SectionLabel("VALUTA I TEČAJ")
        Spacer(Modifier.height(Dimens.gapSmall))
        AppCard(modifier = Modifier.fillMaxWidth(), onClick = onNavigateToExchangeRates) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tečajna lista", style = MaterialTheme.typography.titleMedium)
                    Text("Pregled tečajeva valuta za klupska dugovanja", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        SectionLabel("ČLANARINA")
        Spacer(Modifier.height(Dimens.gapSmall))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Text("Zadani mjesečni iznos", style = MaterialTheme.typography.titleMedium)
            Text("Primjenjuje se na članove bez posebnog iznosa.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Dimens.gap))
            if (isAdmin) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("EUR") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(Dimens.gap))
                    Button(
                        onClick = {
                            val parsed = feeText.replace(',', '.').toDoubleOrNull()
                            if (parsed != null && parsed >= 0.0) {
                                feeViewModel.setDefaultFee(parsed)
                                Toast.makeText(context, "Spremljeno", Toast.LENGTH_SHORT).show()
                            } else Toast.makeText(context, "Neispravan iznos", Toast.LENGTH_SHORT).show()
                        },
                        shape = MaterialTheme.shapes.small
                    ) { Text("Spremi") }
                }
            } else {
                Text("$feeText €", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        if (isAdmin) {
            SectionLabel("UPRAVLJANJE")
            Spacer(Modifier.height(Dimens.gapSmall))
            SettingsLinkCard("Uloge", "Dodaj i uredi uloge i boje", onManageRoles)
            Spacer(Modifier.height(Dimens.gapSmall))
            SettingsLinkCard("Izvještaji", "Izvoz u CSV i PDF", onOpenReports)
            Spacer(Modifier.height(Dimens.gap))
        }

        SectionLabel("INFORMACIJE")
        Spacer(Modifier.height(Dimens.gapSmall))
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Trenutni korisnik", style = MaterialTheme.typography.titleSmall)
                Text(userName.ifBlank { "—" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (userRole.isNotBlank()) {
                Spacer(Modifier.height(Dimens.gap))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Uloga", style = MaterialTheme.typography.titleSmall)
                    Text(userRole, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(Dimens.gap))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Verzija aplikacije", style = MaterialTheme.typography.titleSmall)
                Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(Dimens.sectionGap))

        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                Toast.makeText(context, "Odjava uspješna", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = MaterialTheme.shapes.small
        ) { Text("ODJAVI SE", color = Color.White) }

        Spacer(Modifier.height(Dimens.gap))
    }
}

@Composable
private fun SettingsLinkCard(title: String, subtitle: String, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
