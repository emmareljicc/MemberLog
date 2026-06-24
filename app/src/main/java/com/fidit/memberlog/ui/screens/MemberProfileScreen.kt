package com.fidit.memberlog.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.LoginActivity
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.MembershipStatus
import com.fidit.memberlog.ui.MembersViewModel
import com.fidit.memberlog.ui.MemberSessionViewModel
import com.fidit.memberlog.ui.components.AppCard
import com.fidit.memberlog.ui.components.HeroCard
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.ui.components.ScreenHeader
import com.fidit.memberlog.ui.components.SectionLabel
import com.fidit.memberlog.ui.theme.Dimens
import com.fidit.memberlog.util.DateUtils
import java.io.File
import java.util.UUID

@Composable
fun MemberProfileScreen(
    member: Member,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    sessionViewModel: MemberSessionViewModel,
    onNavigateToExchangeRates: () -> Unit,
    membersViewModel: MembersViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember(member.id) { mutableStateOf(member.email) }
    var phone by remember(member.id) { mutableStateOf(member.phone) }
    var address by remember(member.id) { mutableStateOf(member.address) }
    var photoPath by remember(member.id) { mutableStateOf(member.photoPath) }
    var newPassword by remember { mutableStateOf("") }

    val owedByMember by membersViewModel.owedByMember.collectAsState()
    val memberOwed = owedByMember?.get(member.id) ?: 0.0

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) copyMemberPhoto(context, uri)?.let { photoPath = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screenPadding)
    ) {
        ScreenHeader(title = "Moj profil")
        Spacer(Modifier.height(Dimens.gap))

        HeroCard(modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                MemberAvatar(name = member.name, photoPath = photoPath, color = MaterialTheme.colorScheme.primary, size = Dimens.avatarLarge, fontSize = MaterialTheme.typography.displaySmall.fontSize)
                Spacer(Modifier.height(Dimens.gapSmall))
                Text(member.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    "${MembershipStatus.from(member.status).label} • član od ${DateUtils.formatIsoDate(member.joinDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (photoPath == null) "Dodaj sliku" else "Promijeni sliku")
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        ProfileCard("KONTAKT") {
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") }, singleLine = true, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Broj mobitela") }, singleLine = true, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Adresa") }, singleLine = true, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        sessionViewModel.updateContact(member.copy(email = email, phone = phone, address = address, photoPath = photoPath))
                        Toast.makeText(context, "Spremljeno", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "E-mail je obavezan", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) { Text("Spremi promjene") }
        }

        Spacer(Modifier.height(Dimens.gap))

        ProfileCard("LOZINKA") {
            OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Nova lozinka") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    if (newPassword.length >= 4) {
                        sessionViewModel.changePassword(member, newPassword)
                        newPassword = ""
                        Toast.makeText(context, "Lozinka promijenjena", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "Lozinka mora imati barem 4 znaka", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            ) { Text("Promijeni lozinku") }
        }

        Spacer(Modifier.height(Dimens.gap))

        ProfileCard("IZGLED") {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tamni način rada", style = MaterialTheme.typography.titleMedium)
                Switch(checked = isDarkMode, onCheckedChange = onThemeChanged)
            }
        }

        Spacer(Modifier.height(Dimens.gap))

        SectionLabel("TEČAJNA LISTA")
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

        if (memberOwed > 0.0) {
            Spacer(Modifier.height(Dimens.gapSmall))
            Text(
                text = "Trenutni dug: ${String.format("%.2f", memberOwed)} EUR",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(Dimens.sectionGap))

        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = MaterialTheme.shapes.small
        ) { Text("ODJAVI SE", color = Color.White) }

        Spacer(Modifier.height(Dimens.gap))
    }
}

@Composable
private fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    SectionLabel(title, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(Dimens.gapSmall))
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.gap), content = content)
    }
}

private fun copyMemberPhoto(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "photos").apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}