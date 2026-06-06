package com.example.memberlog

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun toggleRegisterMode_changesBottomText() {
        composeTestRule.onNodeWithText("Prijavi se za nastavak").assertExists()
        composeTestRule.onNodeWithText("Novi korisnik? Registracija").performClick()
        composeTestRule.onNodeWithText("Kreiraj korisnički pristup").assertExists()
    }
}