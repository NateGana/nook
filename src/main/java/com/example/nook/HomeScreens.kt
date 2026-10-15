package com.example.nook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nook.ui.theme.*

@Composable
fun HomeTopRow() {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text("nook", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = NookText)
        Spacer(Modifier.weight(1f))
        BadgedBell()
        Spacer(Modifier.width(16.dp))
        Icon(Icons.Filled.AccountCircle, "Profile", Modifier.size(32.dp), NookText)
    }
}

@Composable
fun HomeScreen() {
    var query by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(NookCream)
            .imePadding()
    ) {
        HomeTopRow()
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
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
        Text("Your Zones", 
            fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NookText,
            modifier = Modifier.padding(horizontal = 20.dp))
        
        Spacer(Modifier.weight(1f))
    }
}
