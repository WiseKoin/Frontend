package com.wisekoin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Candy Neomorphic Color Palette
val CandyBg = Color(0xFFF0F5F9)
val PrimaryRed = Color(0xFFAC3236)
val MintDark = Color(0xFF75D8B3)
val PeachDark = Color(0xFFFFB88C)
val LavenderDark = Color(0xFFB07D7D)
val LemonDark = Color(0xFFFFD54F)
val LemonLight = Color(0xFFFFECB3)
val TextDark = Color(0xFF161A32)
val TextLight = Color(0xFF9A8C98)
val NeoShadowDark = Color(0xFFD1D9E6)
val NeoShadowLight = Color(0xFFFFFFFF)

/**
 * Modifier for Soft Neomorphic Raised Box
 */
fun Modifier.neomorphic(
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 6.dp,
    lightShadowColor: Color = NeoShadowLight,
    darkShadowColor: Color = NeoShadowDark
) = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val elevationPx = elevation.toPx()

    drawIntoCanvas { canvas ->
        // 1. Top-Left Light Shadow
        val lightPaint = Paint().apply {
            color = lightShadowColor
            asFrameworkPaint().apply {
                isAntiAlias = true
                setShadowLayer(elevationPx * 1.5f, -elevationPx, -elevationPx, lightShadowColor.hashCode())
            }
        }
        canvas.drawRoundRect(
            left = 0f, top = 0f,
            right = size.width, bottom = size.height,
            radiusX = cornerPx, radiusY = cornerPx,
            paint = lightPaint
        )

        // 2. Bottom-Right Dark Shadow
        val darkPaint = Paint().apply {
            color = darkShadowColor
            asFrameworkPaint().apply {
                isAntiAlias = true
                setShadowLayer(elevationPx * 1.5f, elevationPx, elevationPx, darkShadowColor.hashCode())
            }
        }
        canvas.drawRoundRect(
            left = 0f, top = 0f,
            right = size.width, bottom = size.height,
            radiusX = cornerPx, radiusY = cornerPx,
            paint = darkPaint
        )
    }
}

val NunitoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = TextDark
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = TextDark
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = TextDark
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = TextLight
    )
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    background = CandyBg,
    surface = CandyBg,
    onPrimary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun CandyNeomorphicTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = NunitoTypography,
        content = content
    )
}
