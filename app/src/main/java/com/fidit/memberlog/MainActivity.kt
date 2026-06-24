package com.fidit.memberlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

            MemberLogTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAdmin) {
                        var showMyProfile by remember { mutableStateOf(false) }
                        if (showMyProfile) {
                            MemberScreen(
                                memberId = memberId,
                                isDarkMode = isDarkMode,
                                onThemeChanged = { isDarkMode = it },
                                onExit = { showMyProfile = false }
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
                            onThemeChanged = { isDarkMode = it }
                        )
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
