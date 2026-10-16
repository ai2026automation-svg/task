package com.example.data.model

import com.squareup.moshi.JsonClass

enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

@JsonClass(generateAdapter = true)
data class User(
    val email: String,
    val name: String,
    val role: String = "Team Member",
    val department: String = "Finance & Operations",
    val designation: String = "Operations Analyst"
)

@JsonClass(generateAdapter = true)
data class TaskItem(
    val id: String,
    val title: String,
    val description: String,
    val assignedTo: String,
    val assignedToEmail: String,
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: String,
    val category: String = "General",
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class AuthSession(
    val token: String,
    val user: User,
    val permissions: List<String> = listOf("READ", "WRITE", "ASSIGN"),
    val scopedDoers: List<String> = emptyList()
)
