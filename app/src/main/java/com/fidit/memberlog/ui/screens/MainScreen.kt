package com.fidit.memberlog.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.ActivitiesViewModel
import com.fidit.memberlog.ui.MembersViewModel
import com.fidit.memberlog.ui.RolesViewModel
import com.fidit.memberlog.ui.components.AddActionSheet
import com.fidit.memberlog.util.DateUtils
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    isAdmin: Boolean,
    memberId: Int,
    onOpenMyProfile: () -> Unit,
    viewModel: MembersViewModel = viewModel(),
    activitiesViewModel: ActivitiesViewModel = viewModel(),
    rolesViewModel: RolesViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }
    var selectedEventId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showAddEvent by remember { mutableStateOf(false) }
    var showAddRole by remember { mutableStateOf(false) }

    var showRoles by remember { mutableStateOf(false) }
    var showReports by remember { mutableStateOf(false) }

    fun resetSubScreens() {
        selectedMemberId = null
        selectedEventId = null
        showRoles = false
        showReports = false
    }

    val members by viewModel.members.collectAsState()
    val owedByMember by viewModel.owedByMember.collectAsState()
    val rolesById by viewModel.rolesById.collectAsState()
    val events by activitiesViewModel.events.collectAsState()
    val roles = rolesById.values.sortedBy { it.name }
    val currentUser = members?.find { it.id == memberId }
    val currentUserName = currentUser?.name ?: ""
    val currentUserRole = currentUser?.let { rolesById[it.roleId]?.name } ?: ""

    val today = DateUtils.todayIso()
    val soonLimit = LocalDate.now().plusDays(14).toString()
    val debtorCount = owedByMember.orEmpty().values.count { it > 0.0 }
    val upcomingSoon = events.orEmpty().count { it.date in today..soonLimit }

    val destinations = listOf(
        BottomDest(Icons.Outlined.GridView, "Nadzorna ploča"),
        BottomDest(Icons.Outlined.Group, "Članovi", debtorCount),
        BottomDest(Icons.Outlined.Event, "Aktivnosti", upcomingSoon),
        BottomDest(Icons.Outlined.Settings, "Postavke")
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
                        showAddSheet = true
                    } else {
                        Toast.makeText(context, "Samo administrator može dodavati", Toast.LENGTH_SHORT).show()
                    }
                },
                addExpanded = showAddSheet
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                showAddDialog -> MemberFormScreen(
                    mode = MemberFormMode.ADMIN_CREATE,
                    roles = roles,
                    onBack = { showAddDialog = false },
                    onSubmit = { name, roleId, email, phone, feeOverride, status, address, notes, photoPath, password ->
                        viewModel.addMember(name, roleId, email, phone, feeOverride, status, address, notes, photoPath, password)
                        showAddDialog = false
                    }
                )
                showAddEvent -> EventFormScreen(
                    title = "Novo događanje",
                    onBack = { showAddEvent = false },
                    onSubmit = { t, d, l, n -> activitiesViewModel.addEvent(t, d, l, n); showAddEvent = false }
                )
                showAddRole -> RoleFormScreen(
                    title = "Nova uloga",
                    onBack = { showAddRole = false },
                    onSubmit = { name, color, grantsAdmin -> rolesViewModel.addRole(name, color, grantsAdmin); showAddRole = false }
                )
                showRoles -> RolesScreen(isAdmin = isAdmin, onBack = { showRoles = false })
                showReports -> ReportsScreen(onBack = { showReports = false })

                selectedTab == 0 -> DashboardScreen(
                    rolesById = rolesById,
                    onMemberClick = { id ->
                        selectedMemberId = id
                        selectedTab = 1
                    },
                    onEventClick = { id ->
                        selectedEventId = id
                        selectedTab = 2
                    }
                )

                selectedTab == 1 -> run {
                    val selectedMember = members?.find { it.id == selectedMemberId }
                    if (selectedMember == null) {
                        MembersListScreen(
                            members = members,
                            owedByMember = owedByMember,
                            rolesById = rolesById,
                            onMemberClick = { clickedMember -> selectedMemberId = clickedMember.id }
                        )
                    } else {
                        MemberDetailsScreen(
                            member = selectedMember,
                            roles = roles,
                            isAdmin = isAdmin,
                            onBack = { selectedMemberId = null },
                            onUpdate = { updatedMember -> viewModel.updateMember(updatedMember) },
                            onDelete = {
                                viewModel.deleteMember(selectedMember)
                                selectedMemberId = null
                            }
                        )
                    }
                }

                selectedTab == 2 -> run {
                    val id = selectedEventId
                    if (id == null) {
                        ActivitiesScreen(isAdmin = isAdmin, onEventClick = { selectedEventId = it })
                    } else {
                        EventDetailScreen(
                            eventId = id,
                            members = members.orEmpty(),
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
                    userName = currentUserName,
                    userRole = currentUserRole,
                    onOpenMyProfile = onOpenMyProfile,
                    onManageRoles = { showRoles = true },
                    onOpenReports = { showReports = true }
                )
            }
        }

        if (showAddSheet) {
            AddActionSheet(
                onDismiss = { showAddSheet = false },
                onAddMember = {
                    showAddSheet = false
                    resetSubScreens()
                    selectedTab = 1
                    showAddDialog = true
                },
                onAddEvent = {
                    showAddSheet = false
                    showAddEvent = true
                },
                onAddRole = {
                    showAddSheet = false
                    showAddRole = true
                }
            )
        }
    }
}
