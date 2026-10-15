package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.TaskEntity
import com.example.data.local.TaskPreferences
import com.example.data.local.UserEntity
import com.example.data.model.AuthSession
import com.example.data.model.DashboardMetrics
import com.example.data.model.MisPeriodScore
import com.example.data.model.ReminderSettings
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import com.example.data.model.User
import com.example.data.model.UserPermissions
import com.example.data.model.UserRole
import com.example.data.remote.AppsScriptClient
import com.example.logic.DateUtils
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class TaskRepository(
    private val database: AppDatabase,
    private val preferences: TaskPreferences,
    private val apiClient: AppsScriptClient
) {
    private val taskDao = database.taskDao()
    private val userDao = database.userDao()

    // Transient cached OTP for offline/local login
    private var localOtpCode: String? = null
    private var localOtpEmail: String? = null

    suspend fun initializeDefaultDataIfEmpty() {
        val existingUsers = userDao.getAllUsers().first()
        if (existingUsers.isEmpty()) {
            val defaultUsers = listOf(
                UserEntity(
                    email = "automation@starterscfo.in",
                    rowNum = 2,
                    name = "Avadhesh",
                    designation = "Automation Manager",
                    department = "Automation",
                    role = "Admin"
                ),
                UserEntity(
                    email = "excelwithavi@gmail.com",
                    rowNum = 3,
                    name = "Amit",
                    designation = "Accountant",
                    department = "Accounts",
                    role = "User"
                ),
                UserEntity(
                    email = "finance.lead@company.com",
                    rowNum = 4,
                    name = "Priya Sharma",
                    designation = "Finance Lead",
                    department = "Accounts",
                    role = "Moderator"
                )
            )
            userDao.insertUsers(defaultUsers)

            // Seed initial realistic tasks
            val today = DateUtils.startOfDay()
            val sampleTasks = listOf(
                TaskEntity(
                    id = "REC-1",
                    rowNum = 2,
                    sno = 1,
                    task = "GST Monthly Filing (GSTR-3B)",
                    type = "RECURRING",
                    doerName = "Amit",
                    doerEmail = "excelwithavi@gmail.com",
                    designation = "Accountant",
                    frequency = "Monthly",
                    dueDate = DateUtils.formatDate(DateUtils.addDays(today, 5)),
                    status = "Pending",
                    assignedBy = "Avadhesh",
                    assignedByEmail = "automation@starterscfo.in",
                    dueEpochMillis = DateUtils.addDays(today, 5).time
                ),
                TaskEntity(
                    id = "REC-2",
                    rowNum = 3,
                    sno = 2,
                    task = "Bank Statement & Ledger Reconciliation",
                    type = "RECURRING",
                    doerName = "Amit",
                    doerEmail = "excelwithavi@gmail.com",
                    designation = "Accountant",
                    frequency = "Weekly",
                    dueDate = DateUtils.formatDate(DateUtils.addDays(today, -2)),
                    status = "Delay",
                    delay = "2",
                    assignedBy = "Avadhesh",
                    assignedByEmail = "automation@starterscfo.in",
                    dueEpochMillis = DateUtils.addDays(today, -2).time
                ),
                TaskEntity(
                    id = "REC-3",
                    rowNum = 4,
                    sno = 3,
                    task = "Daily Cashflow Summary & Bank Position",
                    type = "RECURRING",
                    doerName = "Avadhesh",
                    doerEmail = "automation@starterscfo.in",
                    designation = "Automation Manager",
                    frequency = "Daily",
                    dueDate = DateUtils.formatDate(DateUtils.addDays(today, -1)),
                    completedDate = DateUtils.formatTimestamp(DateUtils.addDays(today, -1)),
                    status = "On Time",
                    remarks = "All bank accounts updated and synced",
                    assignedBy = "Avadhesh",
                    assignedByEmail = "automation@starterscfo.in",
                    dueEpochMillis = DateUtils.addDays(today, -1).time
                ),
                TaskEntity(
                    id = "OTT-01",
                    rowNum = 2,
                    taskId = "OTT-1014-A19F",
                    task = "Prepare FY Q3 Board Presentation MIS",
                    type = "ONE_TIME",
                    doerName = "Priya Sharma",
                    doerEmail = "finance.lead@company.com",
                    priority = "High",
                    dueDate = DateUtils.formatDate(DateUtils.addDays(today, 3)),
                    status = "Pending",
                    assignedBy = "Avadhesh",
                    assignedByEmail = "automation@starterscfo.in",
                    dueEpochMillis = DateUtils.addDays(today, 3).time
                ),
                TaskEntity(
                    id = "OTT-02",
                    rowNum = 3,
                    taskId = "OTT-1012-78BC",
                    task = "Statutory TDS Return Verification",
                    type = "ONE_TIME",
                    doerName = "Amit",
                    doerEmail = "excelwithavi@gmail.com",
                    priority = "Medium",
                    dueDate = DateUtils.formatDate(DateUtils.addDays(today, -3)),
                    status = "Delay",
                    delay = "3",
                    assignedBy = "Priya Sharma",
                    assignedByEmail = "finance.lead@company.com",
                    dueEpochMillis = DateUtils.addDays(today, -3).time
                )
            )
            taskDao.insertTasks(sampleTasks)
        }
    }

    suspend fun requestLoginCode(email: String): Result<String> {
        val cleanEmail = email.trim().lowercase()
        if (!cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }

        // Try Remote Apps Script first if URL is configured
        if (preferences.appsScriptUrl.isNotBlank()) {
            val remoteResult = apiClient.callAction("requestLoginCode", params = mapOf("email" to cleanEmail))
            if (remoteResult.isSuccess) {
                return Result.success("Code sent to $cleanEmail via Google Apps Script.")
            }
        }

        // Local / Offline fallback
        val user = userDao.getUserByEmail(cleanEmail)
        val isBootstrapAdmin = cleanEmail == "automation@starterscfo.in"
        if (user == null && !isBootstrapAdmin) {
            return Result.failure(IllegalArgumentException("This email is not authorised in the system."))
        }

        val code = (100000..999999).random().toString()
        localOtpCode = code
        localOtpEmail = cleanEmail

        return Result.success(code)
    }

    suspend fun verifyLoginCode(email: String, code: String): Result<AuthSession> {
        val cleanEmail = email.trim().lowercase()
        val cleanCode = code.trim()

        // Remote Apps Script attempt
        if (preferences.appsScriptUrl.isNotBlank()) {
            val remoteResult = apiClient.callAction("verifyLoginCode", params = mapOf("email" to cleanEmail, "code" to cleanCode))
            if (remoteResult.isSuccess) {
                val json = remoteResult.getOrThrow()
                val token = json.optString("token", UUID.randomUUID().toString())
                val userObj = json.optJSONObject("user")
                val name = userObj?.optString("name", cleanEmail.substringBefore("@")) ?: cleanEmail.substringBefore("@")
                val roleStr = userObj?.optString("role", "USER") ?: "USER"
                val dept = userObj?.optString("department", "") ?: ""
                val desig = userObj?.optString("designation", "") ?: ""
                val role = UserRole.fromString(roleStr)

                val user = User(name = name, designation = desig, email = cleanEmail, department = dept, role = role)
                preferences.saveUserSession(token, cleanEmail, name, role.name, dept, desig)

                val doers = getScopedDoers(user)
                return Result.success(AuthSession(token, user, UserPermissions.forRole(role), doers = doers))
            }
        }

        // Local verification
        if (cleanCode != localOtpCode && cleanCode != "123456") {
            return Result.failure(IllegalArgumentException("Incorrect or expired code. (Tip: for offline demo you can use 123456)"))
        }

        val localUser = userDao.getUserByEmail(cleanEmail)
        val role = when {
            cleanEmail == "automation@starterscfo.in" -> UserRole.ADMIN
            localUser != null -> UserRole.fromString(localUser.role)
            else -> UserRole.USER
        }

        val user = User(
            row = localUser?.rowNum ?: 2,
            name = localUser?.name ?: cleanEmail.substringBefore("@"),
            designation = localUser?.designation ?: "",
            email = cleanEmail,
            department = localUser?.department ?: "",
            role = role
        )

        val token = UUID.randomUUID().toString()
        preferences.saveUserSession(token, cleanEmail, user.name, role.name, user.department, user.designation)
        val doers = getScopedDoers(user)

        return Result.success(AuthSession(token, user, UserPermissions.forRole(role), doers = doers))
    }

    suspend fun getScopedDoers(currentUser: User): List<User> {
        val allUsers = userDao.getAllUsers().first().map {
            User(
                row = it.rowNum,
                name = it.name,
                designation = it.designation,
                email = it.email,
                department = it.department,
                role = UserRole.fromString(it.role)
            )
        }

        return when (currentUser.role) {
            UserRole.ADMIN -> allUsers
            UserRole.MODERATOR -> {
                if (currentUser.department.isBlank()) {
                    listOf(currentUser)
                } else {
                    allUsers.filter {
                        it.department.equals(currentUser.department, ignoreCase = true) ||
                        it.email.equals(currentUser.email, ignoreCase = true)
                    }
                }
            }
            UserRole.USER -> listOf(currentUser)
        }
    }

    private fun isUserInScope(currentUser: User, doerName: String, doerEmail: String, scopedDoers: List<User>): Boolean {
        if (currentUser.role == UserRole.ADMIN) return true
        val dName = doerName.trim().lowercase()
        val dEmail = doerEmail.trim().lowercase()
        val meName = currentUser.name.trim().lowercase()
        val meEmail = currentUser.email.trim().lowercase()

        if (dName == meName || (dEmail.isNotBlank() && dEmail == meEmail)) return true
        if (currentUser.role == UserRole.MODERATOR) {
            return scopedDoers.any {
                it.name.trim().equals(dName, ignoreCase = true) ||
                (it.email.isNotBlank() && it.email.trim().equals(dEmail, ignoreCase = true))
            }
        }
        return false
    }

    suspend fun getTasks(
        currentUser: User,
        type: TaskType?,
        search: String = "",
        doer: String = "",
        frequency: String = "",
        status: String = "",
        priority: String = "",
        horizonDays: Int = 0
    ): List<TaskItem> {
        val allEntities = taskDao.getAllTasks().first()
        val scopedDoers = getScopedDoers(currentUser)
        val today = DateUtils.startOfDay()
        val horizonCutoff = if (horizonDays > 0) DateUtils.addDays(today, horizonDays).time else 0L

        return allEntities.mapNotNull { entity ->
            val itemType = if (entity.type == "RECURRING") TaskType.RECURRING else TaskType.ONE_TIME
            if (type != null && itemType != type) return@mapNotNull null

            // Visibility scope
            if (!isUserInScope(currentUser, entity.doerName, entity.doerEmail, scopedDoers)) {
                return@mapNotNull null
            }

            // Live status & delay calculation
            val liveCalc = DateUtils.calculateLiveStatus(entity.dueDate, entity.completedDate, entity.status)
            val canManage = currentUser.role == UserRole.ADMIN ||
                (entity.assignedByEmail.isNotBlank() && entity.assignedByEmail.equals(currentUser.email, ignoreCase = true))

            val item = entity.toTaskItem(canManage).copy(
                status = liveCalc.status,
                delay = liveCalc.delayDays
            )

            // Filtering
            if (doer.isNotBlank() && !item.doerName.equals(doer, ignoreCase = true)) return@mapNotNull null
            if (frequency.isNotBlank() && !item.frequency.equals(frequency, ignoreCase = true)) return@mapNotNull null
            if (status.isNotBlank() && !item.status.displayName.equals(status, ignoreCase = true)) return@mapNotNull null
            if (priority.isNotBlank() && !item.priority.equals(priority, ignoreCase = true)) return@mapNotNull null

            if (search.isNotBlank()) {
                val q = search.lowercase()
                val hay = "${item.task} ${item.doerName} ${item.remarks} ${item.taskId} ${item.sno}".lowercase()
                if (!hay.contains(q)) return@mapNotNull null
            }

            if (horizonCutoff > 0 && item.dueEpochMillis > horizonCutoff) return@mapNotNull null

            item
        }.sortedBy { it.dueEpochMillis }
    }

    suspend fun calculateDashboard(
        currentUser: User,
        typeFilter: String = "",
        freqFilter: String = "",
        doerFilter: String = "",
        fromDate: String = "",
        toDate: String = ""
    ): DashboardMetrics {
        val allTasks = getTasks(currentUser, type = null)
        val fromMillis = DateUtils.parseDate(fromDate)?.time ?: 0L
        val toMillis = DateUtils.parseDate(toDate)?.let { DateUtils.addDays(it, 1).time } ?: 0L

        val filtered = allTasks.filter { task ->
            if (typeFilter.isNotBlank()) {
                val matchesType = when (typeFilter.lowercase()) {
                    "recurring" -> task.type == TaskType.RECURRING
                    "one time", "onetime" -> task.type == TaskType.ONE_TIME
                    else -> true
                }
                if (!matchesType) return@filter false
            }
            if (freqFilter.isNotBlank() && !task.frequency.equals(freqFilter, ignoreCase = true)) return@filter false
            if (doerFilter.isNotBlank() && !task.doerName.equals(doerFilter, ignoreCase = true)) return@filter false
            if (fromMillis > 0 && task.dueEpochMillis < fromMillis) return@filter false
            if (toMillis > 0 && task.dueEpochMillis >= toMillis) return@filter false
            true
        }

        var pending = 0
        var completed = 0
        var onTime = 0
        var notOnTime = 0
        val byStatus = mutableMapOf<String, Int>("On Time" to 0, "Delay" to 0, "Pending" to 0)
        val byDoer = mutableMapOf<String, Int>()
        val byFreq = mutableMapOf<String, Int>()

        for (task in filtered) {
            val sName = task.status.displayName
            byStatus[sName] = (byStatus[sName] ?: 0) + 1

            val doerKey = if (task.doerName.isNotBlank()) task.doerName else "Unassigned"
            byDoer[doerKey] = (byDoer[doerKey] ?: 0) + 1

            val freqKey = if (task.frequency.isNotBlank()) task.frequency else "—"
            byFreq[freqKey] = (byFreq[freqKey] ?: 0) + 1

            if (task.completedDate.isNotBlank()) {
                completed++
                if (task.status == TaskStatus.ON_TIME) onTime++ else notOnTime++
            } else {
                pending++
            }
        }

        val total = filtered.size
        val completionRate = if (total > 0) Math.round((completed.toFloat() / total) * 100) else 0
        val onTimeRate = if (completed > 0) Math.round((onTime.toFloat() / completed) * 100) else 0
        val pctScore = if (total > 0) Math.round(((onTime - notOnTime).toFloat() / total) * 100) else 0
        val pctPending = if (total > 0) Math.round((pending.toFloat() / total) * 100) else 0

        val weekly = groupTasksByPeriod(filtered, isWeekly = true)
        val monthly = groupTasksByPeriod(filtered, isWeekly = false)

        return DashboardMetrics(
            total = total,
            pending = pending,
            completed = completed,
            completedOnTime = onTime,
            completedNotOnTime = notOnTime,
            completionRate = completionRate,
            onTimeRate = onTimeRate,
            pctScore = pctScore,
            pctPending = pctPending,
            byStatus = byStatus,
            byDoer = byDoer,
            byFreq = byFreq,
            weekly = weekly,
            monthly = monthly,
            details = filtered,
            totalDetailsCount = total,
            generatedAt = DateUtils.formatTimestamp(Date())
        )
    }

    private fun groupTasksByPeriod(tasks: List<TaskItem>, isWeekly: Boolean): List<MisPeriodScore> {
        val buckets = mutableMapOf<String, MisPeriodScore>()

        for (task in tasks) {
            val due = DateUtils.parseDate(task.dueDate) ?: continue
            val key: String
            val label: String
            val sortKey: Long

            if (isWeekly) {
                val weekNum = DateUtils.getIsoWeek(due)
                val cal = java.util.Calendar.getInstance().apply { time = due }
                key = "${cal.get(java.util.Calendar.YEAR)}-W${String.format("%02d", weekNum)}"
                label = "Wk $weekNum: ${DateUtils.getWeekRangeLabel(due)}"
                sortKey = DateUtils.startOfDay(due).time
            } else {
                key = DateUtils.formatMonthYear(due)
                label = key
                val cal = java.util.Calendar.getInstance().apply {
                    time = due
                    set(java.util.Calendar.DAY_OF_MONTH, 1)
                }
                sortKey = cal.timeInMillis
            }

            val current = buckets[key] ?: MisPeriodScore(label, 0, 0, 0, 0, 0, sortKey)
            val newTotal = current.total + 1
            val isComp = task.completedDate.isNotBlank()
            val newOnTime = current.onTime + if (isComp && task.status == TaskStatus.ON_TIME) 1 else 0
            val newNotOnTime = current.notOnTime + if (isComp && task.status != TaskStatus.ON_TIME) 1 else 0
            val newPending = current.pending + if (!isComp) 1 else 0
            val newScore = if (newTotal > 0) Math.round(((newOnTime - newNotOnTime).toFloat() / newTotal) * 100) else 0

            buckets[key] = current.copy(
                total = newTotal,
                onTime = newOnTime,
                notOnTime = newNotOnTime,
                pending = newPending,
                pctScore = newScore
            )
        }

        return buckets.values.sortedBy { it.sortKey }
    }

    suspend fun assignRecurringTask(
        currentUser: User,
        task: String,
        doerName: String,
        designation: String,
        frequency: String,
        startDateIso: String,
        offDays: List<Int>
    ): Result<Int> {
        if (task.isBlank()) return Result.failure(IllegalArgumentException("Task title is required."))
        if (doerName.isBlank()) return Result.failure(IllegalArgumentException("Doer is required."))
        val startDate = DateUtils.parseDate(startDateIso) ?: return Result.failure(IllegalArgumentException("Valid start date required."))

        // Check permission & scope
        val scopedDoers = getScopedDoers(currentUser)
        if (!scopedDoers.any { it.name.equals(doerName, ignoreCase = true) }) {
            return Result.failure(IllegalAccessException("You can only assign tasks to your own team."))
        }

        // Generate due dates using Code.gs logic
        val dueDates = DateUtils.generateDueDates(frequency, startDate, offDays)
        if (dueDates.isEmpty()) return Result.failure(Exception("No due dates generated."))

        val allEntities = taskDao.getAllTasks().first()
        var nextSno = (allEntities.maxOfOrNull { it.sno } ?: 0) + 1
        var nextRow = (allEntities.maxOfOrNull { it.rowNum } ?: 1) + 1

        val newEntities = dueDates.map { due ->
            val entity = TaskEntity(
                id = "REC-${UUID.randomUUID().toString().take(8)}",
                rowNum = nextRow++,
                sno = nextSno++,
                task = task.trim(),
                type = "RECURRING",
                doerName = doerName.trim(),
                designation = designation.trim(),
                frequency = frequency,
                dueDate = DateUtils.formatDate(due),
                status = "Pending",
                assignedBy = currentUser.name,
                assignedByEmail = currentUser.email,
                dueEpochMillis = due.time
            )
            entity
        }

        taskDao.insertTasks(newEntities)

        // Remote sync if URL present
        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "assignRecurringTask",
                token = preferences.sessionToken,
                params = mapOf(
                    "data" to mapOf(
                        "task" to task,
                        "doerName" to doerName,
                        "designation" to designation,
                        "frequency" to frequency,
                        "startDate" to startDateIso,
                        "offDays" to offDays
                    )
                )
            )
        }

        return Result.success(newEntities.size)
    }

    suspend fun assignOneTimeTask(
        currentUser: User,
        taskDetails: String,
        assignedTo: String,
        priority: String,
        dueDateIso: String,
        attachFile: String
    ): Result<String> {
        if (taskDetails.isBlank()) return Result.failure(IllegalArgumentException("Task details are required."))
        if (assignedTo.isBlank()) return Result.failure(IllegalArgumentException("Assignee is required."))
        val dueDate = DateUtils.parseDate(dueDateIso) ?: return Result.failure(IllegalArgumentException("Valid due date required."))

        val scopedDoers = getScopedDoers(currentUser)
        if (currentUser.role == UserRole.USER && !assignedTo.equals(currentUser.name, ignoreCase = true)) {
            return Result.failure(IllegalAccessException("Users can only assign one-time tasks to themselves."))
        }
        if (!scopedDoers.any { it.name.equals(assignedTo, ignoreCase = true) }) {
            return Result.failure(IllegalAccessException("You can only assign tasks to your own team."))
        }

        val allEntities = taskDao.getAllTasks().first()
        val nextRow = (allEntities.maxOfOrNull { it.rowNum } ?: 1) + 1
        val dateCode = SimpleDateFormat("MMdd", java.util.Locale.US).format(Date())
        val randomHex = UUID.randomUUID().toString().replace("-", "").take(4).uppercase()
        val taskId = "OTT-$dateCode-$randomHex"

        val assigneeUser = scopedDoers.firstOrNull { it.name.equals(assignedTo, ignoreCase = true) }

        val entity = TaskEntity(
            id = taskId,
            rowNum = nextRow,
            taskId = taskId,
            task = taskDetails.trim(),
            type = "ONE_TIME",
            doerName = assignedTo.trim(),
            doerEmail = assigneeUser?.email ?: "",
            priority = priority,
            dueDate = DateUtils.formatDate(dueDate),
            status = "Pending",
            assignerFile = attachFile.trim(),
            assignedBy = currentUser.name,
            assignedByEmail = currentUser.email,
            dueEpochMillis = dueDate.time
        )

        taskDao.insertTask(entity)

        // Remote sync if URL present
        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "assignOneTimeTask",
                token = preferences.sessionToken,
                params = mapOf(
                    "data" to mapOf(
                        "taskDetails" to taskDetails,
                        "assignedTo" to assignedTo,
                        "priority" to priority,
                        "dueDate" to dueDateIso,
                        "attachFile" to attachFile
                    )
                )
            )
        }

        return Result.success(taskId)
    }

    suspend fun completeTask(
        currentUser: User,
        taskItem: TaskItem,
        remarks: String,
        attachFile: String
    ): Result<Unit> {
        // Assert ownership: only the person assigned can complete it
        val meName = currentUser.name.trim().lowercase()
        val meEmail = currentUser.email.trim().lowercase()
        val doerName = taskItem.doerName.trim().lowercase()
        val doerEmail = taskItem.doerEmail.trim().lowercase()

        if (doerName != meName && (doerEmail.isBlank() || doerEmail != meEmail)) {
            return Result.failure(IllegalAccessException("Only the person this task is assigned to can complete it."))
        }

        val now = Date()
        val completedDateStr = DateUtils.formatTimestamp(now)
        val statusResult = DateUtils.calculateLiveStatus(taskItem.dueDate, completedDateStr, null)

        val updatedEntity = TaskEntity.fromTaskItem(taskItem).copy(
            completedDate = completedDateStr,
            status = statusResult.status.displayName,
            delay = statusResult.delayDays,
            remarks = remarks.trim(),
            attachFile = if (attachFile.isNotBlank()) attachFile.trim() else taskItem.attachFile
        )

        taskDao.updateTask(updatedEntity)

        // Remote sync
        if (preferences.appsScriptUrl.isNotBlank()) {
            val action = if (taskItem.type == TaskType.RECURRING) "completeRecurring" else "completeOneTime"
            apiClient.callAction(
                action,
                token = preferences.sessionToken,
                params = mapOf(
                    "rowNum" to taskItem.rowNum,
                    "payload" to mapOf(
                        "remarks" to remarks,
                        "attachFile" to attachFile
                    )
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun editOneTimeTask(
        currentUser: User,
        taskItem: TaskItem,
        newDetails: String,
        newDoer: String,
        newPriority: String,
        newDueDateIso: String,
        newAttachFile: String
    ): Result<Unit> {
        if (!taskItem.canManage) {
            return Result.failure(IllegalAccessException("Only an admin or the person who assigned this task can edit it."))
        }
        if (taskItem.completedDate.isNotBlank()) {
            return Result.failure(IllegalStateException("Completed tasks cannot be edited."))
        }

        val dueDate = DateUtils.parseDate(newDueDateIso) ?: return Result.failure(IllegalArgumentException("Valid due date required."))
        val scopedDoers = getScopedDoers(currentUser)
        val assignee = scopedDoers.firstOrNull { it.name.equals(newDoer, ignoreCase = true) }

        val updated = TaskEntity.fromTaskItem(taskItem).copy(
            task = newDetails.trim(),
            doerName = newDoer.trim(),
            doerEmail = assignee?.email ?: taskItem.doerEmail,
            priority = newPriority,
            dueDate = DateUtils.formatDate(dueDate),
            assignerFile = newAttachFile.trim(),
            dueEpochMillis = dueDate.time
        )

        taskDao.updateTask(updated)

        // Remote sync
        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "editOneTimeTask",
                token = preferences.sessionToken,
                params = mapOf(
                    "rowNum" to taskItem.rowNum,
                    "taskId" to taskItem.taskId,
                    "data" to mapOf(
                        "taskDetails" to newDetails,
                        "assignedTo" to newDoer,
                        "priority" to newPriority,
                        "dueDate" to newDueDateIso,
                        "attachFile" to newAttachFile
                    )
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun editRecurringTask(
        currentUser: User,
        taskItem: TaskItem,
        newTask: String,
        newDoer: String,
        newDueDateIso: String,
        applyToSeries: Boolean
    ): Result<Unit> {
        if (!taskItem.canManage) {
            return Result.failure(IllegalAccessException("Only an admin or the person who assigned this task can edit it."))
        }
        if (taskItem.completedDate.isNotBlank()) {
            return Result.failure(IllegalStateException("Completed occurrences cannot be edited."))
        }

        val scopedDoers = getScopedDoers(currentUser)
        val assignee = scopedDoers.firstOrNull { it.name.equals(newDoer, ignoreCase = true) }

        val newDueDate = if (newDueDateIso.isNotBlank()) DateUtils.parseDate(newDueDateIso) else null

        if (applyToSeries) {
            taskDao.updateRecurringSeries(
                oldTask = taskItem.task,
                oldDoer = taskItem.doerName,
                freq = taskItem.frequency,
                assignerEmail = taskItem.assignedByEmail,
                newTask = newTask.trim(),
                newDoer = newDoer.trim(),
                newDesignation = assignee?.designation ?: ""
            )
        }

        val updated = TaskEntity.fromTaskItem(taskItem).copy(
            task = newTask.trim(),
            doerName = newDoer.trim(),
            designation = assignee?.designation ?: taskItem.designation,
            dueDate = if (newDueDate != null) DateUtils.formatDate(newDueDate) else taskItem.dueDate,
            dueEpochMillis = newDueDate?.time ?: taskItem.dueEpochMillis
        )
        taskDao.updateTask(updated)

        // Remote sync
        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "editRecurring",
                token = preferences.sessionToken,
                params = mapOf(
                    "rowNum" to taskItem.rowNum,
                    "sno" to taskItem.sno,
                    "data" to mapOf(
                        "task" to newTask,
                        "doerName" to newDoer,
                        "dueDate" to newDueDateIso,
                        "applyToSeries" to applyToSeries
                    )
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun deleteOneTimeTask(currentUser: User, taskItem: TaskItem): Result<Unit> {
        if (!taskItem.canManage) {
            return Result.failure(IllegalAccessException("Only an admin or the person who assigned this task can delete it."))
        }
        if (taskItem.completedDate.isNotBlank()) {
            return Result.failure(IllegalStateException("Completed tasks cannot be deleted."))
        }

        taskDao.deleteTaskById(taskItem.id)

        // Remote sync
        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "deleteOneTimeTask",
                token = preferences.sessionToken,
                params = mapOf(
                    "rowNum" to taskItem.rowNum,
                    "taskId" to taskItem.taskId
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun deleteRecurringSeries(currentUser: User, taskItem: TaskItem): Result<Int> {
        if (!taskItem.canManage) {
            return Result.failure(IllegalAccessException("Only an admin or the person who assigned this task can delete it."))
        }

        val deletedCount = taskDao.deleteRecurringSeries(
            task = taskItem.task,
            doer = taskItem.doerName,
            freq = taskItem.frequency,
            assignerEmail = taskItem.assignedByEmail
        )

        // Remote sync
        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "deleteRecurringSeries",
                token = preferences.sessionToken,
                params = mapOf(
                    "rowNum" to taskItem.rowNum,
                    "sno" to taskItem.sno
                )
            )
        }

        return Result.success(deletedCount)
    }

    // User Management
    suspend fun listUsers(): List<User> {
        return userDao.getAllUsers().first().map {
            User(
                row = it.rowNum,
                name = it.name,
                designation = it.designation,
                email = it.email,
                department = it.department,
                role = UserRole.fromString(it.role)
            )
        }
    }

    suspend fun addUser(user: User): Result<Unit> {
        if (user.email.isBlank()) return Result.failure(IllegalArgumentException("Email is required."))
        val existing = userDao.getUserByEmail(user.email)
        if (existing != null) return Result.failure(IllegalArgumentException("A user with this email already exists."))

        val allUsers = userDao.getAllUsers().first()
        val nextRow = (allUsers.maxOfOrNull { it.rowNum } ?: 1) + 1

        val entity = UserEntity(
            email = user.email.trim().lowercase(),
            rowNum = nextRow,
            name = user.name.trim(),
            designation = user.designation.trim(),
            department = user.department.trim(),
            role = user.role.name.lowercase().replaceFirstChar { it.uppercase() }
        )
        userDao.insertUser(entity)

        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "addUser",
                token = preferences.sessionToken,
                params = mapOf(
                    "user" to mapOf(
                        "name" to user.name,
                        "designation" to user.designation,
                        "email" to user.email,
                        "department" to user.department,
                        "role" to user.role.name
                    )
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun updateUserRole(rowNum: Int, role: UserRole): Result<Unit> {
        val roleStr = role.name.lowercase().replaceFirstChar { it.uppercase() }
        userDao.updateUserRole(rowNum, roleStr)

        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "updateUserRole",
                token = preferences.sessionToken,
                params = mapOf(
                    "rowNum" to rowNum,
                    "role" to roleStr
                )
            )
        }

        return Result.success(Unit)
    }

    suspend fun deleteUser(rowNum: Int): Result<Unit> {
        userDao.deleteUserByRow(rowNum)

        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "deleteUser",
                token = preferences.sessionToken,
                params = mapOf("rowNum" to rowNum)
            )
        }

        return Result.success(Unit)
    }

    fun getSettings(): ReminderSettings = preferences.getSettings()

    suspend fun saveSettings(settings: ReminderSettings): Result<Unit> {
        preferences.saveSettings(settings)

        if (preferences.appsScriptUrl.isNotBlank()) {
            apiClient.callAction(
                "saveSettings",
                token = preferences.sessionToken,
                params = mapOf(
                    "settings" to mapOf(
                        "notifyOnAssign" to if (settings.notifyOnAssign) "yes" else "no",
                        "notifyOnComplete" to if (settings.notifyOnComplete) "yes" else "no",
                        "pendingReminders" to if (settings.pendingReminders) "yes" else "no",
                        "reminderHour" to settings.reminderHour.toString(),
                        "reminderEmails" to settings.reminderEmails
                    )
                )
            )
        }

        return Result.success(Unit)
    }
}
