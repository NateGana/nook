package com.example.nook

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nook.ui.theme.*



@Composable
fun ActivityScreen() {
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Activity", right = { BadgedBell() })
        HorizontalDivider(color = NookInput)
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
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
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ZoneChip("All", true)
                ZoneChip("Added", false)
                ZoneChip("Checked", false)
                ZoneChip("Removed", false)
            }
        }
    }
}

@Composable
fun SettingsRow(icon: ImageVector, label: String, trailing: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(20.dp), NookText)
        Spacer(Modifier.width(12.dp))
        Text(label, color = NookText, fontSize = 14.sp, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun SettingsScreen() {
    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Settings")
        HorizontalDivider(color = NookInput)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.AccountCircle, null, Modifier.size(48.dp), NookText)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Guest", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NookText)
                    Text(
                        "guest101@gmail.com",
                        color = NookMuted,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Preferences", color = NookMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(Icons.Filled.ModeNight, "Dark Mode") {
                    Switch(
                        checked = false,
                        onCheckedChange = {},
                        enabled = false
                    )
                }
                SettingsRow(
                    Icons.Filled.Notifications,
                    "Notifications"
                ) { Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText) }
                SettingsRow(Icons.Filled.Alarm, "Reminders") {
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
                    "Backup & Restore"
                ) { Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText) }
            }
            Spacer(Modifier.height(20.dp))
            Text("Support", color = NookMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(12.dp))) {
                SettingsRow(
                    Icons.Filled.HelpOutline,
                    "Help Center"
                ) { Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookText) }
                SettingsRow(Icons.Filled.Send, "Contact Us") {
                    Icon(
                        Icons.Filled.ChevronRight,
                        null,
                        Modifier.size(20.dp),
                        NookText
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}