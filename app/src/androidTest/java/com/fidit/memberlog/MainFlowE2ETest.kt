package com.fidit.memberlog

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainFlowE2ETest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun addMember_recordPayment_reflectedOnDashboard() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val name = "AAA E2E ${System.currentTimeMillis()}"
        val intent = Intent(ctx, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_ROLE, "ADMIN")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            compose.onNodeWithContentDescription("Dodaj člana").performClick()

            compose.onNodeWithText("Ime i Prezime").performTextInput(name)
            compose.onNodeWithText("E-mail adresa").performTextInput("e2e@test.com")
            compose.onNodeWithText("Dodaj").performClick()

            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithText(name).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(name).assertIsDisplayed()

            compose.onNodeWithText(name).performClick()

            compose.onNodeWithText("Zabilježi uplatu").performScrollTo().performClick()
            compose.onNodeWithText("Spremi").performClick()

            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithText("Sve podmireno").fetchSemanticsNodes().isNotEmpty()
            }

            compose.onNodeWithContentDescription("Nadzorna ploča").performClick()
            compose.onNodeWithText("Plaćeno ovaj mjesec").assertIsDisplayed()
        }
    }
}
