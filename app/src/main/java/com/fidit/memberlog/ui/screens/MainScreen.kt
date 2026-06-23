package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    viewModel: MembersViewModel = viewModel(),
    activitiesViewModel: ActivitiesViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }
    var selectedEventId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRoles by remember { mutableStateOf(false) }

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
                },
                onAddClick = {
                    selectedTab = 1
                    selectedMemberId = null
                    selectedEventId = null
                    showRoles = false
                    showAddDialog = true
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showRoles) {
                RolesScreen(onBack = { showRoles = false })
            } else when (selectedTab) {
                0 -> DashboardScreen(
                    rolesById = rolesById,
                    onMemberClick = { id ->
                        selectedMemberId = id
                        selectedTab = 1
                    }
                )
                1 -> {
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
                            onBack = { selectedMemberId = null },
                            onUpdate = { viewModel.updateMember(it) },
                            onDelete = {
                                viewModel.deleteMember(selectedMember)
                                selectedMemberId = null
                            }
                        )
                    }
                }
                2 -> {
                    val eventId = selectedEventId
                    if (eventId == null) {
                        ActivitiesScreen(onEventClick = { selectedEventId = it })
                    } else {
                        EventDetailScreen(
                            eventId = eventId,
                            members = members,
                            rolesById = rolesById,
                            onBack = { selectedEventId = null }
                        )
                    }
                }
                else -> SettingsScreen(
                    isDarkMode = isDarkMode,
                    onThemeChanged = onThemeChanged,
                    onManageRoles = { showRoles = true }
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
