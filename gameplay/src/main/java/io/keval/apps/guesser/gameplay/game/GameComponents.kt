package io.keval.apps.guesser.gameplay.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.keval.apps.guesser.core.ui.theme.GuesserBronzeDark
import io.keval.apps.guesser.core.ui.theme.GuesserChocolateDark
import io.keval.apps.guesser.core.ui.theme.GuesserGoldBorder
import io.keval.apps.guesser.core.ui.theme.GuesserGoldCream
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeBottom
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeMid
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeTop
import io.keval.apps.guesser.core.ui.theme.GuesserWoodBottom
import io.keval.apps.guesser.core.ui.theme.GuesserWoodTop
import io.keval.apps.guesser.domain.game.DigitFeedback

private val GoldFrameBrush = Brush.verticalGradient(listOf(GuesserGoldBorder, GuesserBronzeDark, GuesserGoldBorder))
private val TileEmptyBrush = Brush.verticalGradient(listOf(Color(0xFFE2BC86), Color(0xFFCD9D62), Color(0xFFBE8A50)))
private val TileExactBrush = Brush.verticalGradient(listOf(Color(0xFF7CC94E), Color(0xFF55A535), Color(0xFF3D8526)))
private val TileCloseBrush = Brush.verticalGradient(listOf(GuesserOrangeTop, GuesserOrangeMid, GuesserOrangeBottom))
private val TileAbsentBrush = Brush.verticalGradient(listOf(Color(0xFF7A746E), Color(0xFF5E5853), Color(0xFF48433F)))
private val KeyDeleteBrush = Brush.verticalGradient(listOf(Color(0xFFF2734F), Color(0xFFD9472A), Color(0xFFB2301A)))
private val KeySecondaryBrush = Brush.verticalGradient(listOf(GuesserWoodTop, GuesserWoodBottom))
private val RemarkFill = Color(0xE62A170D)
private val RowNumberColor = Color(0xFFE6BE80)
private val WinnerGreen = Color(0xFF9BDC62)
private val LightDigitColor = Color(0xFFFFF8EC)
private val DarkDigitColor = Color(0xFF4A2C17)
private val DigitShadow = Shadow(color = Color(0x99301608), offset = Offset(0f, 3f), blurRadius = 4f)

/**
 * Rounded wood panel with a gold frame. The texture is cropped (never stretched) so it keeps
 * its proportions on every screen size and aspect ratio.
 */
@Composable
fun WoodPanel(
    textureRes: Int,
    overlay: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val outerShape = RoundedCornerShape(cornerRadius)
    val innerShape = RoundedCornerShape((cornerRadius - 4.dp).coerceAtLeast(0.dp))
    Box(
        modifier = modifier
            .shadow(12.dp, outerShape, clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .background(GoldFrameBrush, outerShape)
            .padding(3.dp)
            .background(GuesserChocolateDark, innerShape)
            .padding(1.dp)
            .clip(innerShape),
    ) {
        Image(
            painter = painterResource(id = textureRes),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(overlay)
                .border(1.dp, GuesserGoldBorder.copy(alpha = 0.35f), innerShape),
        )
        content()
    }
}

@Composable
fun GameRoundIconButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(dampingRatio = 0.6f), label = "iconScale")

    Box(
        modifier = modifier
            .size(size.coerceAtLeast(48.dp))
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(8.dp, CircleShape, clip = false)
                .background(GoldFrameBrush, CircleShape)
                .padding(size * 0.06f)
                .background(Brush.verticalGradient(listOf(Color(0xFF6B3E1F), GuesserChocolateDark)), CircleShape)
                .border(1.dp, GuesserGoldBorder.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GuesserGoldBorder,
                modifier = Modifier.size(size * 0.55f),
            )
        }
    }
}

@Composable
fun GameModeBadge(
    text: String,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    val fontSize = with(LocalDensity.current) { (height * 0.46f).toSp() }
    Row(
        modifier = modifier
            .height(height)
            .shadow(6.dp, shape, clip = false)
            .background(GoldFrameBrush, shape)
            .padding(2.dp)
            .background(Brush.verticalGradient(listOf(Color(0xFF5A321A), GuesserChocolateDark)), shape)
            .padding(horizontal = height * 0.45f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        GoldDot(height * 0.14f)
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = height * 0.35f),
            color = GuesserGoldCream,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            maxLines = 1,
        )
        GoldDot(height * 0.14f)
    }
}

@Composable
private fun GoldDot(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(Brush.radialGradient(listOf(GuesserOrangeTop, GuesserBronzeDark)), CircleShape),
    )
}

/** Visual state of one digit tile in the guess board. */
sealed interface TileState {
    data object Empty : TileState
    data class Pending(val digit: Char) : TileState
    data class Revealed(val digit: Char, val feedback: DigitFeedback) : TileState
}

internal fun digitTileBrush(
    state: TileState,
    showFront: Boolean,
    colorFeedbackEnabled: Boolean,
): Brush = when {
    !colorFeedbackEnabled -> TileAbsentBrush
    showFront || state !is TileState.Revealed -> TileEmptyBrush
    state.feedback == DigitFeedback.EXACT -> TileExactBrush
    state.feedback == DigitFeedback.CLOSE -> TileCloseBrush
    else -> TileAbsentBrush
}

@Composable
fun DigitTile(
    state: TileState,
    size: Dp,
    isCursor: Boolean,
    animateRevealDelayMs: Int?,
    modifier: Modifier = Modifier,
    colorFeedbackEnabled: Boolean = true,
) {
    val rotation = remember(state, animateRevealDelayMs) {
        Animatable(if (state is TileState.Revealed && animateRevealDelayMs != null) 0f else 180f)
    }
    LaunchedEffect(state, animateRevealDelayMs) {
        if (state is TileState.Revealed && animateRevealDelayMs != null) {
            rotation.animateTo(180f, tween(durationMillis = 420, delayMillis = animateRevealDelayMs, easing = FastOutSlowInEasing))
        }
    }
    val showFront = state !is TileState.Revealed || rotation.value < 90f
    val shape = RoundedCornerShape(size * 0.12f)
    val pressScale by animateFloatAsState(
        targetValue = if (state is TileState.Pending) 1f else 0.98f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 700f),
        label = "tilePop",
    )

    val brush = digitTileBrush(state, showFront, colorFeedbackEnabled)
    val digit = when (state) {
        TileState.Empty -> null
        is TileState.Pending -> state.digit
        is TileState.Revealed -> state.digit
    }
    val borderColor = when {
        isCursor -> GuesserOrangeTop
        else -> Color(0x99331A0A)
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                rotationX = if (state is TileState.Revealed) {
                    if (showFront) rotation.value else rotation.value - 180f
                } else {
                    0f
                }
                cameraDistance = 12f * density
                scaleX = pressScale
                scaleY = pressScale
            }
            .shadow(4.dp, shape, clip = false)
            .background(brush, shape)
            .border(if (isCursor) 2.5.dp else 1.dp, borderColor, shape)
            .drawBehind {
                // Subtle top highlight to give the tile a bevel.
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.18f),
                    topLeft = Offset(this.size.width * 0.08f, this.size.height * 0.06f),
                    size = Size(this.size.width * 0.84f, this.size.height * 0.16f),
                    cornerRadius = CornerRadius(this.size.height * 0.08f),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (digit != null) {
            val light = !colorFeedbackEnabled || !showFront
            Text(
                text = digit.toString(),
                style = TextStyle(
                    color = if (light) LightDigitColor else DarkDigitColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Black,
                    fontSize = with(LocalDensity.current) { (size * 0.56f).toSp() },
                    shadow = if (light) DigitShadow else null,
                ),
            )
        }
    }
}

@Composable
fun RemarkBox(
    remark: String?,
    isWinner: Boolean,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    val corner = height * 0.16f
    val goldDash = GuesserGoldBorder.copy(alpha = 0.55f)
    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(corner))
            .background(RemarkFill)
            .border(1.5.dp, Color(0xFF8A5A2B), RoundedCornerShape(corner))
            .drawBehind {
                val inset = 5.dp.toPx()
                drawRoundRect(
                    color = goldDash,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    cornerRadius = CornerRadius(corner.toPx() * 0.6f),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx())),
                    ),
                )
            }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (remark != null) {
            val fontSize = with(LocalDensity.current) { (minOf(height * 0.4f, maxWidth / 5.8f)).toSp() }
            Text(
                text = if (isWinner) "$remark!" else remark,
                color = if (isWinner) WinnerGreen else GuesserGoldCream,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun RowNumber(
    number: Int,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.height(height), contentAlignment = Alignment.Center) {
        Text(
            text = number.toString(),
            color = RowNumberColor,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = with(LocalDensity.current) { (height * 0.36f).toSp() },
            maxLines = 1,
            softWrap = false,
            style = TextStyle(shadow = DigitShadow),
        )
    }
}

enum class KeyStyle {
    Digit,
    Enter,
    Delete,
    Secondary,
}

@Composable
fun KeypadKey(
    label: String,
    contentDescription: String,
    style: KeyStyle,
    enabled: Boolean,
    height: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 700f),
        label = "keyScale",
    )
    val shape = RoundedCornerShape(height * 0.3f)
    val brush = when (style) {
        KeyStyle.Delete -> KeyDeleteBrush
        KeyStyle.Secondary -> KeySecondaryBrush
        else -> TileCloseBrush
    }
    val labelSize: TextUnit = with(LocalDensity.current) {
        (if (style == KeyStyle.Digit) height * 0.52f else height * 0.3f).toSp()
    }

    Box(
        modifier = modifier
            .height(height.coerceAtLeast(48.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .alpha(if (enabled) 1f else 0.45f)
                .shadow(if (isPressed) 2.dp else 7.dp, shape, clip = false)
                .background(Brush.verticalGradient(listOf(GuesserBronzeDark, GuesserGoldBorder)), shape)
                .padding(2.dp)
                .background(brush, shape)
                .border(1.dp, Color.White.copy(alpha = 0.35f), shape)
                .drawBehind {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.22f),
                        topLeft = Offset(size.width * 0.06f, size.height * 0.08f),
                        size = Size(size.width * 0.88f, size.height * 0.3f),
                        cornerRadius = CornerRadius(size.height * 0.15f),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    color = LightDigitColor,
                    fontFamily = FontFamily.Serif,
                    fontWeight = if (style == KeyStyle.Digit) FontWeight.Black else FontWeight.ExtraBold,
                    fontSize = labelSize,
                    letterSpacing = if (style == KeyStyle.Digit) 0.sp else 0.6.sp,
                    maxLines = 1,
                    softWrap = false,
                    style = TextStyle(shadow = DigitShadow),
                )
                if (style == KeyStyle.Delete) {
                    Spacer(modifier = Modifier.width(height * 0.1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Backspace,
                        contentDescription = null,
                        tint = LightDigitColor,
                        modifier = Modifier.size(height * 0.36f),
                    )
                }
            }
        }
    }
}

@Composable
fun GuesserDialogFrame(
    textureRes: Int,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    buttons: @Composable () -> Unit,
) {
    WoodPanel(
        textureRes = textureRes,
        overlay = Color(0xB3220F06),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                color = GuesserOrangeTop,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = DigitShadow),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                color = GuesserGoldCream,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(20.dp))
            buttons()
        }
    }
}
