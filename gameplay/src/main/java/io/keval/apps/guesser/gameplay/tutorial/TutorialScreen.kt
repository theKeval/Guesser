package io.keval.apps.guesser.gameplay.tutorial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.keval.apps.guesser.core.ui.theme.GuesserGoldCream
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeTop
import io.keval.apps.guesser.core.ui.theme.GuesserTheme
import io.keval.apps.guesser.domain.game.GuessRules
import io.keval.apps.guesser.gameplay.R
import io.keval.apps.guesser.gameplay.common.GuideAction
import io.keval.apps.guesser.gameplay.common.GuideBody
import io.keval.apps.guesser.gameplay.common.GuideGuess
import io.keval.apps.guesser.gameplay.common.GuidePanel
import io.keval.apps.guesser.gameplay.common.GuideScaffold
import io.keval.apps.guesser.gameplay.game.DigitTile
import io.keval.apps.guesser.gameplay.game.KeyStyle
import io.keval.apps.guesser.gameplay.game.KeypadKey
import io.keval.apps.guesser.gameplay.game.TileState
import io.keval.apps.guesser.gameplay.info.LearningExamples

@Composable
fun TutorialScreen(
    onBack: () -> Unit,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    var input by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf("") }
    val evaluation = remember(submitted) {
        submitted.takeIf(GuessRules::isValidCode)?.let { GuessRules.evaluate(LearningExamples.SECRET, it) }
    }
    val back: () -> Unit = { if (page > 0) page-- else onBack() }
    BackHandler(onBack = back)
    key(page) {
        GuideScaffold(stringResource(R.string.label_tutorial), back, modifier) {
            GuideBody(stringResource(R.string.tutorial_progress, page + 1, 4))
            LinearProgressIndicator(
                progress = { (page + 1) / 4f },
                modifier = Modifier.fillMaxWidth(),
                color = GuesserOrangeTop,
                trackColor = GuesserGoldCream.copy(alpha = 0.2f),
            )
            when (page) {
                0 -> GuidePanel(stringResource(R.string.tutorial_goal_title)) {
                    GuideBody(stringResource(R.string.tutorial_goal_body))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    ) {
                        LearningExamples.SECRET.forEach {
                            DigitTile(TileState.Pending(it), 64.dp, false, null)
                        }
                    }
                    GuideBody(stringResource(R.string.rules_code_body))
                }
                1 -> GuidePanel(stringResource(R.string.tutorial_clues_title)) {
                    GuideBody(stringResource(R.string.rules_clues_body))
                    GuideGuess(LearningExamples.evaluations.first())
                    GuideBody(stringResource(R.string.example_mixed))
                    GuideBody(stringResource(R.string.tutorial_deduction))
                }
                2 -> GuidePanel(stringResource(R.string.tutorial_practice_title)) {
                    GuideBody(stringResource(R.string.tutorial_practice_body, LearningExamples.SECRET))
                    val inputDescription = stringResource(R.string.tutorial_input_description, input, input.length)
                    Row(
                        Modifier.fillMaxWidth().clearAndSetSemantics { contentDescription = inputDescription },
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    ) {
                        repeat(3) { index ->
                            DigitTile(
                                input.getOrNull(index)?.let { TileState.Pending(it) } ?: TileState.Empty,
                                56.dp, index == input.length, null,
                            )
                        }
                    }
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val rows = if (maxWidth < 272.dp) listOf("123", "456", "789", " 0 ") else listOf("12345", "67890")
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rows.forEach { digits ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    digits.forEach { digit ->
                                        if (digit == ' ') {
                                            Spacer(Modifier.weight(1f))
                                        } else {
                                            KeypadKey(
                                                digit.toString(), stringResource(R.string.cd_key_digit, digit.toString()),
                                                KeyStyle.Digit, input.length < 3 && digit !in input, 48.dp,
                                                Modifier.weight(1f),
                                            ) { input += digit }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GuideAction(stringResource(R.string.key_delete), {
                            input = input.dropLast(1)
                        }, Modifier.weight(1f), input.isNotEmpty())
                        GuideAction(stringResource(R.string.key_enter), {
                            submitted = input
                            input = ""
                        }, Modifier.weight(1f), GuessRules.isValidCode(input))
                    }
                    if (evaluation != null) {
                        GuideGuess(evaluation, announce = true)
                        GuideBody(
                            stringResource(if (evaluation.isWin) R.string.tutorial_practice_win else R.string.tutorial_practice_retry),
                            Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        )
                    }
                }
                3 -> GuidePanel(stringResource(R.string.tutorial_ready_title)) {
                    GuideBody(stringResource(R.string.tutorial_ready_body))
                    GuideBody(stringResource(R.string.rules_single_body))
                    GuideBody(stringResource(R.string.rules_double_body))
                    GuideAction(stringResource(R.string.tutorial_play), onPlay, Modifier.fillMaxWidth())
                }
            }
            if (page < 3) {
                GuideAction(
                    stringResource(if (page == 2) R.string.tutorial_finish else R.string.tutorial_next),
                    { page++ }, Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Compact phone", widthDp = 320, heightDp = 568)
@Preview(name = "Large text", widthDp = 360, heightDp = 800, fontScale = 2f)
@Preview(name = "Tablet", widthDp = 800, heightDp = 1280)
@Composable
private fun TutorialPreview() {
    GuesserTheme { TutorialScreen(onBack = {}, onPlay = {}) }
}
