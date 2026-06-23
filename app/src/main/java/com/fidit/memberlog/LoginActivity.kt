package com.fidit.memberlog

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fidit.memberlog.ui.screens.LoginScreen
import com.fidit.memberlog.ui.theme.MemberLogTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MemberLogTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLoginSuccess = { memberId, isAdmin ->
                            startActivity(
                                Intent(this, MainActivity::class.java)
                                    .putExtra(MainActivity.EXTRA_MEMBER_ID, memberId)
                                    .putExtra(MainActivity.EXTRA_IS_ADMIN, isAdmin)
                            )
                            finish()
                        }
                    )
                }
            }
        }
    }
}
