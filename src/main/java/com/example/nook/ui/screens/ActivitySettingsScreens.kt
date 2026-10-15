package com.example.nook.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nook.data.*
import com.example.nook.ui.components.*
import com.example.nook.ui.theme.*

@Composable
fun ActivityScreen(
    activities: List<ActivityEntry> = emptyList(),
    hasUnreadNotifications: Boolean = false,
    onNotificationClick: () -> Unit = {},
    onDeleteActivity: (ActivityEntry) -> Unit = {},
    onClearAllActivities: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "Added", "Edited", "Removed")

    val filteredActivities = activities.filter { entry ->
        val matchesFilter = when (selectedFilter) {
            "Added" -> entry.action.equals("Added", ignoreCase = true)
            "Edited" -> entry.action.equals("Edited", ignoreCase = true)
            "Removed" -> entry.action.equals("Removed", ignoreCase = true)
            else -> true
        }
        val matchesQuery = query.isBlank() ||
                entry.itemName.contains(query, ignoreCase = true) ||
                entry.zone.contains(query, ignoreCase = true) ||
                entry.action.contains(query, ignoreCase = true)

        matchesFilter && matchesQuery
    }

    var showClearConfirm by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear All Notifications?", fontWeight = FontWeight.Bold, color = NookText) },
            text = { Text("Are you sure you want to clear all notifications? This action cannot be undone.", color = NookText, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearAllActivities()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NookRed)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = NookMuted)
                }
            },
            containerColor = NookCream
        )
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Activity", right = { BadgedBell(hasUnread = hasUnreadNotifications, onClick = onNotificationClick) })
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
                                Text("Search my activities", color = NookMuted, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Spacer(Modifier.height(14.dp))

            // Filter Chips + Clear All Option
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState())
                ) {
                    filters.forEach { filter ->
                        val isSelected = filter == selectedFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) NookTeal else NookInput)
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else NookMuted,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                if (activities.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Clear All",
                        color = NookRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { showClearConfirm = true }
                            .padding(vertical = 4.dp, horizontal = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Activity List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (filteredActivities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NookCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = NookMuted
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (activities.isEmpty()) "No activity recorded yet." else "No activities match your search.",
                                color = NookMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        filteredActivities.forEachIndexed { index, entry ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(entry) {
                                delay(70L * index)
                                visible = true
                            }

                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(durationMillis = 400)) +
                                        slideInVertically(
                                            initialOffsetY = { it / 3 },
                                            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing)
                                        ),
                                exit = fadeOut(animationSpec = tween(durationMillis = 300)) +
                                        shrinkVertically(animationSpec = tween(durationMillis = 300))
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = NookCard)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (entry.action) {
                                                        "Added" -> Color(0x332E7D32)
                                                        "Edited" -> Color(0x333E6FB0)
                                                        else -> Color(0x33C62828)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (entry.action) {
                                                    "Added" -> Icons.Filled.Add
                                                    "Edited" -> Icons.Filled.Edit
                                                    else -> Icons.Filled.Delete
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = when (entry.action) {
                                                    "Added" -> Color(0xFF2E7D32)
                                                    "Edited" -> Color(0xFF3E6FB0)
                                                    else -> Color(0xFFC62828)
                                                }
                                            )
                                        }

                                        Spacer(Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = buildAnnotatedString {
                                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                        append(entry.action)
                                                    }
                                                    append(" ")
                                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                                        append(entry.itemName)
                                                    }
                                                },
                                                fontSize = 14.sp,
                                                color = NookText
                                            )
                                            if (entry.zone.isNotBlank()) {
                                                Text(
                                                    text = "in ${entry.zone}",
                                                    fontSize = 12.sp,
                                                    color = NookMuted
                                                )
                                            }
                                        }

                                        Text(
                                            text = entry.time,
                                            fontSize = 11.sp,
                                            color = NookMuted
                                        )

                                        Spacer(Modifier.width(10.dp))

                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete activity entry",
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { onDeleteActivity(entry) },
                                            tint = NookMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentName: String,
    currentImageUri: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(currentImageUri?.let { Uri.parse(it) }) }
    var showZoneCamera by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    if (showZoneCamera) {
        Dialog(
            onDismissRequest = { showZoneCamera = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ZoneCameraScreen(
                onClose = { showZoneCamera = false },
                onImageCaptured = { uri ->
                    selectedImageUri = uri
                    showZoneCamera = false
                }
            )
        }
    }

    val bitmap = remember(selectedImageUri) {
        selectedImageUri?.let { uri ->
            try {
                loadOrientedBitmap(context, uri)
            } catch (_: Exception) {
                null
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = NookText) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Avatar Box
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(NookInput)
                        .clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Profile Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = NookMuted
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Take Photo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NookTeal,
                        modifier = Modifier.clickable { showZoneCamera = true }
                    )
                    Text("•", fontSize = 12.sp, color = NookMuted)
                    Text(
                        "Choose Gallery",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NookTeal,
                        modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("Full Name", fontSize = 12.sp, color = NookMuted, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Enter full name", color = NookMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NookCard,
                            unfocusedContainerColor = NookCard,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedImageUri?.toString())
                    } else {
                        Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
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
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (oldPassword: String, newPassword: String) -> Boolean
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password", fontWeight = FontWeight.Bold, color = NookText) },
        text = {
            Column {
                Text("Current Password", fontSize = 12.sp, color = NookMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                TextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    placeholder = { Text("Enter current password", color = NookMuted) },
                    singleLine = true,
                    visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { oldVisible = !oldVisible }) {
                            Icon(
                                if (oldVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = NookMuted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NookCard,
                        unfocusedContainerColor = NookCard,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text("New Password", fontSize = 12.sp, color = NookMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                TextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = { Text("Enter new password", color = NookMuted) },
                    singleLine = true,
                    visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(
                                if (newVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = NookMuted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NookCard,
                        unfocusedContainerColor = NookCard,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(12.dp))

                Text("Confirm New Password", fontSize = 12.sp, color = NookMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Confirm new password", color = NookMuted) },
                    singleLine = true,
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                if (confirmVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = NookMuted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NookCard,
                        unfocusedContainerColor = NookCard,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(context, "Please fill all password fields", Toast.LENGTH_SHORT).show()
                    } else if (newPassword.length < 4) {
                        Toast.makeText(context, "New password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                    } else if (newPassword != confirmPassword) {
                        Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        val success = onChangePassword(oldPassword, newPassword)
                        if (success) {
                            Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        } else {
                            Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NookTeal)
            ) { Text("Save Password", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = NookMuted) }
        },
        containerColor = NookCream
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    tint: Color = NookText,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint)
        Spacer(Modifier.width(12.dp))
        Text(label, color = tint, fontSize = 14.sp, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun SettingsScreen(
    userName: String = "Guest",
    userEmail: String = "guest101@gmail.com",
    userProfileImageUri: String? = null,
    isDarkMode: Boolean = false,
    onDarkModeToggle: (Boolean) -> Unit = {},
    onUpdateProfile: (name: String, imageUri: String?) -> Unit = { _, _ -> },
    onChangePassword: (oldPassword: String, newPassword: String) -> Boolean = { _, _ -> false },
    onNavigateNotifications: () -> Unit = {},
    onNavigateReminders: () -> Unit = {},
    onNavigateBackupRestore: () -> Unit = {},
    onNavigateHelpCenter: () -> Unit = {},
    onNavigateContactUs: () -> Unit = {},
    onLogout: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = userName,
            currentImageUri = userProfileImageUri,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, newImageUri ->
                onUpdateProfile(newName, newImageUri)
                showEditProfileDialog = false
                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = onChangePassword
        )
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Settings")
        HorizontalDivider(color = NookInput)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            // Profile Card (Clickable to Edit)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEditProfileDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NookCard)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(NookInput),
                        contentAlignment = Alignment.Center
                    ) {
                        val profileBitmap = remember(userProfileImageUri) {
                            userProfileImageUri?.let { uriStr ->
                                try {
                                    val uri = Uri.parse(uriStr)
                                    loadOrientedBitmap(context, uri)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        }

                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                tint = NookText
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NookText
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = userEmail,
                            color = NookMuted,
                            fontSize = 12.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(18.dp),
                            tint = NookTeal
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = NookMuted
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Preferences", color = NookMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(Icons.Filled.ModeNight, "Dark Mode") {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onDarkModeToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NookTeal,
                            uncheckedThumbColor = NookMuted,
                            uncheckedTrackColor = NookInput
                        )
                    )
                }
                SettingsRow(
                    Icons.Filled.Notifications,
                    "Notifications",
                    onClick = onNavigateNotifications
                ) { Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText) }
                SettingsRow(Icons.Filled.Alarm, "Reminders", onClick = onNavigateReminders) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        null,
                        Modifier.size(20.dp),
                        NookText
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Data & Backup", color = NookMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(
                    Icons.Filled.Backup,
                    "Backup & Restore",
                    onClick = onNavigateBackupRestore
                ) { Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText) }
            }
            Spacer(Modifier.height(20.dp))
            Text("Support", color = NookMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(
                    Icons.Filled.HelpOutline,
                    "Help Center",
                    onClick = onNavigateHelpCenter
                ) { Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText) }
                SettingsRow(Icons.Filled.Send, "Contact Us", onClick = onNavigateContactUs) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        null,
                        Modifier.size(20.dp),
                        NookText
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("Account", color = NookMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(Icons.Filled.Lock, "Change Password", onClick = { showChangePasswordDialog = true }) {
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText)
                }
                HorizontalDivider(color = NookInput)
                SettingsRow(Icons.AutoMirrored.Filled.Logout, "Log out", tint = NookRed, onClick = onLogout) {
                    Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookRed)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
