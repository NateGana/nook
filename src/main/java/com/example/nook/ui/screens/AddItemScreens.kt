package com.example.nook.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nook.data.*
import com.example.nook.ui.components.*
import com.example.nook.ui.theme.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private val overlayDark = Color(0x99333333)

@Composable
fun AddItemCameraScreen(onClose: () -> Unit, onImageCaptured: (Uri) -> Unit) {
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
                    Text("Camera permission is required to take photos of your items.", color = Color.White, fontSize = 15.sp, textAlign = TextAlign.Center)
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
                    text = "Take Photo",
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
                                "nook_${System.currentTimeMillis()}.jpg"
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
fun AddItemFormScreen(
    onBack: () -> Unit,
    imageUri: String?,
    onChangePhoto: () -> Unit,
    zones: List<Zone>,
    onAddZone: (String, String?) -> Unit,
    onSaveItem: (name: String, category: String, zone: String, desc: String, imageUri: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var showAddZoneDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val categoriesList = remember {
        mutableStateListOf(
            "Electronics", "Furniture", "Office Supplies", "Kitchenware",
            "Documents", "Keys & Accessories", "Tools", "Clothing", "Miscellaneous"
        )
    }

    if (showAddZoneDialog) {
        AddZoneDialog(
            onDismiss = { showAddZoneDialog = false },
            onAdd = { newZoneName, newZoneImageUri ->
                onAddZone(newZoneName, newZoneImageUri)
                zone = newZoneName
            }
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

    val bitmap = remember(imageUri) {
        imageUri?.let {
            try {
                val uri = Uri.parse(it)
                loadOrientedBitmap(context, uri)
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(NookCream)
            .imePadding()
    ) {
        NookTopBar("Add Item", onBack)

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(NookCard)
                        .border(1.5.dp, NookInput, RoundedCornerShape(16.dp))
                        .clickable { onChangePhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Photo Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CameraAlt, null, Modifier.size(32.dp), NookMuted)
                            Spacer(Modifier.height(8.dp))
                            Text("[Photo Preview]", color = NookMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.clickable { onChangePhoto() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.PhotoCamera, null, Modifier.size(14.dp), NookTeal)
                    Spacer(Modifier.width(6.dp))
                    Text("Change Photo", color = NookTeal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(20.dp))

                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
                    Text("Item Name *", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NookText)
                    Spacer(Modifier.height(6.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Enter item name", color = NookMuted, fontSize = 13.sp) },
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

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.weight(1f)) {
                            NookDropdownField(
                                label = "Category *",
                                selectedOption = category,
                                options = categoriesList,
                                onOptionSelected = { category = it },
                                onAddAction = { showAddCategoryDialog = true },
                                addActionLabel = "+ Add Category",
                                placeholder = "Select category"
                            )
                        }
                        Box(Modifier.weight(1f)) {
                            NookDropdownField(
                                label = "Zone",
                                selectedOption = zone,
                                options = zones.map { it.name },
                                onOptionSelected = { zone = it },
                                onAddAction = { showAddZoneDialog = true },
                                addActionLabel = "+ Add Zone",
                                placeholder = "Select zone"
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Text("Description", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NookText)
                    Spacer(Modifier.height(6.dp))
                    TextField(
                        value = desc,
                        onValueChange = { desc = it },
                        placeholder = { Text("Add additional details...", color = NookMuted, fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = NookCard,
                            unfocusedContainerColor = NookCard,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            NookFilledButton("Save Item", {
                if (name.isNotBlank() && category.isNotBlank()) {
                    onSaveItem(name.trim(), category, zone, desc, imageUri)
                } else {
                    Toast.makeText(context, "Please fill Item Name and Category", Toast.LENGTH_SHORT).show()
                }
            }, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
fun ItemSavedScreen(onViewItem: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().background(NookCream).statusBarsPadding().navigationBarsPadding().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CheckBadge()
        Spacer(Modifier.height(20.dp))
        Text("Item Saved!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = NookText)
        Spacer(Modifier.height(12.dp))
        Text("Your item has been saved.", fontSize = 14.sp, color = NookText)
        Spacer(Modifier.height(56.dp))
        NookFilledButton("View Item", onViewItem, Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        NookOutlinedButton("Back to Home", onHome, Modifier.fillMaxWidth())
    }
}
