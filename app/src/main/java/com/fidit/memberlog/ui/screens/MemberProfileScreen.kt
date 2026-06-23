package com.fidit.memberlog.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidit.memberlog.LoginActivity
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.model.MembershipStatus
import com.fidit.memberlog.ui.MemberSessionViewModel
import com.fidit.memberlog.ui.components.MemberAvatar
import com.fidit.memberlog.ui.theme.DisplayFont
import com.fidit.memberlog.util.DateUtils
import java.io.File
import java.util.UUID

@Composable
fun MemberProfileScreen(
    member: Member,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    sessionViewModel: MemberSessionViewModel
) {
    val context = LocalContext.current
    var email by remember(member.id) { mutableStateOf(member.email) }
    var phone by remember(member.id) { mutableStateOf(member.phone) }
    var address by remember(member.id) { mutableStateOf(member.address) }
    var photoPath by remember(member.id) { mutableStateOf(member.photoPath) }
    var newPassword by remember { mutableStateOf("") }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) copyMemberPhoto(context, uri)?.let { photoPath = it }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Moj profil",
            fontFamily = DisplayFont,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(Modifier.height(16.dp))

        MemberAvatar(
            name = member.name,
            photoPath = photoPath,
            color = MaterialTheme.colorScheme.primary,
            size = 96.dp,
            fontSize = 32.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(member.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            "${MembershipStatus.from(member.status).label} • član od ${DateUtils.formatIsoDate(member.joinDate)}",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }) {
            Icon(Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text(if (photoPath == null) "Dodaj sliku" else "Promijeni sliku")
        }

        Spacer(Modifier.height(16.dp))

        ProfileCard("KONTAKT") {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Broj mobitela") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Adresa") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        sessionViewModel.updateContact(
                            member.copy(email = email, phone = phone, address = address, photoPath = photoPath)
                        )
                        Toast.makeText(context, "Spremljeno", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "E-mail je obavezan", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Spremi promjene") }
        }

        Spacer(Modifier.height(16.dp))

        ProfileCard("LOZINKA") {
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nova lozinka") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (newPassword.length >= 4) {
                        sessionViewModel.changePassword(member, newPassword)
                        newPassword = ""
                        Toast.makeText(context, "Lozinka promijenjena", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Lozinka mora imati barem 4 znaka", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Promijeni lozinku") }
        }

        Spacer(Modifier.height(16.dp))

        ProfileCard("IZGLED") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tamni način rada", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Switch(checked = isDarkMode, onCheckedChange = onThemeChanged)
            }
        }

        Spacer(Modifier.height(24.dp))

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
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("ODJAVI SE", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProfileCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
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
