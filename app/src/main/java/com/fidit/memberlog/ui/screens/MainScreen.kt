package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fidit.memberlog.ui.MembersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    viewModel: MembersViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRoles by remember { mutableStateOf(false) }

    val members by viewModel.members.collectAsState()
    val owedByMember by viewModel.owedByMember.collectAsState()
    val rolesById by viewModel.rolesById.collectAsState()
    val roles = rolesById.values.sortedBy { it.name }
    val selectedMember = members.find { it.id == selectedMemberId }

    Scaffold(
        bottomBar = {
            MemberLogBottomBar(
                selectedTab = selectedTab,
                onMembersClick = {
                    selectedTab = 0
                    selectedMemberId = null
                    showRoles = false
                },
                onSettingsClick = {
                    selectedTab = 1
                    selectedMemberId = null
                    showRoles = false
                },
                onAddClick = {
                    selectedTab = 0
                    selectedMemberId = null
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
            } else if (selectedTab == 0) {
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
            } else {
                SettingsScreen(
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
