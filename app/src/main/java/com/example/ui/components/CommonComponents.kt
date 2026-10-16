package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Priority
import com.example.data.model.TaskStatus
import com.example.data.model.User
import com.example.ui.theme.*

/**
 * Standard OutlinedTextField colors with 100% opacity, high contrast,
 * eliminating the low opacity / washed out text issue reported by user.
 */
@Composable
fun appOutlinedTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextDark,
        unfocusedTextColor = TextDark,
        disabledTextColor = Color(0xFF64748B),
        errorTextColor = Color(0xFFDC2626),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color(0xFFF1F5F9),
        cursorColor = PrimaryBlue,
        errorCursorColor = Color(0xFFDC2626),
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = BorderNormal,
        disabledBorderColor = BorderSubtle,
        errorBorderColor = Color(0xFFDC2626),
        focusedLabelColor = PrimaryBlue,
        unfocusedLabelColor = TextMuted,
        disabledLabelColor = Color(0xFF94A3B8),
        errorLabelColor = Color(0xFFDC2626),
        focusedPlaceholderColor = TextMuted,
        unfocusedPlaceholderColor = TextMuted,
        focusedLeadingIconColor = PrimaryBlue,
        unfocusedLeadingIconColor = TextMuted,
        focusedTrailingIconColor = PrimaryBlue,
        unfocusedTrailingIconColor = TextMuted
    )
}

/**
 * Reusable full-opacity TextField to prevent any faint text across screens.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    testTag: String = "app_text_field"
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.testTag(testTag),
        label = label?.let { { Text(it, fontWeight = FontWeight.Medium) } },
        placeholder = placeholder?.let { { Text(it, color = TextMuted) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, tint = if (value.isNotEmpty()) PrimaryBlue else TextMuted) } },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        readOnly = readOnly,
        enabled = enabled,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = TextStyle(
            color = TextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal
        ),
        shape = RoundedCornerShape(10.dp),
        colors = appOutlinedTextFieldColors()
    )
}

/**
 * Compact, non-cluttered TopBar replacing bulky banners with a clean profile button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartFinTopBar(
    currentUser: User?,
    onProfileClick: () -> Unit,
    onSyncClick: () -> Unit,
    isSyncing: Boolean = false
) {
    Surface(
        color = PrimaryNavy,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "SmartFin Logo",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "SmartFin",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = "Task Operations",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right: Sync Status & Profile Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sync button
                IconButton(
                    onClick = onSyncClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("topbar_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AccentCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "Sync",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Profile Avatar Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentBlue)
                        .clickable(onClick = onProfileClick)
                        .testTag("topbar_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = currentUser?.name?.take(1)?.uppercase()
                        ?: currentUser?.email?.take(1)?.uppercase() ?: "U"
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Profile Dialog displaying user info, role, Apps Script sync endpoint, and explicit Logout.
 */
@Composable
fun ProfileDialog(
    user: User?,
    appsScriptUrl: String,
    onUpdateAppsScriptUrl: (String) -> Unit,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    var editedUrl by remember(appsScriptUrl) { mutableStateOf(appsScriptUrl) }
    var showUrlEditor by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("profile_dialog_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "User Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = user?.name?.take(1)?.uppercase() ?: "U"
                    Text(
                        text = initial,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name & Email
                Text(
                    text = user?.name ?: "User",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = user?.email ?: "",
                    fontSize = 13.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role & Department Tags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFDBEAFE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = user?.role ?: "Member",
                            color = Color(0xFF1E40AF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = user?.department ?: "Finance",
                            color = Color(0xFF334155),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = BorderSubtle)
                Spacer(modifier = Modifier.height(14.dp))

                // Apps Script Sync Endpoint Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SyncAlt,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Apps Script Backend",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }
                    TextButton(
                        onClick = { showUrlEditor = !showUrlEditor },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (showUrlEditor) "Hide" else "Config",
                            fontSize = 12.sp,
                            color = AccentBlue
                        )
                    }
                }

                if (showUrlEditor) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        value = editedUrl,
                        onValueChange = {
                            editedUrl = it
                            onUpdateAppsScriptUrl(it)
                        },
                        label = "Script Web App URL",
                        placeholder = "https://script.google.com/macros/s/.../exec",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "apps_script_url_input"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sync button
                OutlinedButton(
                    onClick = {
                        onSyncNow()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_sync_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Data Now", fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Explicit Logout Button (Only way to log out!)
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_logout_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: Priority) {
    val (bgColor, textColor) = when (priority) {
        Priority.HIGH -> PriorityHighBg to PriorityHighText
        Priority.MEDIUM -> PriorityMediumBg to PriorityMediumText
        Priority.LOW -> PriorityLowBg to PriorityLowText
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = priority.name,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun StatusBadge(status: TaskStatus) {
    val (bgColor, textColor, label) = when (status) {
        TaskStatus.PENDING -> Triple(StatusPendingBg, StatusPendingText, "Pending")
        TaskStatus.IN_PROGRESS -> Triple(StatusProgressBg, StatusProgressText, "In Progress")
        TaskStatus.COMPLETED -> Triple(StatusCompletedBg, StatusCompletedText, "Completed")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
