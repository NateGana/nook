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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.nook.data.*
import com.example.nook.ui.components.*
import com.example.nook.ui.theme.*

@Composable
fun ZonesScreen(
    zones: List<Zone>,
    hasUnreadNotifications: Boolean = false,
    onAddZone: (String, String?) -> Unit,
    onSelectZone: (Zone) -> Unit,
    onNotificationClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showAddDialog) {
        AddZoneDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newZoneName, newZoneImageUri ->
                onAddZone(newZoneName, newZoneImageUri)
            }
        )
    }

    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Zones", right = { BadgedBell(hasUnread = hasUnreadNotifications, onClick = onNotificationClick) })
        HorizontalDivider(color = NookInput)
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .imePadding()
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(NookInput)
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
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text("Search zones...", color = NookMuted, fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                }
            )
        }
        Spacer(Modifier.height(20.dp))

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            val filteredZones = zones.filter { it.name.contains(query, ignoreCase = true) }
            if (filteredZones.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Text("No zones found", color = NookMuted, fontSize = 14.sp)
                }
            } else {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredZones.forEachIndexed { index, zone ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(zone) {
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
                                    .clickable { onSelectZone(zone) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = NookCard)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NookInput),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val bitmap = remember(zone.imageUri) {
                                            zone.imageUri?.let { uriStr ->
                                                try {
                                                    val uri = Uri.parse(uriStr)
                                                    loadOrientedBitmap(context, uri)
                                                } catch (_: Exception) {
                                                    null
                                                }
                                            }
                                        }

                                        if (bitmap != null) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = zone.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else if (zone.image != 0) {
                                            Image(
                                                painter = painterResource(id = zone.image),
                                                contentDescription = zone.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.Bookmark,
                                                contentDescription = null,
                                                modifier = Modifier.size(28.dp),
                                                tint = NookTeal
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(16.dp))

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            zone.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = NookText
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            zone.meta,
                                            fontSize = 13.sp,
                                            color = NookMuted
                                        )
                                    }

                                    Icon(
                                        Icons.Filled.ChevronRight,
                                        null,
                                        Modifier.size(20.dp),
                                        NookMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(16.dp))
        NookFilledButton("Add zone", { showAddDialog = true }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ZoneDetailScreen(
    zone: Zone?,
    items: List<NookItem>,
    allZones: List<Zone>,
    onUpdateZone: (Zone) -> Unit,
    onDeleteZone: (Zone) -> Unit,
    onUpdateItem: (NookItem, NookItem) -> Unit,
    onDeleteItem: (NookItem) -> Unit,
    onBack: () -> Unit
) {
    if (zone == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showChangeImageDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<NookItem?>(null) }
    var itemToDeleteConfirm by remember { mutableStateOf<NookItem?>(null) }
    var showDeleteZoneConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showChangeImageDialog) {
        AddZoneDialog(
            onDismiss = { showChangeImageDialog = false },
            onAdd = { _, newImageUri ->
                if (newImageUri != null) {
                    onUpdateZone(zone.copy(imageUri = newImageUri))
                    Toast.makeText(context, "Zone photo updated!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showEditNameDialog) {
        var newName by remember { mutableStateOf(zone.name) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Zone Name", fontWeight = FontWeight.Bold, color = NookText) },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Zone name", color = NookMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NookCard,
                        unfocusedContainerColor = NookCard
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onUpdateZone(zone.copy(name = newName.trim()))
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NookTeal)
                ) { Text("Save", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = NookMuted)
                }
            },
            containerColor = NookCream
        )
    }

    if (showDeleteZoneConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteZoneConfirm = false },
            title = { Text("Delete Zone?", fontWeight = FontWeight.Bold, color = NookText) },
            text = { Text("Are you sure you want to delete '${zone.name}' and all its items?", color = NookText, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteZoneConfirm = false
                        onDeleteZone(zone)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteZoneConfirm = false }) {
                    Text("Cancel", color = NookMuted)
                }
            },
            containerColor = NookCream
        )
    }

    if (itemToEdit != null) {
        EditItemDialog(
            item = itemToEdit!!,
            zones = allZones,
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
        NookTopBar(
            title = zone.name,
            onBack = onBack,
            right = {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete Zone",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { showDeleteZoneConfirm = true },
                    tint = Color(0xFFD32F2F)
                )
            }
        )
        HorizontalDivider(color = NookInput)
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .imePadding()
        ) {

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Zone Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NookCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NookInput)
                            .clickable { showChangeImageDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = remember(zone.imageUri) {
                            zone.imageUri?.let { uriStr ->
                                try {
                                    val uri = Uri.parse(uriStr)
                                    loadOrientedBitmap(context, uri)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = zone.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (zone.image != 0) {
                            Image(
                                painter = painterResource(id = zone.image),
                                contentDescription = zone.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Filled.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = NookTeal
                            )
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Tap to change photo",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = zone.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NookText
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit Zone Name",
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { showEditNameDialog = true },
                            tint = NookTeal
                        )
                    }

                    Text(
                        text = "${items.size} ${if (items.size == 1) "item" else "items"}",
                        fontSize = 13.sp,
                        color = NookMuted
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Items in this Zone",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NookText
            )
            Spacer(Modifier.height(10.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NookCard),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = NookMuted
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "No items in this zone yet",
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
                    items.forEach { item ->
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
                                    if (item.category.isNotBlank()) {
                                        Text(
                                            text = item.category,
                                            fontSize = 12.sp,
                                            color = NookMuted
                                        )
                                    }
                                    if (item.description.isNotBlank()) {
                                        Text(
                                            text = item.description,
                                            fontSize = 12.sp,
                                            color = NookMuted,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit Item",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { itemToEdit = item },
                                        tint = NookTeal
                                    )
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete Item",
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { itemToDeleteConfirm = item },
                                        tint = Color(0xFFD32F2F)
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
fun EditItemDialog(
    item: NookItem,
    zones: List<Zone>,
    onDismiss: () -> Unit,
    onSave: (NookItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var category by remember { mutableStateOf(item.category) }
    var zoneName by remember { mutableStateOf(item.zone) }
    var desc by remember { mutableStateOf(item.description) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(item.imageUri?.let { Uri.parse(it) }) }
    var showZoneCamera by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val categoriesList = remember {
        mutableStateListOf(
            "Electronics", "Furniture", "Office Supplies", "Kitchenware",
            "Documents", "Keys & Accessories", "Tools", "Clothing", "Miscellaneous"
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { newCategory ->
                if (!categoriesList.contains(newCategory)) {
                    categoriesList.add(newCategory)
                }
                category = newCategory
            }
        )
    }

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
        title = { Text("Edit Item", fontWeight = FontWeight.Bold, color = NookText) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NookCard)
                        .border(1.dp, NookInput, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Item Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (item.image != 0) {
                        Image(
                            painter = painterResource(id = item.image),
                            contentDescription = "Item Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Filled.CameraAlt, null, Modifier.size(32.dp), NookMuted)
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            null,
                            Modifier.size(16.dp).clickable { showZoneCamera = true },
                            Color.White
                        )
                        Icon(
                            Icons.Filled.PhotoLibrary,
                            null,
                            Modifier.size(16.dp).clickable { galleryLauncher.launch("image/*") },
                            Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text("Item Name *", fontSize = 12.sp, color = NookMuted)
                Spacer(Modifier.height(4.dp))
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NookCard,
                        unfocusedContainerColor = NookCard
                    )
                )

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        NookDropdownField(
                            label = "Category",
                            selectedOption = category,
                            options = categoriesList,
                            onOptionSelected = { category = it },
                            onAddAction = { showAddCategoryDialog = true },
                            addActionLabel = "+ Add Category",
                            placeholder = "Category"
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        NookDropdownField(
                            label = "Zone",
                            selectedOption = zoneName,
                            options = zones.map { it.name },
                            onOptionSelected = { zoneName = it },
                            placeholder = "Zone"
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text("Description", fontSize = 12.sp, color = NookMuted)
                Spacer(Modifier.height(4.dp))
                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NookCard,
                        unfocusedContainerColor = NookCard
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            item.copy(
                                name = name.trim(),
                                category = category,
                                zone = zoneName,
                                description = desc,
                                imageUri = selectedImageUri?.toString() ?: item.imageUri
                            )
                        )
                    } else {
                        Toast.makeText(context, "Item Name is required", Toast.LENGTH_SHORT).show()
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
