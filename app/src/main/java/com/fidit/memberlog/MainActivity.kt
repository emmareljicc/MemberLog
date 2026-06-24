package com.fidit.memberlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.MembersViewModel
import com.fidit.memberlog.ui.screens.ExchangeRateScreen
import com.fidit.memberlog.ui.screens.MainScreen
import com.fidit.memberlog.ui.screens.MemberScreen
import com.fidit.memberlog.ui.theme.MemberLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isAdmin = intent.getBooleanExtra(EXTRA_IS_ADMIN, false)
        val memberId = intent.getIntExtra(EXTRA_MEMBER_ID, -1)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var currentScreen by remember { mutableStateOf("home") }

            val membersViewModel: MembersViewModel = viewModel()
            val owedByMember by membersViewModel.owedByMember.collectAsState()
            val debt = owedByMember?.get(memberId) ?: 0.0

            MemberLogTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentScreen == "exchange_rates") {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            ExchangeRateScreen(
                                onBack = { currentScreen = "home" },
                                memberOwedEur = debt
                            )
                        }
                    } else {
                        if (isAdmin) {
                            var showMyProfile by remember { mutableStateOf(false) }
                            if (showMyProfile) {
                                MemberScreen(
                                    memberId = memberId,
                                    isDarkMode = isDarkMode,
                                    onThemeChanged = { isDarkMode = it },
                                    onExit = { showMyProfile = false },
                                    onNavigateToExchangeRates = { currentScreen = "exchange_rates" }
                                )
                            } else {
                                MainScreen(
                                    isDarkMode = isDarkMode,
                                    onThemeChanged = { isDarkMode = it },
                                    isAdmin = true,
                                    memberId = memberId,
                                    onOpenMyProfile = { showMyProfile = true }
                                )
                            }
                        } else {
                            MemberScreen(
                                memberId = memberId,
                                isDarkMode = isDarkMode,
                                onThemeChanged = { isDarkMode = it },
                                onNavigateToExchangeRates = { currentScreen = "exchange_rates" }
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_MEMBER_ID = "member_id"
        const val EXTRA_IS_ADMIN = "is_admin"
    }
}