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
class LoginE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @Test
    fun fullLoginWorkflow_success() {
        composeTestRule.onNodeWithText("Korisničko ime").performTextInput("testKorisnik")
        composeTestRule.onNodeWithText("Lozinka").performTextInput("sigurnaLozinka123")
        composeTestRule.onNodeWithText("PRIJAVI SE").performClick()
    }
}