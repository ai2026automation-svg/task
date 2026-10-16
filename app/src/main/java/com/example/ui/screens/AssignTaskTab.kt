package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.data.model.User
import com.example.ui.components.AppTextField
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun AssignTaskTab(
    teamUsers: List<User>,
    onTaskCreated: (TaskItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assignedTo by remember { mutableStateOf("") }
    var assignedToEmail by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.HIGH) }
    var dueDate by remember { mutableStateOf("Oct 30, 2026") }
    var category by remember { mutableStateOf("Financial Operations") }
    var remarks by remember { mutableStateOf("") }
    var showSuccessBanner by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCanvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Assign New Task",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Dispatch financial workflows and operations to team members",
                fontSize = 13.sp,
                color = TextMuted
            )
        }

        if (showSuccessBanner) {
            Surface(
                color = Color(0xFFD1FAE5),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Task assigned successfully!",
                        color = Color(0xFF065F46),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Task Title (Crisp full-opacity text)
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Task Title *",
                    placeholder = "e.g. Audit Escrow Accounts & Reconciliation",
                    leadingIcon = Icons.Default.Title,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "assign_title_input"
                )

                // Description (Crisp full-opacity text)
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Detailed Scope & Instructions",
                    placeholder = "Provide instructions, references, or specific deliverables...",
                    leadingIcon = Icons.Default.Description,
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "assign_description_input"
                )

                // Quick Assignee selector chips
                if (teamUsers.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Quick Select Team Member:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(teamUsers) { user ->
                                val isSelected = assignedToEmail == user.email
                                Surface(
                                    color = if (isSelected) PrimaryBlue else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        assignedTo = user.name
                                        assignedToEmail = user.email
                                    }
                                ) {
                                    Text(
                                        text = user.name,
                                        color = if (isSelected) Color.White else TextDark,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Assignee Name (Crisp full-opacity text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppTextField(
                        value = assignedTo,
                        onValueChange = { assignedTo = it },
                        label = "Assignee Name *",
                        placeholder = "e.g. Rahul Sharma",
                        leadingIcon = Icons.Default.Person,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        testTag = "assign_name_input"
                    )

                    AppTextField(
                        value = assignedToEmail,
                        onValueChange = { assignedToEmail = it },
                        label = "Assignee Email",
                        placeholder = "name@smartfin.com",
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        testTag = "assign_email_input"
                    )
                }

                // Priority Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Priority Level:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDark
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Priority.values().forEach { p ->
                            val isSelected = priority == p
                            val (selectedBg, selectedText) = when (p) {
                                Priority.HIGH -> Color(0xFFDC2626) to Color.White
                                Priority.MEDIUM -> Color(0xFFD97706) to Color.White
                                Priority.LOW -> Color(0xFF2563EB) to Color.White
                            }
                            Surface(
                                color = if (isSelected) selectedBg else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { priority = p }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = p.name,
                                        color = if (isSelected) selectedText else TextDark,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Due Date & Category (Crisp full-opacity text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = "Target Due Date",
                        placeholder = "e.g. Oct 30, 2026",
                        leadingIcon = Icons.Default.CalendarToday,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        testTag = "assign_duedate_input"
                    )

                    AppTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = "Department / Category",
                        placeholder = "e.g. Audit, Accounts",
                        leadingIcon = Icons.Default.Category,
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        testTag = "assign_category_input"
                    )
                }

                // Remarks / Notes (Crisp full-opacity text)
                AppTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = "Initial Remarks / Checkpoints",
                    placeholder = "Attach notes, ticket links, or policy references...",
                    leadingIcon = Icons.Default.Comment,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "assign_remarks_input"
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Assign Button
                Button(
                    onClick = {
                        if (title.isNotBlank() && assignedTo.isNotBlank()) {
                            val newTask = TaskItem(
                                id = "task_" + UUID.randomUUID().toString().take(8),
                                title = title.trim(),
                                description = description.trim(),
                                assignedTo = assignedTo.trim(),
                                assignedToEmail = assignedToEmail.trim(),
                                priority = priority,
                                status = TaskStatus.PENDING,
                                dueDate = dueDate.trim(),
                                category = category.trim().ifBlank { "General" },
                                remarks = remarks.trim()
                            )
                            onTaskCreated(newTask)
                            showSuccessBanner = true
                            // Reset form fields
                            title = ""
                            description = ""
                            remarks = ""
                        }
                    },
                    enabled = title.isNotBlank() && assignedTo.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_assign_task_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dispatch & Assign Task",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
