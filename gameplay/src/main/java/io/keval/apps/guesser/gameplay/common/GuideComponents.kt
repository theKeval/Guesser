package io.keval.apps.guesser.gameplay.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.keval.apps.guesser.core.ui.theme.GuesserChocolateDark
import io.keval.apps.guesser.core.ui.theme.GuesserGoldCream
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeTop
import io.keval.apps.guesser.domain.game.DigitFeedback
import io.keval.apps.guesser.domain.game.GuessEvaluation
import io.keval.apps.guesser.gameplay.R
import io.keval.apps.guesser.gameplay.game.DigitTile
import io.keval.apps.guesser.gameplay.game.GameRoundIconButton
import io.keval.apps.guesser.gameplay.game.RemarkBox
import io.keval.apps.guesser.gameplay.game.TileState
import io.keval.apps.guesser.gameplay.game.WoodPanel

@Composable
internal fun GuideScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GuesserScreenScaffold(modifier) {
        Column(
            Modifier.fillMaxSize().background(GuesserChocolateDark.copy(alpha = 0.3f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier.widthIn(max = 520.dp).fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GameRoundIconButton(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    stringResource(R.string.guide_back),
                    48.dp,
                    onClick = onBack,
                )
                GuideHeading(title, Modifier.weight(1f))
            }
            Column(
                Modifier.weight(1f).widthIn(max = 520.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        }
    }
}

@Composable
internal fun GuideHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text, modifier.semantics { heading() }, color = GuesserOrangeTop,
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 32.sp,
    )
}

@Composable
internal fun GuideBody(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, color = GuesserGoldCream, fontSize = 16.sp, lineHeight = 24.sp)
}

@Composable
internal fun GuidePanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    WoodPanel(R.drawable.game_board_wood, Color(0xE62A1A12), Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                GuideHeading(title)
                content()
            },
        )
    }
}

@Composable
internal fun GuideAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick, modifier.heightIn(min = 48.dp), enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = GuesserOrangeTop, contentColor = GuesserChocolateDark,
            disabledContainerColor = GuesserGoldCream.copy(alpha = 0.2f),
            disabledContentColor = GuesserGoldCream.copy(alpha = 0.6f),
        ),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
internal fun GuideGuess(evaluation: GuessEvaluation, announce: Boolean = false) {
    val description = stringResource(
        R.string.guide_guess_description, evaluation.guess, evaluation.remark,
        evaluation.digits.mapIndexed { index, feedback ->
            stringResource(
                R.string.guide_digit_description, evaluation.guess[index].toString(),
                stringResource(
                    when (feedback) {
                        DigitFeedback.EXACT -> R.string.clue_exact
                        DigitFeedback.CLOSE -> R.string.clue_close
                        DigitFeedback.ABSENT -> R.string.clue_absent
                    },
                ),
            )
        }.joinToString("; "),
    )
    // A spoken clue for each digit conveys the same information as the tile colors.
    Column(
        Modifier.fillMaxWidth().clearAndSetSemantics {
            contentDescription = description
            if (announce) liveRegion = LiveRegionMode.Polite
        },
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            evaluation.guess.forEachIndexed { index, digit ->
                DigitTile(TileState.Revealed(digit, evaluation.digits[index]), 56.dp, false, null)
            }
        }
        RemarkBox(evaluation.remark, evaluation.isWin, 52.dp, Modifier.fillMaxWidth())
    }
}
