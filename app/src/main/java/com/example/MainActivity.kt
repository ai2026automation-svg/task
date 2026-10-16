package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ProfileDialog
import com.example.ui.components.SmartFinTopBar
import com.example.ui.dialogs.TaskDetailDialog
import com.example.ui.screens.AssignTaskTab
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TeamTab
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SmartFinTheme
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.NavigationTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartFinTheme {
                SmartFinApp()
            }
        }
    }
}

@Composable
fun SmartFinApp(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    if (!uiState.isLoggedIn) {
        LoginScreen(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onRequestCode = { email -> viewModel.requestLoginCode(email) },
            onVerifyCode = { email, code -> viewModel.verifyLoginCode(email, code) },
            onClearError = { viewModel.clearError() }
        )
    } else {
        // Back handler: return to dashboard if on sub-tab
        BackHandler(enabled = uiState.currentTab != NavigationTab.DASHBOARD) {
            viewModel.setTab(NavigationTab.DASHBOARD)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                SmartFinTopBar(
                    currentUser = uiState.currentUser,
                    onProfileClick = { viewModel.setShowProfileDialog(true) },
                    onSyncClick = { viewModel.syncDataNow() },
                    isSyncing = uiState.isSyncing
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = uiState.currentTab == NavigationTab.DASHBOARD,
                        onClick = { viewModel.setTab(NavigationTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Dashboard") },
                        label = { Text("Tasks", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFFDBEAFE)
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == NavigationTab.ASSIGN,
                        onClick = { viewModel.setTab(NavigationTab.ASSIGN) },
                        icon = { Icon(Icons.Default.PostAdd, contentDescription = "Assign") },
                        label = { Text("Assign", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFFDBEAFE)
                        ),
                        modifier = Modifier.testTag("nav_item_assign")
                    )

                    NavigationBarItem(
                        selected = uiState.currentTab == NavigationTab.TEAM,
                        onClick = { viewModel.setTab(NavigationTab.TEAM) },
                        icon = { Icon(Icons.Default.Groups, contentDescription = "Team") },
                        label = { Text("Team", fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,
                            selectedTextColor = PrimaryBlue,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = Color(0xFFDBEAFE)
                        ),
                        modifier = Modifier.testTag("nav_item_team")
                    )
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentTab) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            tasks = tasks,
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { viewModel.setSearchQuery(it) },
                            selectedFilter = uiState.selectedFilter,
                            onSelectFilter = { viewModel.setFilter(it) },
                            onTaskClick = { viewModel.selectTaskForDetail(it) },
                            onQuickStatusChange = { task, newStatus ->
                                viewModel.updateTaskStatus(task, newStatus)
                            }
                        )
                    }

                    NavigationTab.ASSIGN -> {
                        AssignTaskTab(
                            teamUsers = users,
                            onTaskCreated = { newTask ->
                                viewModel.createTask(newTask)
                                viewModel.setTab(NavigationTab.DASHBOARD)
                            }
                        )
                    }

                    NavigationTab.TEAM -> {
                        TeamTab(
                            users = users,
                            tasks = tasks,
                            onAddUser = { newUser -> viewModel.addUser(newUser) }
                        )
                    }
                }
            }

            // Task detail dialog modal
            uiState.selectedTaskForDetail?.let { task ->
                TaskDetailDialog(
                    task = task,
                    onDismiss = { viewModel.selectTaskForDetail(null) },
                    onSaveTask = { updated -> viewModel.updateTask(updated) },
                    onDeleteTask = { id -> viewModel.deleteTask(id) }
                )
            }

            // Profile dialog modal (with explicit logout)
            if (uiState.showProfileDialog) {
                ProfileDialog(
                    user = uiState.currentUser,
                    appsScriptUrl = uiState.appsScriptUrl,
                    onUpdateAppsScriptUrl = { url -> viewModel.updateAppsScriptUrl(url) },
                    onSyncNow = { viewModel.syncDataNow() },
                    onLogout = { viewModel.logout() },
                    onDismiss = { viewModel.setShowProfileDialog(false) }
                )
            }
        }
    }
}
