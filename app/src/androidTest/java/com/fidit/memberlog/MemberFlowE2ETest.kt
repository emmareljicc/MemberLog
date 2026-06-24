package com.fidit.memberlog

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MemberFlowE2ETest {

    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun member_recordsOwnPayment_returnsToHome() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(ctx, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_IS_ADMIN, false)
            .putExtra(MainActivity.EXTRA_MEMBER_ID, 4)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        ActivityScenario.launch<MainActivity>(intent).use {
            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithText("Pozdrav,").fetchSemanticsNodes().isNotEmpty()
            }

            compose.onNodeWithContentDescription("Plaćanje").performClick()
            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithText("Spremi uplatu").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Spremi uplatu").performClick()

            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithText("Pozdrav,").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Pozdrav,").assertIsDisplayed()
        }
    }
}
