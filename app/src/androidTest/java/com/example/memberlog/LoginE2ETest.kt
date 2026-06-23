package com.example.memberlog

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun fullLoginWorkflow_opensMainActivity() {
        // Korisnik unosi podatke i pokreće prijavu
        composeTestRule.onNodeWithText("Korisničko ime").performTextInput("testKorisnik")
        composeTestRule.onNodeWithText("Lozinka").performTextInput("sigurnaLozinka123")
        composeTestRule.onNodeWithText("PRIJAVI SE").performClick()

        // Uspješan tok prijave otvara glavni ekran (MainActivity)
        intended(hasComponent(MainActivity::class.java.name))
    }
}
