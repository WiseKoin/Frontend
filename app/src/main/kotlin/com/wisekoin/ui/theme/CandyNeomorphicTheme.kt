package com.wisekoin.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
val DarkBg = Color(0xFF191919)
val ReticleRed = Color(0xFFE53935)
val ControlBtnBg = Color(0xFF333333)
val BrandGreenDark = Color(0xFF235347)
val NeoSunkenBg = Color(0xFFE5ECF4)
val DashedBorderColor = Color(0xFFB8C8D2)

fun Modifier.neomorphicBox(
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

fun Modifier.neomorphicInset(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 3.5.dp,
    darkShadowColor: Color = Color(0xFFBAC7D5),
    lightShadowColor: Color = Color(0xFFFFFFFF),
    backgroundColor: Color = NeoSunkenBg
) = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor)
    .drawBehind {
        val cornerPx = cornerRadius.toPx()
        val elevationPx = elevation.toPx()

        // 1. Đổ bóng tối ở góc trên & trái vào trong
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(darkShadowColor.copy(alpha = 0.6f), Color.Transparent),
                startY = 0f,
                endY = elevationPx * 2.2f
            ),
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(darkShadowColor.copy(alpha = 0.45f), Color.Transparent),
                startX = 0f,
                endX = elevationPx * 2.2f
            ),
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )

        // 2. Đổ viền sáng ở góc dưới & phải vào trong
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, lightShadowColor.copy(alpha = 0.85f)),
                startY = size.height - elevationPx * 2f,
                endY = size.height
            ),
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )
    }

fun Modifier.dashedBorder(
    width: Dp = 1.5.dp,
    color: Color = DashedBorderColor,
    cornerRadius: Dp = 20.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
) = this.drawBehind {
    val strokeWidthPx = width.toPx()
    val cornerPx = cornerRadius.toPx()
    val dashPx = dashLength.toPx()
    val gapPx = gapLength.toPx()

    val stroke = Stroke(
        width = strokeWidthPx,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
    )

    val halfStroke = strokeWidthPx / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(halfStroke, halfStroke),
        size = Size(
            size.width - strokeWidthPx,
            size.height - strokeWidthPx
        ),
        cornerRadius = CornerRadius(cornerPx, cornerPx),
        style = stroke
    )
}

fun Modifier.neomorphicCircle(
    shadowColor: Color = Color.Black.copy(alpha = 0.15f),
    lightColor: Color = Color.White,
    offset: Dp = 3.dp,
    blur: Dp = 6.dp
): Modifier = this.drawBehind {
    val radius = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)

    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()

        frameworkPaint.color = lightColor.toArgb()
        if (blur > 0.dp) {
            frameworkPaint.maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(
            center = center + Offset(-offset.toPx(), -offset.toPx()),
            radius = radius,
            paint = paint
        )

        frameworkPaint.color = shadowColor.toArgb()
        if (blur > 0.dp) {
            frameworkPaint.maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawCircle(
            center = center + Offset(offset.toPx(), offset.toPx()),
            radius = radius,
            paint = paint
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