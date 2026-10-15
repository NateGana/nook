package com.example.nook.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nook.data.*
import com.example.nook.ui.components.*
import com.example.nook.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationSettingsScreen(
    userEmail: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val initialPrefs = remember(userEmail) { NookRepository.loadNotificationPrefs(context, userEmail) }

    var allowNotifs by remember { mutableStateOf(initialPrefs.first) }
    var allowReminders by remember { mutableStateOf(initialPrefs.second) }
    var allowActivities by remember { mutableStateOf(initialPrefs.third) }

    var systemPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        systemPermissionGranted = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Notification permission denied in Android settings", Toast.LENGTH_SHORT).show()
        }
    }

    fun savePrefs(newAllow: Boolean, newRem: Boolean, newAct: Boolean) {
        allowNotifs = newAllow
        allowReminders = newRem
        allowActivities = newAct
        NookRepository.saveNotificationPrefs(context, userEmail, newAllow, newRem, newAct)
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Notification Settings", onBack = onBack)
        HorizontalDivider(color = NookInput)

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !systemPermissionGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Warning, null, tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Notifications Disabled in System", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Android notification permission is currently denied. Tap below to enable app notifications in system settings.", color = Color(0xFF5D4037), fontSize = 12.sp)
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NookTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Open App Settings", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Text("Master Controls", color = NookMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))

            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(Icons.Filled.Notifications, "Allow Notifications") {
                    Switch(
                        checked = allowNotifs,
                        onCheckedChange = { checked ->
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !systemPermissionGranted) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                            savePrefs(checked, allowReminders, allowActivities)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NookTeal)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Notification Categories", color = NookMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))

            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(Icons.Filled.Alarm, "Reminder Notifications") {
                    Switch(
                        checked = allowNotifs && allowReminders,
                        enabled = allowNotifs,
                        onCheckedChange = { checked -> savePrefs(allowNotifs, checked, allowActivities) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NookTeal)
                    )
                }
                HorizontalDivider(color = NookInput)
                SettingsRow(Icons.Filled.History, "Activity Notifications") {
                    Switch(
                        checked = allowNotifs && allowActivities,
                        enabled = allowNotifs,
                        onCheckedChange = { checked -> savePrefs(allowNotifs, allowReminders, checked) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NookTeal)
                    )
                }
            }
        }
    }
}

@Composable
fun RemindersScreen(
    userEmail: String,
    items: List<NookItem>,
    zones: List<Zone>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var remindersList by remember { mutableStateOf(NookRepository.loadReminders(context, userEmail)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<Reminder?>(null) }

    fun refreshList() {
        remindersList = NookRepository.loadReminders(context, userEmail)
    }

    if (showAddDialog) {
        AddEditReminderDialog(
            reminder = null,
            items = items,
            zones = zones,
            onDismiss = { showAddDialog = false },
            onSave = { newReminder ->
                val updated = remindersList + newReminder
                NookRepository.saveReminders(context, userEmail, updated)
                refreshList()
                showAddDialog = false
                Toast.makeText(context, "Reminder scheduled!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (reminderToEdit != null) {
        AddEditReminderDialog(
            reminder = reminderToEdit,
            items = items,
            zones = zones,
            onDismiss = { reminderToEdit = null },
            onSave = { updatedReminder ->
                val updated = remindersList.map { if (it.id == updatedReminder.id) updatedReminder else it }
                NookRepository.saveReminders(context, userEmail, updated)
                refreshList()
                reminderToEdit = null
                Toast.makeText(context, "Reminder updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Reminders", onBack = onBack)
        HorizontalDivider(color = NookInput)

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .imePadding()
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Scheduled Reminders", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NookText)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NookTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, null, Modifier.size(16.dp), Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Reminder", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                if (remindersList.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)).background(NookCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Alarm, null, Modifier.size(36.dp), NookMuted)
                            Spacer(Modifier.height(8.dp))
                            Text("No reminders scheduled yet. Tap 'Add Reminder' above!", color = NookMuted, fontSize = 13.sp)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        remindersList.forEachIndexed { index, r ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(r) {
                                delay(60L * index)
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(300))
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { reminderToEdit = r },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = NookCard)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(r.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NookText)
                                            if (r.note.isNotBlank()) {
                                                Text(r.note, fontSize = 12.sp, color = NookMuted, maxLines = 1)
                                            }
                                            Spacer(Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Schedule, null, Modifier.size(13.dp), NookTeal)
                                                Spacer(Modifier.width(4.dp))
                                                Text("${r.dateText} at ${r.timeText}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = NookTeal)
                                            }
                                            if (r.relatedItemName.isNotBlank() || r.relatedZoneName.isNotBlank()) {
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    buildString {
                                                        if (r.relatedItemName.isNotBlank()) append("Item: ${r.relatedItemName} ")
                                                        if (r.relatedZoneName.isNotBlank()) append("Zone: ${r.relatedZoneName}")
                                                    },
                                                    fontSize = 11.sp,
                                                    color = NookMuted
                                                )
                                            }
                                        }

                                        Switch(
                                            checked = r.isEnabled,
                                            onCheckedChange = { checked ->
                                                val updated = remindersList.map { if (it.id == r.id) it.copy(isEnabled = checked) else it }
                                                NookRepository.saveReminders(context, userEmail, updated)
                                                refreshList()
                                            },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NookTeal)
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        Icon(
                                            Icons.Filled.Delete,
                                            "Delete reminder",
                                            Modifier.size(20.dp).clickable {
                                                val updated = remindersList.filter { it.id != r.id }
                                                NookRepository.saveReminders(context, userEmail, updated)
                                                refreshList()
                                                Toast.makeText(context, "Reminder deleted", Toast.LENGTH_SHORT).show()
                                            },
                                            Color(0xFFD32F2F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditReminderDialog(
    reminder: Reminder?,
    items: List<NookItem>,
    zones: List<Zone>,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var title by remember { mutableStateOf(reminder?.title ?: "") }
    var note by remember { mutableStateOf(reminder?.note ?: "") }
    var selectedItem by remember { mutableStateOf(reminder?.relatedItemName ?: "") }
    var selectedZone by remember { mutableStateOf(reminder?.relatedZoneName ?: "") }

    var selectedTimeMillis by remember { mutableStateOf(reminder?.timeMillis ?: (System.currentTimeMillis() + 3600000)) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.US) }

    var dateText by remember { mutableStateOf(reminder?.dateText ?: dateFormatter.format(Date(selectedTimeMillis))) }
    var timeText by remember { mutableStateOf(reminder?.timeText ?: timeFormatter.format(Date(selectedTimeMillis))) }

    fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimeMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                selectedTimeMillis = cal.timeInMillis
                dateText = dateFormatter.format(cal.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedTimeMillis }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                selectedTimeMillis = cal.timeInMillis
                timeText = timeFormatter.format(cal.time)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (reminder == null) "Schedule Reminder" else "Edit Reminder", fontWeight = FontWeight.Bold, color = NookText) },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Title *", fontSize = 12.sp, color = NookMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Bring Passport", color = NookMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = NookCard, unfocusedContainerColor = NookCard)
                )

                Spacer(Modifier.height(10.dp))
                Text("Note (Optional)", fontSize = 12.sp, color = NookMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("e.g. In top drawer next to keys", color = NookMuted) },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = NookCard, unfocusedContainerColor = NookCard)
                )

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f).clickable { showDatePicker() }) {
                        Column {
                            Text("Date", fontSize = 12.sp, color = NookMuted)
                            Spacer(Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = NookCard, modifier = Modifier.fillMaxWidth().height(44.dp)) {
                                Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CalendarToday, null, Modifier.size(16.dp), NookTeal)
                                    Spacer(Modifier.width(6.dp))
                                    Text(dateText, fontSize = 12.sp, color = NookText)
                                }
                            }
                        }
                    }
                    Box(Modifier.weight(1f).clickable { showTimePicker() }) {
                        Column {
                            Text("Time", fontSize = 12.sp, color = NookMuted)
                            Spacer(Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = NookCard, modifier = Modifier.fillMaxWidth().height(44.dp)) {
                                Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessTime, null, Modifier.size(16.dp), NookTeal)
                                    Spacer(Modifier.width(6.dp))
                                    Text(timeText, fontSize = 12.sp, color = NookText)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        NookDropdownField(
                            label = "Related Item",
                            selectedOption = selectedItem,
                            options = items.map { it.name },
                            onOptionSelected = { selectedItem = it },
                            placeholder = "Select Item"
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        NookDropdownField(
                            label = "Related Zone",
                            selectedOption = selectedZone,
                            options = zones.map { it.name },
                            onOptionSelected = { selectedZone = it },
                            placeholder = "Select Zone"
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            Reminder(
                                id = reminder?.id ?: UUID.randomUUID().toString(),
                                title = title.trim(),
                                note = note.trim(),
                                timeMillis = selectedTimeMillis,
                                timeText = timeText,
                                dateText = dateText,
                                relatedItemName = selectedItem,
                                relatedZoneName = selectedZone,
                                isEnabled = true
                            )
                        )
                    } else {
                        Toast.makeText(context, "Please enter a reminder title", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NookTeal)
            ) { Text("Save", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = NookMuted) }
        },
        containerColor = NookCream
    )
}

@Composable
fun BackupRestoreScreen(
    userEmail: String,
    onBack: () -> Unit,
    onDataRestored: () -> Unit
) {
    val context = LocalContext.current
    var inspectResult by remember { mutableStateOf<Pair<Boolean, Map<String, Any>>?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        if (uri != null) {
            isExporting = true
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val success = NookRepository.exportBackupZip(context, userEmail, outputStream)
                    isExporting = false
                    if (success) {
                        Toast.makeText(context, "Backup completed successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to create backup ZIP", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                isExporting = false
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val (valid, details) = NookRepository.inspectBackupZip(context, inputStream)
                    if (valid) {
                        inspectResult = valid to details
                    } else {
                        Toast.makeText(context, details["error"]?.toString() ?: "Invalid backup file", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (inspectResult != null && inspectResult!!.first) {
        val details = inspectResult!!.second
        val tempDir = details["temp_dir"] as String
        var isMergeMode by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { inspectResult = null },
            title = { Text("Confirm Backup Restore", fontWeight = FontWeight.Bold, color = NookText) },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    Text("Backup File Inspection:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NookText)
                    Spacer(Modifier.height(6.dp))
                    Text("• Created: ${details["created_at"]}", fontSize = 12.sp, color = NookMuted)
                    Text("• Owner: ${details["owner_email"]}", fontSize = 12.sp, color = NookMuted)
                    Text("• Zones: ${details["zones"]} | Items: ${details["items"]}", fontSize = 12.sp, color = NookMuted)
                    Text("• Reminders: ${details["reminders"]} | Photos: ${details["images"]}", fontSize = 12.sp, color = NookMuted)

                    Spacer(Modifier.height(14.dp))
                    Text("Select Restore Mode:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = NookText)
                    Spacer(Modifier.height(6.dp))

                    Row(Modifier.fillMaxWidth().clickable { isMergeMode = true }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isMergeMode, onClick = { isMergeMode = true })
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("Merge with Current Data", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NookText)
                            Text("Appends backup items/zones without deleting existing data.", fontSize = 11.sp, color = NookMuted)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth().clickable { isMergeMode = false }, verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !isMergeMode, onClick = { isMergeMode = false })
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("Replace Current User Data", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NookRed)
                            Text("Replaces active user's data completely. Other accounts are unaffected.", fontSize = 11.sp, color = NookMuted)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = NookRepository.restoreBackupFromInspect(context, userEmail, tempDir, isMergeMode)
                        inspectResult = null
                        if (success) {
                            onDataRestored()
                            Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Failed to restore backup data", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isMergeMode) NookTeal else NookRed)
                ) {
                    Text(if (isMergeMode) "Merge & Restore" else "Replace & Restore", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { inspectResult = null }) { Text("Cancel", color = NookMuted) }
            },
            containerColor = NookCream
        )
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Backup & Restore", onBack = onBack)
        HorizontalDivider(color = NookInput)

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NookCard)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudUpload, null, Modifier.size(24.dp), NookTeal)
                        Spacer(Modifier.width(10.dp))
                        Text("Export Backup", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NookText)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Export your zones, items, descriptions, reminders, and photos into a portable ZIP backup file.",
                        fontSize = 12.sp,
                        color = NookMuted
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val defaultName = "nook_backup_${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())}.zip"
                            exportLauncher.launch(defaultName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NookTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.FolderZip, null, Modifier.size(18.dp), Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Export Backup (.zip)", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NookCard)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CloudDownload, null, Modifier.size(24.dp), NookTeal)
                        Spacer(Modifier.width(10.dp))
                        Text("Restore Backup", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NookText)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Import a Nook ZIP backup file to restore your items, zones, reminders, and photos on this device.",
                        fontSize = 12.sp,
                        color = NookMuted
                    )
                    Spacer(Modifier.height(14.dp))
                    NookOutlinedButton("Select Backup File (.zip)", {
                        importLauncher.launch(arrayOf("application/zip", "application/x-zip-compressed", "*/*"))
                    }, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun HelpCenterScreen(onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }

    val faqs = remember {
        listOf(
            "What is Nook?" to "Nook is your personal belongings manager that helps you keep track of where you put your items, items inside specific zones, and schedule reminders.",
            "What is a Zone?" to "A Zone is any location or container in your home where you store items, such as 'Living Room', 'Hallway Drawer', or 'Garage Shelf'.",
            "How do I create a Zone?" to "Go to the Zones tab and tap '+ Add zone' at the bottom. You can type a zone name and optionally take a photo or select one from your gallery.",
            "How do I add an item?" to "Tap the large '+' button in the center of the bottom navigation bar. Take a photo, enter item details (name, category, zone, description), and tap 'Save Item'.",
            "How do I edit or delete an item?" to "Tap on any item card on the Home screen or inside a Zone detail screen. Use the edit or delete icon buttons on the item card.",
            "How do Reminders work?" to "Go to Settings → Reminders and tap 'Add Reminder'. You can set a date, time, title, note, and optionally link it to a specific item or zone. At the set time, a local notification will alert you.",
            "How do Notifications work?" to "Notifications log recent activities (adding items/zones, edits, reminders) and display a summary when you tap the notification bell icon at the top of the app.",
            "How do I create a backup?" to "Go to Settings → Backup & Restore and tap 'Export Backup (.zip)'. Select a destination on your phone to save the portable zip file.",
            "How do I restore a backup?" to "Go to Settings → Backup & Restore, tap 'Select Backup File (.zip)', pick your backup, choose whether to Merge or Replace, and confirm.",
            "What if an item's photo is missing?" to "You can edit any item or zone at any time to retake a photo or choose a new picture from your device gallery.",
            "Does logging out delete my saved items?" to "No! Your items, zones, photos, and reminders are stored permanently per user account and will be reloaded whenever you log back in.",
            "Can another account see my items?" to "No. Every user account has strict data isolation, ensuring only you can see your saved belongings and reminders."
        )
    }

    val filteredFaqs = faqs.filter {
        query.isBlank() || it.first.contains(query, ignoreCase = true) || it.second.contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Help Center", onBack = onBack)
        HorizontalDivider(color = NookInput)

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .imePadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(NookInput, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, null, Modifier.size(18.dp), NookMuted)
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = TextStyle(color = NookText, fontSize = 14.sp),
                    cursorBrush = SolidColor(NookTeal),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) {
                                Text("Search help topics...", color = NookMuted, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredFaqs.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)).background(NookCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching help topics found.", color = NookMuted, fontSize = 13.sp)
                    }
                } else {
                    filteredFaqs.forEachIndexed { index, faq ->
                        var expanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = NookCard)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        faq.first,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NookText,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = null,
                                        tint = NookTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (expanded) {
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = NookInput)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        faq.second,
                                        fontSize = 13.sp,
                                        color = NookMuted,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactUsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    fun launchEmailIntent(subject: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$SUPPORT_EMAIL")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(
                Intent.EXTRA_TEXT,
                """
                Nook App Version: 1.0
                Android Version: SDK ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})
                Device Model: ${Build.MANUFACTURER} ${Build.MODEL}

                Message / Details:
                [Type your message here]
                """.trimIndent()
            )
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Send Email via..."))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No email app found. Support email: $SUPPORT_EMAIL", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open email client: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Contact Us", onBack = onBack)
        HorizontalDivider(color = NookInput)

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NookCard)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Nook Support", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NookText)
                    Spacer(Modifier.height(4.dp))
                    Text("Support Email: $SUPPORT_EMAIL", fontSize = 13.sp, color = NookTeal, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("Need assistance, found a bug, or have feedback? Reach out to us anytime.", fontSize = 12.sp, color = NookMuted)
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(Icons.AutoMirrored.Filled.Send, "Email Support", onClick = { launchEmailIntent("Nook Support Request") }) {
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText)
                }
                HorizontalDivider(color = NookInput)
                SettingsRow(Icons.AutoMirrored.Filled.HelpOutline, "Report a Problem", onClick = { launchEmailIntent("Nook Problem Report") }) {
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText)
                }
                HorizontalDivider(color = NookInput)
                SettingsRow(Icons.Filled.Feedback, "Send Feedback", onClick = { launchEmailIntent("Nook App Feedback") }) {
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText)
                }
            }
        }
    }
}
