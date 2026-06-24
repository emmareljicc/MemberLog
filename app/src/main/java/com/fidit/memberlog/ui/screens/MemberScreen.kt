package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.FeeViewModel
import com.fidit.memberlog.ui.MemberSessionViewModel
import com.fidit.memberlog.ui.components.BackButton
import com.fidit.memberlog.ui.components.LoadingSpinner
import com.fidit.memberlog.ui.theme.Dimens

@Composable
fun MemberScreen(
    memberId: Int,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onExit: (() -> Unit)? = null,
    onNavigateToExchangeRates: () -> Unit,
    sessionViewModel: MemberSessionViewModel = viewModel(),
    feeViewModel: FeeViewModel = viewModel(),
    activitiesViewModel: ActivitiesViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val member by sessionViewModel.member(memberId).collectAsState(initial = null)

    val destinations = listOf(
        BottomDest(Icons.Outlined.Home, "Početna"),
        BottomDest(Icons.Outlined.Event, "Događanja"),
        BottomDest(Icons.Outlined.Person, "Profil")
    )

    Scaffold(
        topBar = {
            if (onExit != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Dimens.screenPadding, end = Dimens.screenPadding, top = Dimens.gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackButton(onExit)
                    Spacer(Modifier.width(Dimens.gap))
                    Text("Moj profil", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        bottomBar = {
            MemberLogBottomBar(
                destinations = destinations,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val m = member
            if (m == null) {
                LoadingSpinner()
            } else {
                when (selectedTab) {
                    0 -> MemberHomeScreen(m, feeViewModel, activitiesViewModel)
                    1 -> MemberEventsScreen(m, activitiesViewModel)
                    else -> MemberProfileScreen(
                        member = m,
                        isDarkMode = isDarkMode,
                        onThemeChanged = onThemeChanged,
                        sessionViewModel = sessionViewModel,
                        onNavigateToExchangeRates = onNavigateToExchangeRates
                    )
                }
            }
        }
    }
}