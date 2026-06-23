package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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

@Composable
fun MemberScreen(
    memberId: Int,
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    sessionViewModel: MemberSessionViewModel = viewModel(),
    feeViewModel: FeeViewModel = viewModel(),
    activitiesViewModel: ActivitiesViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val member by sessionViewModel.member(memberId).collectAsState(initial = null)

    val destinations = listOf(
        BottomDest(Icons.Filled.Home, "Početna"),
        BottomDest(Icons.Filled.Payments, "Članarina"),
        BottomDest(Icons.Filled.Event, "Događaji"),
        BottomDest(Icons.Filled.Person, "Profil")
    )

    Scaffold(
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> MemberHomeScreen(m, feeViewModel, activitiesViewModel, onOpenDues = { selectedTab = 1 })
                    1 -> MemberDuesScreen(m, feeViewModel)
                    2 -> MemberEventsScreen(m, activitiesViewModel)
                    else -> MemberProfileScreen(m, isDarkMode, onThemeChanged, sessionViewModel)
                }
            }
        }
    }
}
