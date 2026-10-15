package com.example.nook.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

class NookColorScheme(
    val cream: Color,
    val card: Color,
    val input: Color,
    val text: Color,
    val muted: Color,
    val teal: Color,
    val tealDark: Color,
    val tealChip: Color,
    val red: Color,
    val blue: Color,
    val isDark: Boolean
)

val LightNookColors = NookColorScheme(
    cream = Color(0xFFEFEAE3),
    card = Color(0xFFFBF8F2),
    input = Color(0xFFDFDAD3),
    text = Color(0xFF1C1B1A),
    muted = Color(0xFF888279),
    teal = Color(0xFF2A8C82),
    tealDark = Color(0xFF1E6B62),
    tealChip = Color(0xFF8FBCB4),
    red = Color(0xFFB33737),
    blue = Color(0xFF3E6FB0),
    isDark = false
)

val DarkNookColors = NookColorScheme(
    cream = Color(0xFF141312),
    card = Color(0xFF22201E),
    input = Color(0xFF2F2C29),
    text = Color(0xFFF4F0EA),
    muted = Color(0xFFA6A097),
    teal = Color(0xFF38A89D),
    tealDark = Color(0xFF2A8C82),
    tealChip = Color(0xFF2C5E57),
    red = Color(0xFFE55353),
    blue = Color(0xFF5B8CD0),
    isDark = true
)

val LocalNookColors = staticCompositionLocalOf { LightNookColors }

val NookCream: Color
    @Composable
    get() = LocalNookColors.current.cream

val NookCard: Color
    @Composable
    get() = LocalNookColors.current.card

val NookInput: Color
    @Composable
    get() = LocalNookColors.current.input

val NookText: Color
    @Composable
    get() = LocalNookColors.current.text

val NookMuted: Color
    @Composable
    get() = LocalNookColors.current.muted

val NookTeal: Color
    @Composable
    get() = LocalNookColors.current.teal

val NookTealDark: Color
    @Composable
    get() = LocalNookColors.current.tealDark

val NookTealChip: Color
    @Composable
    get() = LocalNookColors.current.tealChip

val NookRed: Color
    @Composable
    get() = LocalNookColors.current.red

val NookBlue: Color
    @Composable
    get() = LocalNookColors.current.blue
