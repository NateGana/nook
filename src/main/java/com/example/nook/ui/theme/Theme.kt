package com.example.nook.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun NookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val nookColors = if (darkTheme) DarkNookColors else LightNookColors

    val materialColors = if (darkTheme) {
        darkColorScheme(
            primary = nookColors.teal,
            onPrimary = Color.White,
            secondary = nookColors.teal,
            onSecondary = Color.White,
            background = nookColors.cream,
            onBackground = nookColors.text,
            surface = nookColors.card,
            onSurface = nookColors.text
        )
    } else {
        lightColorScheme(
            primary = nookColors.teal,
            onPrimary = Color.White,
            secondary = nookColors.teal,
            onSecondary = Color.White,
            background = nookColors.cream,
            onBackground = nookColors.text,
            surface = nookColors.card,
            onSurface = nookColors.text
        )
    }

    CompositionLocalProvider(LocalNookColors provides nookColors) {
        MaterialTheme(
            colorScheme = materialColors,
            content = content
        )
    }
}
