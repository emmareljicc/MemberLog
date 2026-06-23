package com.fidit.memberlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    val members by viewModel.members.collectAsState()
    val selectedMember = members.find { it.id == selectedMemberId }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                NavigationBarItem(
                    selected = selectedTab == 0 && selectedMember == null,
                    onClick = {
                        selectedTab = 0
                        selectedMemberId = null
                    },
                    label = { Text("Članovi") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        selectedMemberId = null
                    },
                    label = { Text("Postavke") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 && selectedMember == null) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Novi Član") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedTab == 0) {
                if (selectedMember == null) {
                    MembersListScreen(
                        members = members,
                        onMemberClick = { selectedMemberId = it.id }
                    )
                } else {
                    MemberDetailsScreen(
                        member = selectedMember,
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
                    onThemeChanged = onThemeChanged
                )
            }
        }

        if (showAddDialog) {
            MemberFormDialog(
                title = "Dodaj novog člana",
                confirmLabel = "Dodaj",
                onDismiss = { showAddDialog = false },
                onSubmit = { name, role, isPaid, email, phone ->
                    viewModel.addMember(name, role, isPaid, email, phone)
                    showAddDialog = false
                }
            )
        }
    }
}
