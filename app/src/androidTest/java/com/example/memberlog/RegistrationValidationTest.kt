package com.example.memberlog

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrationValidationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun validRegistration_passesValidator_returnsToLogin() {
        // 1. Prebacivanje sučelja u način registracije
        composeTestRule.onNodeWithText("Novi korisnik? Registracija").performClick()
        composeTestRule.onNodeWithText("Kreiraj korisnički pristup").assertExists()

        // 2. Unos ispravnih podataka (prolaze kroz Validator.isValidEmail i isValidPassword)
        composeTestRule.onNodeWithText("Ime i prezime").performTextInput("Ivan Horvat")
        composeTestRule.onNodeWithText("E-mail").performTextInput("ivan@primjer.com")
        composeTestRule.onNodeWithText("Korisničko ime").performTextInput("ivanh")
        composeTestRule.onNodeWithText("Lozinka").performTextInput("sigurnaLozinka123")
        composeTestRule.onNodeWithText("Ponovi lozinku").performTextInput("sigurnaLozinka123")

        // 3. Pokretanje registracije
        composeTestRule.onNodeWithText("REGISTRIRAJ SE").performClick()

        // 4. Nakon uspješne validacije aplikacija se vraća na ekran prijave
        composeTestRule.onNodeWithText("Prijavi se za nastavak").assertExists()
    }
}
