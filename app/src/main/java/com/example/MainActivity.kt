package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.TaskPreferences
import com.example.data.remote.AppsScriptClient
import com.example.data.repository.TaskRepository
import com.example.ui.components.TopBar
import com.example.ui.dialogs.AppsScriptConfigDialog
import com.example.ui.dialogs.CompleteTaskDialog
import com.example.ui.dialogs.DeleteTaskDialog
import com.example.ui.dialogs.EditTaskDialog
import com.example.ui.screens.AssignTaskTab
import com.example.ui.screens.DashboardTab
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OneTimeTasksTab
import com.example.ui.screens.RecurringTasksTab
import com.example.ui.screens.UsersTab
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.StatusDelay
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val preferences = TaskPreferences(applicationContext)
        val apiClient = AppsScriptClient { preferences.appsScriptUrl }
        val repository = TaskRepository(database, preferences, apiClient)

        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(repository, preferences, apiClient) as T
            }
        }

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                MainAppScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle back button when on sub-tabs
    BackHandler(enabled = uiState.isLoggedIn && uiState.currentTab != AppTab.DASHBOARD) {
        viewModel.setAppTab(AppTab.DASHBOARD)
    }

    // Auto-dismiss floating toast
    LaunchedEffect(uiState.toastMessage) {
        if (uiState.toastMessage != null) {
            delay(3200)
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (!uiState.isLoggedIn) {
            LoginScreen(
                appsScriptUrl = uiState.appsScriptUrl,
                email = uiState.loginEmail,
                code = uiState.loginCode,
                step = uiState.loginStep,
                msg = uiState.loginMsg,
                isError = uiState.loginIsError,
                isLoading = uiState.isLoggingIn,
                onUrlSubmit = { viewModel.submitAppsScriptUrlAndProceed(it) },
                onBackToUrl = { viewModel.backToUrl() },
                onEmailChange = { viewModel.setLoginEmail(it) },
                onCodeChange = { viewModel.setLoginCode(it) },
                onRequestCode = { viewModel.requestCode() },
                onVerifyCode = { viewModel.verifyCode() },
                onBackToEmail = { viewModel.backToEmail() },
                onOpenServerConfig = { viewModel.toggleServerConfigDialog(true) }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Brand Bar
                TopBar(
                    currentUser = uiState.currentUser,
                    isAppsScriptConnected = uiState.appsScriptUrl.isNotBlank(),
                    onOpenServerConfig = { viewModel.toggleServerConfigDialog(true) },
                    onLogout = { viewModel.logout() }
                )

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (uiState.currentTab) {
                        AppTab.DASHBOARD -> {
                            DashboardTab(
                                metrics = uiState.dashboardMetrics,
                                misMode = uiState.misMode,
                                scopedDoers = uiState.scopedDoers,
                                selectedType = uiState.dashTypeFilter,
                                selectedFreq = uiState.dashFreqFilter,
                                selectedDoer = uiState.dashDoerFilter,
                                fromDate = uiState.dashFromFilter,
                                toDate = uiState.dashToFilter,
                                detailsPage = uiState.detailsPage,
                                pageSize = uiState.detailsPageSize,
                                onFilterChange = { t, f, d, from, to ->
                                    viewModel.updateDashFilters(t, f, d, from, to)
                                },
                                onClearFilters = { viewModel.clearDashFilters() },
                                onMisModeChange = { viewModel.setMisMode(it) },
                                onPageChange = { viewModel.setDetailsPage(it) }
                            )
                        }
                        AppTab.RECURRING -> {
                            RecurringTasksTab(
                                tasks = uiState.recurringTasks,
                                currentUser = uiState.currentUser,
                                scopedDoers = uiState.scopedDoers,
                                search = uiState.recSearch,
                                doerFilter = uiState.recDoerFilter,
                                freqFilter = uiState.recFreqFilter,
                                statusFilter = uiState.recStatusFilter,
                                groupKey = uiState.recGroup,
                                horizonDays = uiState.recHorizon,
                                onFilterChange = { s, d, f, st, g, h ->
                                    viewModel.updateRecFilters(s, d, f, st, g, h)
                                },
                                onOpenComplete = { viewModel.openCompleteDialog(it) },
                                onOpenEdit = { viewModel.openEditDialog(it) },
                                onOpenDeleteSeries = { viewModel.openDeleteDialog(it) }
                            )
                        }
                        AppTab.ONETIME -> {
                            OneTimeTasksTab(
                                tasks = uiState.oneTimeTasks,
                                currentUser = uiState.currentUser,
                                scopedDoers = uiState.scopedDoers,
                                search = uiState.ottSearch,
                                doerFilter = uiState.ottDoerFilter,
                                priorityFilter = uiState.ottPriorityFilter,
                                statusFilter = uiState.ottStatusFilter,
                                groupKey = uiState.ottGroup,
                                horizonDays = uiState.ottHorizon,
                                onFilterChange = { s, d, p, st, g, h ->
                                    viewModel.updateOttFilters(s, d, p, st, g, h)
                                },
                                onOpenComplete = { viewModel.openCompleteDialog(it) },
                                onOpenEdit = { viewModel.openEditDialog(it) },
                                onOpenDelete = { viewModel.openDeleteDialog(it) }
                            )
                        }
                        AppTab.ASSIGN -> {
                            AssignTaskTab(
                                mode = uiState.assignMode,
                                permissions = uiState.permissions,
                                scopedDoers = uiState.scopedDoers,
                                rTask = uiState.rTask,
                                rDoer = uiState.rDoer,
                                rDesignation = uiState.rDesignation,
                                rFrequency = uiState.rFrequency,
                                rStartDate = uiState.rStartDate,
                                rOffDays = uiState.rOffDays,
                                rMsg = uiState.rMsg,
                                rIsError = uiState.rIsError,
                                isAssigningRecurring = uiState.isAssigningRecurring,
                                oDetails = uiState.oDetails,
                                oDoer = uiState.oDoer,
                                oPriority = uiState.oPriority,
                                oDueDate = uiState.oDueDate,
                                oAttachFile = uiState.oAttachFile,
                                oMsg = uiState.oMsg,
                                oIsError = uiState.oIsError,
                                isAssigningOneTime = uiState.isAssigningOneTime,
                                onModeChange = { viewModel.setAssignMode(it) },
                                onUpdateRecurring = { t, d, f, s -> viewModel.updateRecurringForm(t, d, f, s) },
                                onToggleOffDay = { viewModel.toggleOffDay(it) },
                                onSubmitRecurring = { viewModel.submitRecurringTask() },
                                onUpdateOneTime = { d, doer, p, due, f -> viewModel.updateOneTimeForm(d, doer, p, due, f) },
                                onSubmitOneTime = { viewModel.submitOneTimeTask() }
                            )
                        }
                        AppTab.USERS -> {
                            UsersTab(
                                users = uiState.usersList,
                                name = uiState.uName,
                                designation = uiState.uDesignation,
                                email = uiState.uEmail,
                                department = uiState.uDepartment,
                                role = uiState.uRole,
                                msg = uiState.uMsg,
                                isError = uiState.uIsError,
                                settings = uiState.settings,
                                settingsMsg = uiState.settingsMsg,
                                onUserFormChange = { n, des, e, dep, r -> viewModel.updateUserForm(n, des, e, dep, r) },
                                onSubmitUser = { viewModel.submitUser() },
                                onChangeRole = { row, r -> viewModel.changeUserRole(row, r) },
                                onDeleteUser = { row -> viewModel.deleteUser(row) },
                                onUpdateSettings = { a, c, p, h, em -> viewModel.updateSettings(a, c, p, h, em) },
                                onSaveSettings = { viewModel.saveSettings() }
                            )
                        }
                    }
                }

                // Mobile Bottom Navigation Bar with icons (Home for dashboard)
                AppBottomNavigationBar(
                    currentTab = uiState.currentTab,
                    canAssign = uiState.permissions.assignRecurring || uiState.permissions.assignOneTime,
                    canManageUsers = uiState.permissions.manageUsers,
                    onTabSelected = { viewModel.setAppTab(it) }
                )
            }
        }

        // Modals
        uiState.activeCompleteTask?.let { task ->
            CompleteTaskDialog(
                task = task,
                onDismiss = { viewModel.dismissCompleteDialog() },
                onSave = { remarks, file -> viewModel.completeTask(remarks, file) }
            )
        }

        uiState.activeEditTask?.let { task ->
            EditTaskDialog(
                task = task,
                scopedDoers = uiState.scopedDoers,
                onDismiss = { viewModel.dismissEditDialog() },
                onSave = { title, doer, priority, dueIso, file, applyToSeries ->
                    viewModel.saveEditedTask(title, doer, priority, dueIso, file, applyToSeries)
                }
            )
        }

        uiState.activeDeleteTask?.let { task ->
            DeleteTaskDialog(
                task = task,
                onDismiss = { viewModel.dismissDeleteDialog() },
                onConfirmDelete = { viewModel.confirmDeleteTask() }
            )
        }

        if (uiState.showServerConfigDialog) {
            AppsScriptConfigDialog(
                currentUrl = uiState.appsScriptUrl,
                connectionStatus = uiState.connectionStatus,
                isTesting = uiState.isTestingConnection,
                onDismiss = { viewModel.toggleServerConfigDialog(false) },
                onUrlChange = { viewModel.updateAppsScriptUrl(it) },
                onTestConnection = { viewModel.testAppsScriptConnection() }
            )
        }

        // Toast Notification Overlay
        AnimatedVisibility(
            visible = uiState.toastMessage != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            uiState.toastMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyPrimary,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .border(androidx.compose.foundation.BorderStroke(3.dp, OrangePrimary), RoundedCornerShape(10.dp))
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentTab: AppTab,
    canAssign: Boolean,
    canManageUsers: Boolean,
    onTabSelected: (AppTab) -> Unit
) {
    Surface(
        color = NavyPrimary,
        shadowElevation = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Subtle top gold accent separator line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(GoldAccent.copy(alpha = 0.45f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Dashboard Tab with Home Icon as requested
                BottomNavItem(
                    tab = AppTab.DASHBOARD,
                    label = "Home",
                    icon = Icons.Default.Home,
                    isActive = currentTab == AppTab.DASHBOARD,
                    onClick = { onTabSelected(AppTab.DASHBOARD) }
                )
                BottomNavItem(
                    tab = AppTab.RECURRING,
                    label = "Recurring",
                    icon = Icons.Default.Repeat,
                    isActive = currentTab == AppTab.RECURRING,
                    onClick = { onTabSelected(AppTab.RECURRING) }
                )
                BottomNavItem(
                    tab = AppTab.ONETIME,
                    label = "One-Time",
                    icon = Icons.Default.AssignmentTurnedIn,
                    isActive = currentTab == AppTab.ONETIME,
                    onClick = { onTabSelected(AppTab.ONETIME) }
                )
                if (canAssign) {
                    BottomNavItem(
                        tab = AppTab.ASSIGN,
                        label = "Assign",
                        icon = Icons.Default.AddTask,
                        isActive = currentTab == AppTab.ASSIGN,
                        onClick = { onTabSelected(AppTab.ASSIGN) }
                    )
                }
                if (canManageUsers) {
                    BottomNavItem(
                        tab = AppTab.USERS,
                        label = "Users",
                        icon = Icons.Default.People,
                        isActive = currentTab == AppTab.USERS,
                        onClick = { onTabSelected(AppTab.USERS) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    tab: AppTab,
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val activeColor = OrangePrimary
    val inactiveColor = Color(0xFF94A3B8)
    val color = if (isActive) activeColor else inactiveColor

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
            .testTag("tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) NavySecondary else Color.Transparent)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = color,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
