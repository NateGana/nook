package com.example.nook.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nook.data.*
import com.example.nook.ui.components.*
import com.example.nook.ui.theme.*

@Composable
fun HomeTopRow(
    userName: String = "Guest",
    profileImageUri: String? = null,
    hasUnreadNotifications: Boolean = false,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("nook", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = NookText)
        Spacer(Modifier.weight(1f))
        BadgedBell(hasUnread = hasUnreadNotifications, onClick = onNotificationClick)
        Spacer(Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(NookInput)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            val profileBitmap = remember(profileImageUri) {
                profileImageUri?.let { uriStr ->
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
                    contentDescription = "Profile",
                    modifier = Modifier.fillMaxSize(),
                    tint = NookText
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    items: List<NookItem>,
    zones: List<Zone> = emptyList(),
    userName: String = "Guest",
    userProfileImageUri: String? = null,
    hasUnreadNotifications: Boolean = false,
    onProfileClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onUpdateItem: (oldItem: NookItem, updatedItem: NookItem) -> Unit = { _, _ -> },
    onDeleteItem: (itemToDelete: NookItem) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var itemToEdit by remember { mutableStateOf<NookItem?>(null) }
    var itemToDeleteConfirm by remember { mutableStateOf<NookItem?>(null) }
    val context = LocalContext.current

    if (itemToEdit != null) {
        EditItemDialog(
            item = itemToEdit!!,
            zones = zones,
            onDismiss = { itemToEdit = null },
            onSave = { updatedItem ->
                onUpdateItem(itemToEdit!!, updatedItem)
                itemToEdit = null
                Toast.makeText(context, "Item updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (itemToDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { itemToDeleteConfirm = null },
            title = { Text("Delete Item?", fontWeight = FontWeight.Bold, color = NookText) },
            text = { Text("Are you sure you want to delete '${itemToDeleteConfirm?.name}'?", color = NookText, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteItem(itemToDeleteConfirm!!)
                        itemToDeleteConfirm = null
                        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeleteConfirm = null }) {
                    Text("Cancel", color = NookMuted)
                }
            },
            containerColor = NookCream
        )
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        HomeTopRow(
            userName = userName,
            profileImageUri = userProfileImageUri,
            hasUnreadNotifications = hasUnreadNotifications,
            onProfileClick = onProfileClick,
            onNotificationClick = onNotificationClick
        )
        HorizontalDivider(color = NookInput)
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .imePadding()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(NookInput)
                    .padding(horizontal = 16.dp)
                    .heightIn(min = 48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                Text("Where's my...", color = NookMuted, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        Spacer(Modifier.height(20.dp))

        Text(
            text = "Your Items",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NookText,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(12.dp))

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val filteredItems = items.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.zone.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }

            if (filteredItems.isEmpty()) {
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
                            Icons.Filled.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = NookMuted
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (items.isEmpty()) "No items saved yet. Tap + to add an item!" else "No items found matching '$query'",
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
                    filteredItems.forEachIndexed { index, item ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(item) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { itemToEdit = item },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = NookCard)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NookInput),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val itemBitmap = remember(item.imageUri) {
                                            item.imageUri?.let { uriStr ->
                                                try {
                                                    val uri = Uri.parse(uriStr)
                                                    loadOrientedBitmap(context, uri)
                                                } catch (_: Exception) {
                                                    null
                                                }
                                            }
                                        }

                                        if (itemBitmap != null) {
                                            Image(
                                                bitmap = itemBitmap.asImageBitmap(),
                                                contentDescription = item.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else if (item.image != 0) {
                                            Image(
                                                painter = painterResource(id = item.image),
                                                contentDescription = item.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.Inventory2,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                                tint = NookTeal
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = NookText
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.Place,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = NookTeal
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = if (item.zone.isNotBlank()) "in ${item.zone}" else "No zone specified",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = NookTeal
                                            )
                                        }
                                        if (item.category.isNotBlank()) {
                                            Text(
                                                text = item.category,
                                                fontSize = 12.sp,
                                                color = NookMuted
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Edit Item",
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { itemToEdit = item },
                                            tint = NookTeal
                                        )
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete Item",
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clickable { itemToDeleteConfirm = item },
                                            tint = Color(0xFFD32F2F)
                                        )
                                    }
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
