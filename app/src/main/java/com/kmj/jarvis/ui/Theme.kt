package com.kmj.jarvis.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Dark = darkColorScheme(
    primary = Color(0xFF8B5CF6),
    onPrimary = Color(0xFF1B0B3B),
    primaryContainer = Color(0xFF3B2A6B),
    onPrimaryContainer = Color(0xFFE9DEFF),
    secondary = Color(0xFFB7A8E8),
    onSecondary = Color(0xFF241640),
    tertiary = Color(0xFFFFC24B),
    onTertiary = Color(0xFF3D2A00),
    tertiaryContainer = Color(0xFF5C4300),
    onTertiaryContainer = Color(0xFFFFE0A3),
    background = Color(0xFF0D0B16),
    onBackground = Color(0xFFEAE6F7),
    surface = Color(0xFF0D0B16),
    onSurface = Color(0xFFEAE6F7),
    surfaceVariant = Color(0xFF1C1730),
    onSurfaceVariant = Color(0xFFA99EC9),
    surfaceContainer = Color(0xFF151125),
    outline = Color(0xFF3A2F5C),
    outlineVariant = Color(0xFF292147),
    error = Color(0xFFFF8A80),
)

private val Light = lightColorScheme(
    primary = Color(0xFF6D28D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE7DCFF),
    onPrimaryContainer = Color(0xFF2A0F66),
    secondary = Color(0xFF62578D),
    onSecondary = Color.White,
    tertiary = Color(0xFFC77C12),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0A3),
    onTertiaryContainer = Color(0xFF4A3400),
    background = Color(0xFFF7F5FB),
    onBackground = Color(0xFF1B1730),
    surface = Color(0xFFF7F5FB),
    onSurface = Color(0xFF1B1730),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF5E5678),
    surfaceContainer = Color(0xFFFFFFFF),
    outline = Color(0xFFD9D3E8),
    outlineVariant = Color(0xFFE8E3F2),
    error = Color(0xFFB3261E),
)

private val Type = Typography(
    displaySmall = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp),
)

private val Corners = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

/** KMJ JARVIS purple-and-gold look; follows the phone's light or dark setting. */
@Composable
fun KMJTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = Type,
        shapes = Corners,
        content = content,
    )
}

// Short names the screens use; they follow light and dark mode.
val Accent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
val Background: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
val Surface: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceVariant
val Muted: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
val Warning: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.tertiary
