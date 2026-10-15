package com.example.nook.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import com.example.nook.R
import com.example.nook.data.*
import com.example.nook.ui.theme.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

enum class Tab { Home, Zones, Activity, Settings }

@Composable
fun NookFilledButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick, modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NookTeal, contentColor = Color.White),
        shape = RoundedCornerShape(10.dp)) { Text(text, fontSize = 15.sp) }
}

@Composable
fun NookOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = NookTeal) {
    TextButton(onClick,
        modifier.height(48.dp).border(1.5.dp, color, RoundedCornerShape(10.dp)),
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        shape = RoundedCornerShape(10.dp)) { Text(text, fontSize = 15.sp) }
}

@Composable
fun Photo(@DrawableRes res: Int, modifier: Modifier = Modifier) {
    Image(painterResource(id = res), null,
        modifier.clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
}

@Composable
fun NookLogo(size: Int) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "nook logo",
        modifier = Modifier.size(size.dp).clip(CircleShape),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun CheckBadge(size: Int = 120) {
    Icon(Icons.Filled.Verified, null, Modifier.size(size.dp), NookTeal)
}

@Composable
fun BadgedBell(
    hasUnread: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Notifications, "Notifications", Modifier.size(24.dp), NookText)
        if (hasUnread) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(NookTeal, CircleShape)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun RecentNotificationsDialog(
    activities: List<ActivityEntry>,
    onDismiss: () -> Unit,
    onViewAllActivity: () -> Unit,
    onClearAll: () -> Unit = {},
    onDeleteNotification: (ActivityEntry) -> Unit = {}
) {
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
                        onClearAll()
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

    val recentList = remember(activities) { activities.take(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = NookTeal
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Notifications",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = NookText
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = NookMuted
                        )
                    }
                }
                if (activities.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Clear All",
                            color = NookRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showClearConfirm = true }
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (recentList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NookCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.NotificationsNone,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = NookMuted
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "No recent activity recorded yet.",
                                color = NookMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Recent Activity Summary",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NookMuted
                    )
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentList.forEachIndexed { index, entry ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(entry) {
                                delay(90L * index)
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
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = NookCard,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
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
                                                modifier = Modifier.size(16.dp),
                                                tint = when (entry.action) {
                                                    "Added" -> Color(0xFF2E7D32)
                                                    "Edited" -> Color(0xFF3E6FB0)
                                                    else -> Color(0xFFC62828)
                                                }
                                            )
                                        }

                                        Spacer(Modifier.width(10.dp))

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
                                                fontSize = 13.sp,
                                                color = NookText
                                            )
                                            if (entry.zone.isNotBlank()) {
                                                Text(
                                                    text = "in ${entry.zone}",
                                                    fontSize = 11.sp,
                                                    color = NookMuted
                                                )
                                            }
                                        }

                                        Text(
                                            text = entry.time,
                                            fontSize = 10.sp,
                                            color = NookMuted
                                        )

                                        Spacer(Modifier.width(8.dp))

                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "Remove notification",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { onDeleteNotification(entry) },
                                            tint = NookMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onViewAllActivity()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NookTeal)
            ) {
                Text("View All Activity", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = NookMuted)
            }
        },
        containerColor = NookCream
    )
}

@Composable
fun NookTopBar(title: String, onBack: (() -> Unit)? = null, right: (@Composable () -> Unit)? = null) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
        onBack?.let {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                Modifier.align(Alignment.CenterStart).size(24.dp).clickable { it() }, NookText)
        }
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NookText)
        right?.let { Box(Modifier.align(Alignment.CenterEnd)) { it() } }
    }
}

@Composable
fun ZoneChip(label: String, selected: Boolean) {
    Box(Modifier.clip(RoundedCornerShape(16.dp))
        .background(if (selected) NookTeal else NookInput)
        .padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, color = if (selected) Color.White else NookMuted, fontSize = 13.sp)
    }
}

@Composable
fun TagChip(label: String) {
    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(NookTealChip).padding(horizontal = 10.dp, vertical = 5.dp)) {
        Text(label, color = NookTealDark, fontSize = 12.sp)
    }
}

@Composable
fun ItemRow(item: NookItem) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Photo(item.image, Modifier.size(50.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NookText)
            Text(item.zone, color = NookMuted, fontSize = 13.sp)
        }
        Text(item.time, color = NookMuted, fontSize = 12.sp)
    }
}

@Composable
fun ActivityRow(entry: ActivityEntry) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(NookInput), contentAlignment = Alignment.Center) {
            Icon(when(entry.action) {
                "Added" -> Icons.Filled.Add
                "Checked" -> Icons.Filled.Visibility
                else -> Icons.Filled.Delete
            }, null, Modifier.size(20.dp), NookTeal)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(entry.action) }
                append(" ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(entry.itemName) }
            }, fontSize = 14.sp, color = NookText)
            Text(entry.zone, color = NookMuted, fontSize = 12.sp)
        }
        Text(entry.time, color = NookMuted, fontSize = 12.sp)
    }
}

@Composable
fun ZoneRow(zone: Zone) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Photo(zone.image, Modifier.size(60.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(zone.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NookText)
            Text(zone.meta, color = NookMuted, fontSize = 13.sp)
        }
        Icon(Icons.Filled.ChevronRight, null, Modifier.size(20.dp), NookMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NookDropdownField(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    onAddAction: (() -> Unit)? = null,
    addActionLabel: String = "+ Add New Zone",
    placeholder: String = "Select option"
) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NookText)
        Spacer(Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption.ifEmpty { placeholder },
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NookCard,
                    unfocusedContainerColor = NookCard,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = if (selectedOption.isEmpty()) NookMuted else NookText,
                    unfocusedTextColor = if (selectedOption.isEmpty()) NookMuted else NookText
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(NookCard)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = NookText) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
                if (onAddAction != null) {
                    HorizontalDivider(color = NookInput)
                    DropdownMenuItem(
                        text = { Text(addActionLabel, color = NookTeal, fontWeight = FontWeight.Bold) },
                        onClick = {
                            expanded = false
                            onAddAction()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var categoryName by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Category", color = NookText, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Enter category name (e.g. Gaming, Collectibles)", fontSize = 13.sp, color = NookMuted)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    placeholder = { Text("Category name", color = NookMuted, fontSize = 13.sp) },
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
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onAdd(categoryName.trim())
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Please enter category name", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NookTeal)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NookMuted)
            }
        },
        containerColor = NookCream
    )
}

private val overlayDark = Color(0x99333333)

@Composable
fun ZoneCameraScreen(onClose: () -> Unit, onImageCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImageCaptured(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    var flashOn by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }

    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    LaunchedEffect(Unit) {
        try {
            val provider = suspendCancellableCoroutine<ProcessCameraProvider> { continuation ->
                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    try {
                        continuation.resume(future.get())
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }, androidx.core.content.ContextCompat.getMainExecutor(context))
            }
            cameraProvider = provider
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to initialize camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(cameraProvider, hasCameraPermission, isFrontCamera) {
        val provider = cameraProvider
        if (hasCameraPermission && provider != null) {
            try {
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val capture = ImageCapture.Builder().build()
                imageCapture = capture

                val cameraSelector = if (isFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                provider.unbindAll()
                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    capture
                )
                cameraControl = camera.cameraControl
                cameraControl?.enableTorch(flashOn)
            } catch (exc: Exception) {
                Toast.makeText(context, "Camera bind error: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(flashOn, cameraControl) {
        try {
            cameraControl?.enableTorch(flashOn)
        } catch (_: Exception) {
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("Camera permission is required to take photos of your zones.", color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    NookFilledButton("Grant Permission", { permissionLauncher.launch(android.Manifest.permission.CAMERA) })
                }
            }
        }

        // Top Overlay Bar
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(overlayDark)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }

            Surface(
                color = overlayDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Take Zone Photo",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (flashOn) Color(0xCC333333) else overlayDark)
                    .clickable {
                        flashOn = !flashOn
                        Toast.makeText(context, if (flashOn) "Flash ON" else "Flash OFF", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (flashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = "Toggle Flash",
                    modifier = Modifier.size(22.dp),
                    tint = if (flashOn) Color(0xFFFFD700) else Color.White
                )
            }
        }

        // Bottom Controls Overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(overlayDark)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.PhotoLibrary,
                    contentDescription = "Open Gallery",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .clickable {
                        val capture = imageCapture
                        if (capture != null) {
                            val photoFile = File(
                                context.cacheDir,
                                "zone_${System.currentTimeMillis()}.jpg"
                            )
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                            capture.takePicture(
                                outputOptions,
                                androidx.core.content.ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onError(exc: ImageCaptureException) {
                                        Toast.makeText(context, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        val savedUri = Uri.fromFile(photoFile)
                                        onImageCaptured(savedUri)
                                    }
                                }
                            )
                        } else {
                            Toast.makeText(context, "Camera not ready", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(Color.White, CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(overlayDark)
                    .clickable {
                        isFrontCamera = !isFrontCamera
                        Toast.makeText(context, if (isFrontCamera) "Front Camera" else "Rear Camera", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Cameraswitch,
                    contentDescription = "Switch Camera",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun AddZoneDialog(onDismiss: () -> Unit, onAdd: (String, String?) -> Unit) {
    var zoneName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
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
        title = { Text("Add New Zone", color = NookText, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enter zone name (e.g. Living Room, Kitchen)", fontSize = 13.sp, color = NookMuted)
                Spacer(Modifier.height(10.dp))
                TextField(
                    value = zoneName,
                    onValueChange = { zoneName = it },
                    placeholder = { Text("Zone name", color = NookMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = NookCard,
                        unfocusedContainerColor = NookCard,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(Modifier.height(16.dp))
                Text("Zone Picture", fontSize = 13.sp, color = NookMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))

                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NookCard)
                            .border(1.dp, NookInput, RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected Zone Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.PhotoCamera,
                                contentDescription = "Take Photo",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showZoneCamera = true },
                                tint = Color.White
                            )
                            Icon(
                                Icons.Filled.PhotoLibrary,
                                contentDescription = "Choose from Gallery",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { galleryLauncher.launch("image/*") },
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NookCard)
                                .border(1.dp, NookInput, RoundedCornerShape(12.dp))
                                .clickable { showZoneCamera = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.PhotoCamera,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = NookTeal
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Take Photo",
                                    color = NookTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(NookCard)
                                .border(1.dp, NookInput, RoundedCornerShape(12.dp))
                                .clickable { galleryLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Filled.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = NookTeal
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Choose Gallery",
                                    color = NookTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (zoneName.isNotBlank()) {
                        onAdd(zoneName.trim(), selectedImageUri?.toString())
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Please enter zone name", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NookTeal)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = NookMuted)
            }
        },
        containerColor = NookCream
    )
}

@Composable
fun NookBottomBar(current: Tab, onTab: (Tab) -> Unit, onAdd: () -> Unit) {
    Surface(color = NookCard, shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            BottomTab(Icons.Filled.Home, "Home", Tab.Home, current, onTab, Modifier.weight(1f))
            BottomTab(Icons.Filled.Bookmark, "Zones", Tab.Zones, current, onTab, Modifier.weight(1f))
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Box(Modifier.size(50.dp).clip(CircleShape).background(NookTeal).clickable { onAdd() },
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Add, "Add item", Modifier.size(28.dp), Color.White)
                }
            }
            BottomTab(Icons.Filled.AccessTime, "Activity", Tab.Activity, current, onTab, Modifier.weight(1f))
            BottomTab(Icons.Filled.Settings, "Settings", Tab.Settings, current, onTab, Modifier.weight(1f))
        }
    }
}

@Composable
private fun BottomTab(icon: ImageVector, label: String, tab: Tab, current: Tab, onTab: (Tab) -> Unit, modifier: Modifier) {
    val color = if (tab == current) NookTeal else NookText
    Column(modifier.clickable { onTab(tab) }.padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, label, Modifier.size(22.dp), color)
        Text(label, fontSize = 12.sp, color = color)
    }
}
