package com.fidit.memberlog.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fidit.memberlog.MainActivity
import com.fidit.memberlog.ui.theme.MemberLogTheme
import com.fidit.memberlog.util.Validator

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var isRegisterMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatedPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (isRegisterMode) "Registracija člana" else "MemberLog",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (isRegisterMode) "Kreiraj korisnički pristup" else "Prijavi se za nastavak",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isRegisterMode) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Ime i prezime") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Korisničko ime") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Lozinka") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (isRegisterMode) {
                    OutlinedTextField(
                        value = repeatedPassword,
                        onValueChange = { repeatedPassword = it },
                        label = { Text("Ponovi lozinku") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (isRegisterMode) {
                    when {
                        fullName.isBlank() || email.isBlank() || username.isBlank() || password.isBlank() ->
                            Toast.makeText(context, "Popunite sva polja", Toast.LENGTH_SHORT).show()
                        !Validator.isValidEmail(email) ->
                            Toast.makeText(context, "Neispravan e-mail", Toast.LENGTH_SHORT).show()
                        !Validator.isValidPassword(password) ->
                            Toast.makeText(context, "Lozinka mora biti duža", Toast.LENGTH_SHORT).show()
                        password != repeatedPassword ->
                            Toast.makeText(context, "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
                        else -> {
                            Toast.makeText(context, "Registracija je uspješna", Toast.LENGTH_SHORT).show()
                            isRegisterMode = false
                        }
                    }
                } else if (username.isNotBlank() && password.isNotBlank()) {
                    context.startActivity(Intent(context, MainActivity::class.java))
                } else {
                    Toast.makeText(context, "Unesite podatke", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (isRegisterMode) "REGISTRIRAJ SE" else "PRIJAVI SE", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = { isRegisterMode = !isRegisterMode },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isRegisterMode) "Već imaš račun? Prijava" else "Novi korisnik? Registracija")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    MemberLogTheme {
        LoginScreen()
    }
}
