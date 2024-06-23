package com.example.fooditeminventory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC), // purple_200
    primaryContainer = Color(0xFF3700B3), // purple_700
    onPrimary = Color.Black,
    secondary = Color(0xFF03DAC5), // teal_200
    onSecondary = Color.Black,
    background = Color.Black,
    onBackground = Color.DarkGray,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceDim = Color(0xFF121212) // Custom dimmed surface for dark theme
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE), // purple_500
    primaryContainer = Color(0xFF3700B3), // purple_700
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC5), // teal_200
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.White,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceDim = Color.White
)

@Composable
fun FoodItemInventoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        content = content
    )


}

