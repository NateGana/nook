package com.example.nook

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nook.ui.theme.*

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
fun BadgedBell() {
    Box {
        Icon(Icons.Filled.Notifications, "Notifications", Modifier.size(24.dp), NookText)
        Box(Modifier.size(6.dp).background(NookTeal, CircleShape).align(Alignment.TopEnd))
    }
}

@Composable
fun NookTopBar(title: String, onBack: (() -> Unit)? = null, right: (@Composable () -> Unit)? = null) {
    Box(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
        onBack?.let {
            Icon(Icons.Filled.ArrowBack, "Back",
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