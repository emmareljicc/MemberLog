package com.fidit.memberlog.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.MembersViewModel
import com.fidit.memberlog.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    isAdmin: Boolean,
    viewModel: MembersViewModel = viewModel(),
    activitiesViewModel: ActivitiesViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }
    var selectedEventId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    var showRoles by remember { mutableStateOf(false) }
    var showAccounts by remember { mutableStateOf(false) }
    var showReports by remember { mutableStateOf(false) }
    var showExchangeRates by remember { mutableStateOf(false) }

    // Pomoćna funkcija koja zatvara sve pod-ekrane odjednom
    // Ovo sprječava "Assigned value is never read" jer se poziva kroz funkciju!
    fun resetSubScreens() {
        selectedMemberId = null
        selectedEventId = null
        if (showRoles) showRoles = false
        if (showAccounts) showAccounts = false
        if (showReports) showReports = false
        if (showExchangeRates) showExchangeRates = false
    }

    val members by viewModel.members.collectAsState()
    val owedByMember by viewModel.owedByMember.collectAsState()
    val rolesById by viewModel.rolesById.collectAsState()
    val events by activitiesViewModel.events.collectAsState()
    val roles = rolesById.values.sortedBy { it.name }
    val selectedMember = members.find { it.id == selectedMemberId }

    val today = DateUtils.todayIso()
    val soonLimit = LocalDate.now().plusDays(14).toString()
    val debtorCount = owedByMember.values.count { it > 0.0 }
    val upcomingSoon = events.count { it.date in today..soonLimit }

    val destinations = listOf(
        BottomDest(Icons.Filled.Dashboard, "Nadzorna ploča"),
        BottomDest(Icons.AutoMirrored.Filled.List, "Članovi", debtorCount),
        BottomDest(Icons.Filled.Event, "Aktivnosti", upcomingSoon),
        BottomDest(Icons.Filled.Settings, "Postavke")
    )

    Scaffold(
        bottomBar = {
            MemberLogBottomBar(
                destinations = destinations,
                selectedIndex = selectedTab,
                onSelect = { tabIndex ->
                    resetSubScreens()
                    selectedTab = tabIndex
                },
                onAddClick = {
                    if (isAdmin) {
                        resetSubScreens()
                        showAddDialog = true
                        selectedTab = 1
                    } else {
                        Toast.makeText(context, "Samo administrator može dodavati", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                showRoles -> RolesScreen(isAdmin = isAdmin, onBack = { showRoles = false })
                showAccounts -> AccountsScreen(onBack = { showAccounts = false })
                showReports -> ReportsScreen(onBack = { showReports = false })
                showExchangeRates -> ExchangeRateScreen(onBack = { showExchangeRates = false })
                selectedTab == 0 -> DashboardScreen(
                    rolesById = rolesById,
                    onMemberClick = { id ->
                        selectedMemberId = id
                        selectedTab = 1
                    }
                )
                selectedTab == 1 -> AnimatedContent(targetState = selectedMember, label = "members") { m ->
                    if (m == null) {
                        MembersListScreen(
                            members = members,
                            owedByMember = owedByMember,
                            rolesById = rolesById,
                            onMemberClick = { selectedMemberId = it.id }
                        )
                    } else {
                        MemberDetailsScreen(
                            member = m,
                            roles = roles,
                            isAdmin = isAdmin,
                            onBack = { selectedMemberId = null },
                            onUpdate = { viewModel.updateMember(it) },
                            onDelete = {
                                viewModel.deleteMember(m)
                                selectedMemberId = null
                            }
                        )
                    }
                }
                selectedTab == 2 -> AnimatedContent(targetState = selectedEventId, label = "events") { id ->
                    if (id == null) {
                        ActivitiesScreen(isAdmin = isAdmin, onEventClick = { selectedEventId = it })
                    } else {
                        EventDetailScreen(
                            eventId = id,
                            members = members,
                            rolesById = rolesById,
                            isAdmin = isAdmin,
                            onBack = { selectedEventId = null }
                        )
                    }
                }
                else -> SettingsScreen(
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged,
                    isAdmin = isAdmin,
                    onManageRoles = { showRoles = true },
                    onManageAccounts = { showAccounts = true },
                    onOpenReports = { showReports = true },
                    onNavigateToExchangeRates = { showExchangeRates = true }
                )
            }
        }

        if (showAddDialog) {
            MemberFormDialog(
                title = "Dodaj novog člana",
                roles = roles,
                confirmLabel = "Dodaj",
                onDismiss = { showAddDialog = false },
                onSubmit = { name, roleId, email, phone, feeOverride, status, address, notes, photoPath ->
                    viewModel.addMember(name, roleId, email, phone, feeOverride, status, address, notes, photoPath)
                    showAddDialog = false
                }
            )
        }
    }
}