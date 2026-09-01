package com.wisekoin.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object WiseKoinColors {
    val Primary = Color(0xFFAC3236)
    val PrimaryContainer = Color(0xFFFF6F6F)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnPrimaryContainer = Color(0xFF700012)

    val Secondary = Color(0xFF666000)
    val SecondaryContainer = Color(0xFFEEE35F)
    val OnSecondary = Color(0xFFFFFFFF)

    val Background = Color(0xFFFDF7FF)
    val OnBackground = Color(0xFF1F005F)

    val Surface = Color(0xFFFDF7FF)
    val SurfaceContainerLow = Color(0xFFF8F1FF)
    val SurfaceContainer = Color(0xFFF2EBFF)
    val SurfaceContainerHighest = Color(0xFFE7DEFF)

    val OnSurface = Color(0xFF1F005F)
    val OnSurfaceVariant = Color(0xFF584140)
    val Outline = Color(0xFF8C716F)
}

val WiseKoinLightColorScheme = lightColorScheme(
    primary = WiseKoinColors.Primary,
    onPrimary = WiseKoinColors.OnPrimary,
    primaryContainer = WiseKoinColors.PrimaryContainer,
    onPrimaryContainer = WiseKoinColors.OnPrimaryContainer,
    secondary = WiseKoinColors.Secondary,
    secondaryContainer = WiseKoinColors.SecondaryContainer,
    background = WiseKoinColors.Background,
    onBackground = WiseKoinColors.OnBackground,
    surface = WiseKoinColors.Surface,
    onSurface = WiseKoinColors.OnSurface,
    onSurfaceVariant = WiseKoinColors.OnSurfaceVariant,
    outline = WiseKoinColors.Outline
)

object WiseKoinTheme {
    val colors: WiseKoinColors
        @Composable
        @ReadOnlyComposable
        get() = WiseKoinColors
}

@Composable
fun WiseKoinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WiseKoinLightColorScheme,
        content = content
    )
}

fun Modifier.neomorphicCardShadow(
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 6.dp
): Modifier = this.drawBehind {
    val elevationPx = elevation.toPx()
    val lightShadowColor = NeoShadowLight
    val darkShadowColor = NeoShadowDark

    drawIntoCanvas { canvas ->
        val lightPaint = Paint().apply {
            color = lightShadowColor
            asFrameworkPaint().apply {
                isAntiAlias = true
                setShadowLayer(elevationPx * 1.5f, -elevationPx, -elevationPx, lightShadowColor.hashCode())
            }
        }
        val darkPaint = Paint().apply {
            color = darkShadowColor
            asFrameworkPaint().apply {
                isAntiAlias = true
                setShadowLayer(elevationPx * 1.5f, elevationPx, elevationPx, darkShadowColor.hashCode())
            }
        }

        if (shape == CircleShape) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            canvas.drawCircle(center, radius, lightPaint)
            canvas.drawCircle(center, radius, darkPaint)
        } else {
            val cornerPx = 24.dp.toPx()
            canvas.drawRoundRect(
                left = 0f, top = 0f,
                right = size.width, bottom = size.height,
                radiusX = cornerPx, radiusY = cornerPx,
                paint = lightPaint
            )
            canvas.drawRoundRect(
                left = 0f, top = 0f,
                right = size.width, bottom = size.height,
                radiusX = cornerPx, radiusY = cornerPx,
                paint = darkPaint
            )
        }
    }
}
