package com.tanatos.symphonymusic2.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.styropyr0.prismal.PrismalBackdrop
import com.styropyr0.prismal.drawPrismalGlass
import com.styropyr0.prismal.effects.applyPrismalGlassEffects
import com.styropyr0.prismal.shapes.PrismalCapsule
import com.styropyr0.prismal.specular.PrismalSpecular

/**
 * 3D Liquid Glass Pebble Component for Symphony 2.0
 * Features:
 * - AGSL hardware refraction and blur
 * - Dynamic Material You extracted tint fringe
 * - Silver-to-dark specular meniscus cap
 * - Razor-sharp white highlight edge
 * - Bottom caustic crescent glow
 * - Obsidian glass body with spring physics on press
 */
@Composable
fun SymphonyPebbleGlass(
    backdrop: PrismalBackdrop,
    modifier: Modifier = Modifier,
    shape: () -> Shape = { PrismalCapsule() },
    isCircle: Boolean = false,
    cornerRadius: Dp = 24.dp,
    chromaticColor: Color = Color(0xFF4FA5FF),
    highlightIntensity: Float = 1.0f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "pebblePressScale"
    )

    val resolvedShape = remember(isCircle, cornerRadius) {
        if (isCircle) {
            { PrismalCapsule() }
        } else {
            shape
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .drawPrismalGlass(
                backdrop = backdrop,
                shape = resolvedShape,
                effects = {
                    applyPrismalGlassEffects(
                        density = this,
                        adaptiveLuminance = false,
                        luminance = 0.4f,
                        blurRadiusPx = 16.dp.toPx(),
                        refractionHeightPx = 18.dp.toPx(),
                        refractionAmountPx = 32.dp.toPx(),
                        useVibrancy = true,
                        chromaticAberration = 0.6f
                    )
                },
                specular = { PrismalSpecular.Default }
            )
            .drawWithCache {
                val w = size.width
                val h = size.height

                // Precompute top specular meniscus and bottom caustic arcs
                val topMeniscusBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCAD4DE).copy(alpha = 0.75f * highlightIntensity),
                        Color(0xFF8A98A6).copy(alpha = 0.40f * highlightIntensity),
                        Color(0xFF323B45).copy(alpha = 0.10f * highlightIntensity),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.55f
                )

                val bottomCausticBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        chromaticColor.copy(alpha = 0.18f * highlightIntensity),
                        Color.White.copy(alpha = 0.35f * highlightIntensity)
                    ),
                    startY = h * 0.7f,
                    endY = h
                )

                onDrawWithContent {
                    drawContent()

                    // 1. Top Specular Meniscus Cap
                    val topCapPath = Path().apply {
                        if (isCircle) {
                            moveTo(0f, h * 0.5f)
                            arcTo(
                                rect = Rect(Offset.Zero, Size(w, h)),
                                startAngleDegrees = -180f,
                                sweepAngleDegrees = 180f,
                                forceMoveTo = false
                            )
                            quadraticTo(w * 0.5f, h * 0.60f, 0f, h * 0.5f)
                            close()
                        } else {
                            val radiusPx = cornerRadius.toPx()
                            moveTo(0f, radiusPx)
                            arcTo(
                                rect = Rect(0f, 0f, radiusPx * 2, radiusPx * 2),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            lineTo(w - radiusPx, 0f)
                            arcTo(
                                rect = Rect(w - radiusPx * 2, 0f, w, radiusPx * 2),
                                startAngleDegrees = 270f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                            quadraticTo(w * 0.5f, h * 0.45f, 0f, radiusPx)
                            close()
                        }
                    }
                    drawPath(topCapPath, topMeniscusBrush)

                    // 2. Chromatic Dispersion Upper Rim Fringe (Dynamic Material You)
                    drawArc(
                        color = chromaticColor.copy(alpha = 0.85f * highlightIntensity),
                        startAngle = -150f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = Offset(1.5f, 1.5f),
                        size = Size(w - 3f, h - 3f),
                        style = Stroke(width = 2.2f)
                    )

                    // 3. Bottom Caustic Crescent Glow
                    val bottomCrescentPath = Path().apply {
                        moveTo(w * 0.15f, h)
                        quadraticTo(w * 0.5f, h * 0.78f, w * 0.85f, h)
                        close()
                    }
                    drawPath(bottomCrescentPath, bottomCausticBrush)

                    // 4. Razor Sharp Top Highlight Line
                    drawArc(
                        color = Color.White.copy(alpha = 0.90f * highlightIntensity),
                        startAngle = -120f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(1f, 1f),
                        size = Size(w - 2f, h - 2f),
                        style = Stroke(width = 1.6f)
                    )
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}
