package com.fidit.memberlog.ui.screens

import android.widget.Toast
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
                onSelect = {
                    selectedTab = it
                    selectedMemberId = null
                    selectedEventId = null
                    showRoles = false
                    showAccounts = false
                },
                onAddClick = {
                    if (isAdmin) {
                        selectedTab = 1
                        selectedMemberId = null
                        selectedEventId = null
                        showRoles = false
                        showAccounts = false
                        showAddDialog = true
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
                selectedTab == 0 -> DashboardScreen(
                    rolesById = rolesById,
                    onMemberClick = { id ->
                        selectedMemberId = id
                        selectedTab = 1
                    }
                )
                selectedTab == 1 -> {
                    if (selectedMember == null) {
                        MembersListScreen(
                            members = members,
                            owedByMember = owedByMember,
                            rolesById = rolesById,
                            onMemberClick = { selectedMemberId = it.id }
                        )
                    } else {
                        MemberDetailsScreen(
                            member = selectedMember,
                            roles = roles,
                            isAdmin = isAdmin,
                            onBack = { selectedMemberId = null },
                            onUpdate = { viewModel.updateMember(it) },
                            onDelete = {
                                viewModel.deleteMember(selectedMember)
                                selectedMemberId = null
                            }
                        )
                    }
                }
                selectedTab == 2 -> {
                    val eventId = selectedEventId
                    if (eventId == null) {
                        ActivitiesScreen(isAdmin = isAdmin, onEventClick = { selectedEventId = it })
                    } else {
                        EventDetailScreen(
                            eventId = eventId,
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
                    onManageAccounts = { showAccounts = true }
                )
            }
        }

        if (showAddDialog) {
            MemberFormDialog(
                title = "Dodaj novog člana",
                roles = roles,
                confirmLabel = "Dodaj",
                onDismiss = { showAddDialog = false },
                onSubmit = { name, roleId, email, phone, feeOverride ->
                    viewModel.addMember(name, roleId, email, phone, feeOverride)
                    showAddDialog = false
                }
            )
        }
    }
}
