package com.example.nook

import com.example.nook.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nook.ui.theme.*

private val overlayDark = Color(0x99333333)

@Composable
fun AddItemCameraScreen(onClose: () -> Unit, onCapture: () -> Unit) {
    Column(Modifier.fillMaxSize().background(NookCream)) {
        NookTopBar("Add Item", onClose)
        Box(Modifier.fillMaxSize()) {
            Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.align(Alignment.TopStart).padding(16.dp).size(40.dp).clip(CircleShape)
                .background(overlayDark).clickable { onClose() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Close, "Close", Modifier.size(20.dp), Color.White)
            }
            Box(Modifier.align(Alignment.TopEnd).padding(16.dp).size(40.dp).clip(CircleShape)
                .background(overlayDark), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.FlashOn, "Flash", Modifier.size(20.dp), Color.White)
            }
            Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(24.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(overlayDark), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.PhotoLibrary, "Gallery", Modifier.size(22.dp), Color.White)
                }
                Box(Modifier.size(76.dp).border(4.dp, Color.White, CircleShape).clickable { onCapture() },
                    contentAlignment = Alignment.Center) {
                    Box(Modifier.size(58.dp).background(Color.White, CircleShape))
                }
                Box(Modifier.size(48.dp).clip(CircleShape).background(overlayDark), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.PhotoCamera, "Camera", Modifier.size(22.dp), Color.White)
                }
            }
        }
    }
}

@Composable
fun LabeledField(label: String, value: String, onValueChange: (String) -> Unit) {
    Text(label, fontSize = 13.sp, color = NookText)
    Spacer(Modifier.height(6.dp))
    TextField(value, onValueChange, Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = NookCard, unfocusedContainerColor = NookCard,
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
    Spacer(Modifier.height(16.dp))
}

@Composable
fun AddItemFormScreen(onBack: () -> Unit, onSave: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .background(NookCream)
            .imePadding()
    ) {
        NookTopBar("Add Item", onBack)
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Image(painterResource(R.drawable.ic_launcher_background), null,
                Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Text("Change photo", color = NookTeal, fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 6.dp))
            Spacer(Modifier.height(16.dp))
            LabeledField("Item Name", name) { name = it }
            LabeledField("Category", category) { category = it }
            LabeledField("Zone", zone) { zone = it }
            LabeledField("Description (optional)", desc) { desc = it }
            Text("Tag (optional)", fontSize = 13.sp, color = NookText)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().background(NookCard, RoundedCornerShape(8.dp)).padding(12.dp)) {
                Text("Add tag...", color = NookMuted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(24.dp))
            NookFilledButton("Save Item", onSave, Modifier.fillMaxWidth())
            Spacer(Modifier.navigationBarsPadding())
            Spacer(Modifier.height(16.dp))
        }
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