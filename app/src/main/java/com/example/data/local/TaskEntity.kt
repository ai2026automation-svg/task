package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.data.model.User

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val assignedTo: String,
    val assignedToEmail: String,
    val priority: String,
    val status: String,
    val dueDate: String,
    val category: String,
    val remarks: String,
    val createdAt: Long
) {
    fun toDomain(): TaskItem = TaskItem(
        id = id,
        title = title,
        description = description,
        assignedTo = assignedTo,
        assignedToEmail = assignedToEmail,
        priority = runCatching { Priority.valueOf(priority) }.getOrDefault(Priority.MEDIUM),
        status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.PENDING),
        dueDate = dueDate,
        category = category,
        remarks = remarks,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(item: TaskItem): TaskEntity = TaskEntity(
            id = item.id,
            title = item.title,
            description = item.description,
            assignedTo = item.assignedTo,
            assignedToEmail = item.assignedToEmail,
            priority = item.priority.name,
            status = item.status.name,
            dueDate = item.dueDate,
            category = item.category,
            remarks = item.remarks,
            createdAt = item.createdAt
        )
    }
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val role: String,
    val department: String,
    val designation: String
) {
    fun toDomain(): User = User(
        email = email,
        name = name,
        role = role,
        department = department,
        designation = designation
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            email = user.email,
            name = user.name,
            role = user.role,
            department = user.department,
            designation = user.designation
        )
    }
}
