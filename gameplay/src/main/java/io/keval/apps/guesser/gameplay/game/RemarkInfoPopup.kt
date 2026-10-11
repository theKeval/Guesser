package io.keval.apps.guesser.gameplay.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.keval.apps.guesser.domain.game.GuessEvaluation
import io.keval.apps.guesser.gameplay.R

private const val ENTER_MS = 300
private const val EXIT_MS = 200
private val IosEaseOut = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f)
private val ScrimColor = Color(0x99000000)

/**
 * iOS-style alert: the card zooms in from slightly larger while fading in, and fades out with
 * a slight shrink on dismiss. Tap outside, GOT IT, or Back to close.
 */
@Composable
fun RemarkInfoPopup(
    evaluation: GuessEvaluation?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keep the last shown remark so the content stays visible during the exit animation.
    var shown by remember { mutableStateOf(evaluation) }
    if (evaluation != null) shown = evaluation
    val visible = evaluation != null

    BackHandler(enabled = visible, onBack = onDismiss)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(ENTER_MS)),
            exit = fadeOut(tween(EXIT_MS)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ScrimColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(ENTER_MS / 2)) + scaleIn(tween(ENTER_MS, easing = IosEaseOut), initialScale = 1.18f),
            exit = fadeOut(tween(EXIT_MS)) + scaleOut(tween(EXIT_MS), targetScale = 0.92f),
        ) {
            shown?.let { current ->
                GuesserDialogFrame(
                    textureRes = R.drawable.game_keypad_wood,
                    title = current.remark,
                    message = remarkMeaningText(current.remarkMeaning()),
                    modifier = Modifier
                        .semantics { paneTitle = current.remark }
                        .padding(horizontal = 40.dp)
                        .widthIn(max = 320.dp)
                        // Swallow taps so touching the card does not dismiss it via the scrim.
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                ) {
                    KeypadKey(
                        label = stringResource(R.string.remark_info_dismiss),
                        contentDescription = stringResource(R.string.remark_info_dismiss),
                        style = KeyStyle.Enter,
                        enabled = true,
                        height = 44.dp,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun remarkMeaningText(meaning: RemarkMeaning): String = when (meaning) {
    RemarkMeaning.Winner -> stringResource(R.string.remark_info_winner)
    RemarkMeaning.Smyle -> stringResource(R.string.remark_info_smyle)
    is RemarkMeaning.Counts -> {
        val right = pluralStringResource(R.plurals.remark_info_right, meaning.right, meaning.right)
        val wrong = pluralStringResource(R.plurals.remark_info_wrong, meaning.wrong, meaning.wrong)
        when {
            meaning.wrong == 0 -> right
            meaning.right == 0 -> wrong
            else -> stringResource(R.string.remark_info_counts, right, wrong)
        }
    }
}
