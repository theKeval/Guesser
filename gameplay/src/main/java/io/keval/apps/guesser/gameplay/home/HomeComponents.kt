package io.keval.apps.guesser.gameplay.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.keval.apps.guesser.core.ui.theme.GuesserBronzeDark
import io.keval.apps.guesser.core.ui.theme.GuesserChocolate
import io.keval.apps.guesser.core.ui.theme.GuesserChocolateDark
import io.keval.apps.guesser.core.ui.theme.GuesserGoldBorder
import io.keval.apps.guesser.core.ui.theme.GuesserGoldCream
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeBottom
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeMid
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeTop
import io.keval.apps.guesser.core.ui.theme.GuesserWoodBottom
import io.keval.apps.guesser.core.ui.theme.GuesserWoodTop
import io.keval.apps.guesser.gameplay.audio.rememberTapFeedback

private val PillShape = RoundedCornerShape(percent = 50)

private enum class HomeButtonStyle {
    Primary,
    Secondary,
}

@Composable
fun HomePrimaryButton(
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    HomePillButton(
        text = text,
        contentDescription = contentDescription,
        style = HomeButtonStyle.Primary,
        visualAspectRatio = 3.3f,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
fun HomeSecondaryButton(
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    HomePillButton(
        text = text,
        contentDescription = contentDescription,
        style = HomeButtonStyle.Secondary,
        visualAspectRatio = 4.8f,
        modifier = modifier,
        onClick = onClick,
    )
}

@Composable
private fun HomePillButton(
    text: String,
    contentDescription: String,
    style: HomeButtonStyle,
    visualAspectRatio: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 600f),
        label = "buttonScale",
    )
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0f,
        label = "overlayAlpha",
    )
    val feedback = rememberTapFeedback()

    val backgroundBrush = when (style) {
        HomeButtonStyle.Primary -> Brush.verticalGradient(
            colors = listOf(GuesserOrangeTop, GuesserOrangeMid, GuesserOrangeBottom),
        )

        HomeButtonStyle.Secondary -> Brush.verticalGradient(
            colors = listOf(GuesserWoodTop, GuesserWoodBottom),
        )
    }

    val labelColor = if (style == HomeButtonStyle.Primary) GuesserChocolateDark else GuesserGoldCream

    BoxWithConstraints(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
            ) {
                feedback()
                onClick()
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        val visualHeight = (maxWidth / visualAspectRatio).coerceAtMost(maxHeight)
        val labelSize = if (style == HomeButtonStyle.Primary) visualHeight * 0.38f else visualHeight * 0.36f
        val letterSpacing = if (style == HomeButtonStyle.Primary) 0.sp else 0.8.sp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(visualHeight)
                .shadow(
                    elevation = 10.dp,
                    shape = PillShape,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.45f),
                )
                .border(width = 2.dp, brush = Brush.verticalGradient(listOf(GuesserBronzeDark, GuesserGoldBorder)), shape = PillShape)
                .padding(2.dp)
                .border(width = 1.2.dp, color = GuesserGoldBorder.copy(alpha = 0.95f), shape = PillShape)
                .background(brush = backgroundBrush, shape = PillShape)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = labelColor,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { labelSize.toSp() },
                maxLines = 1,
                softWrap = false,
                letterSpacing = letterSpacing,
            )
            if (overlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = overlayAlpha), shape = PillShape),
                )
            }
        }
    }
}

@Composable
fun PlayerModeToggle(
    playerMode: PlayerMode,
    singleLabel: String,
    doubleLabel: String,
    selectorDescription: String,
    singleDescription: String,
    doubleDescription: String,
    modifier: Modifier = Modifier,
    onPlayerModeSelected: (PlayerMode) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .heightIn(min = 48.dp)
            .shadow(
                elevation = 8.dp,
                shape = PillShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.35f),
                spotColor = Color.Black.copy(alpha = 0.35f),
            )
            .semantics { contentDescription = selectorDescription },
    ) {
        val selectorPadding = 5.dp
        val trackWidth = maxWidth - (selectorPadding * 2)
        val segmentWidth = trackWidth / 2
        val selectedOffset by animateDpAsState(
            targetValue = if (playerMode == PlayerMode.SINGLE) selectorPadding else selectorPadding + segmentWidth,
            animationSpec = spring(stiffness = 500f, dampingRatio = 0.75f),
            label = "selectedOffset",
        )
        val textSize = with(androidx.compose.ui.platform.LocalDensity.current) { (maxHeight * 0.25f).toSp() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(width = 2.dp, brush = Brush.verticalGradient(listOf(GuesserBronzeDark, GuesserGoldBorder)), shape = PillShape)
                .padding(2.dp)
                .background(
                    brush = Brush.verticalGradient(listOf(GuesserOrangeTop, GuesserOrangeMid, GuesserOrangeBottom)),
                    shape = PillShape,
                ),
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = selectorPadding, vertical = 4.dp)
                    .fillMaxSize()
                    .background(Color.Transparent, shape = PillShape),
            ) {
                Box(
                    modifier = Modifier
                        .width(segmentWidth)
                        .fillMaxSize()
                        .padding(end = 1.dp)
                        .offset(x = selectedOffset - selectorPadding)
                        .background(
                            brush = Brush.verticalGradient(listOf(GuesserChocolate, GuesserChocolateDark)),
                            shape = PillShape,
                        ),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .selectableGroup(),
            ) {
                ModeSegment(
                    label = singleLabel,
                    description = singleDescription,
                    selected = playerMode == PlayerMode.SINGLE,
                    textSize = textSize,
                    modifier = Modifier.weight(1f),
                ) { onPlayerModeSelected(PlayerMode.SINGLE) }
                ModeSegment(
                    label = doubleLabel,
                    description = doubleDescription,
                    selected = playerMode == PlayerMode.DOUBLE,
                    textSize = textSize,
                    modifier = Modifier.weight(1f),
                ) { onPlayerModeSelected(PlayerMode.DOUBLE) }
            }
        }
    }
}

@Composable
private fun ModeSegment(
    label: String,
    description: String,
    selected: Boolean,
    textSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val feedback = rememberTapFeedback()
    Box(
        modifier = modifier
            .fillMaxSize()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
            ) {
                if (!selected) feedback()
                onClick()
            }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = GuesserGoldCream,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = textSize,
            lineHeight = textSize * 0.95f,
            maxLines = 2,
        )
    }
}
