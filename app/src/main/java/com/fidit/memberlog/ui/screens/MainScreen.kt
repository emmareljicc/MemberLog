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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fidit.memberlog.model.Member
import com.fidit.memberlog.ui.theme.MemberLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val membersList = remember {
        mutableStateListOf(
            Member(1, "Ivan Horvat", "Voditelj", "01.03.2021.", true, "ivan.horvat@email.com", "091/123-4567"),
            Member(2, "Marko Marić", "Tajnik", "15.08.2022.", true, "marko.maric@email.com", "092/876-5432"),
            Member(3, "Ana Anić", "Blagajnik", "10.10.2022.", false, "ana.anic@email.com", "095/555-4443"),
            Member(4, "Petra Petrović", "Član", "05.02.2024.", true, "petra.petrovic@email.com", "098/987-6543"),
            Member(5, "Josip Jurić", "Član", "20.08.2023.", false, "josip.juric@email.com", "097/111-2222")
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                NavigationBarItem(
                    selected = selectedTab == 0 && selectedMember == null,
                    onClick = {
                        selectedTab = 0
                        selectedMember = null
                    },
                    label = { Text("Članovi") },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        selectedMember = null
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
                        members = membersList,
                        onMemberClick = { selectedMember = it }
                    )
                } else {
                    MemberDetailsScreen(
                        member = selectedMember!!,
                        onBack = { selectedMember = null }
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
            AddMemberDialog(
                onDismiss = { showAddDialog = false },
                onAddMember = { name, role, isPaid, email, phone ->
                    val newId = (membersList.maxOfOrNull { it.id } ?: 0) + 1
                    membersList.add(
                        Member(newId, name, role, "Danas", isPaid, email, phone)
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MemberLogTheme {
        MainScreen(isDarkMode = false, onThemeChanged = {})
    }
}
