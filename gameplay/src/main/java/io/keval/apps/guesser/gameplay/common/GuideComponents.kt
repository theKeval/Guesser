package io.keval.apps.guesser.gameplay.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.keval.apps.guesser.core.ui.theme.GuesserChocolateDark
import io.keval.apps.guesser.core.ui.theme.GuesserGoldCream
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeTop
import io.keval.apps.guesser.core.ui.theme.GuesserBronzeDark
import io.keval.apps.guesser.core.ui.theme.GuesserGoldBorder
import io.keval.apps.guesser.domain.game.DigitFeedback
import io.keval.apps.guesser.domain.game.GuessEvaluation
import io.keval.apps.guesser.gameplay.R
import io.keval.apps.guesser.gameplay.audio.rememberTapFeedback
import io.keval.apps.guesser.gameplay.game.DigitTile
import io.keval.apps.guesser.gameplay.game.GameRoundIconButton
import io.keval.apps.guesser.gameplay.game.GoldFrameBrush
import io.keval.apps.guesser.gameplay.game.RemarkBox
import io.keval.apps.guesser.gameplay.game.TileState
import io.keval.apps.guesser.gameplay.game.WoodPanel

@Composable
internal fun GuideScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    fixedContent: Boolean = false,
    trailingAction: @Composable RowScope.() -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
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
                trailingAction()
            }
            if (fixedContent) {
                Column(
                    Modifier.weight(1f).widthIn(max = 520.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content,
                )
            } else {
                Column(
                    Modifier.weight(1f).widthIn(max = 520.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content,
                )
            }
            footer()
        }
    }
}

@Composable
internal fun GuideSkipAction(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val feedback = rememberTapFeedback()
    TextButton(
        onClick = {
            feedback()
            onClick()
        },
        enabled = enabled,
        modifier = Modifier.heightIn(min = 48.dp),
    ) {
        Text(text, color = GuesserGoldCream, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun GuidePageDots(
    count: Int,
    selectedIndex: Int,
    pageDescription: @Composable (Int) -> String,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val feedback = rememberTapFeedback()
    Row(
        modifier = modifier.semantics { selectableGroup() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { index ->
            val selected = index == selectedIndex
            val description = pageDescription(index)
            androidx.compose.foundation.layout.Box(
                Modifier
                    .size(48.dp)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = {
                            if (!selected) feedback()
                            onSelect(index)
                        },
                    )
                    .semantics {
                        contentDescription = description
                        this.selected = selected
                    },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(if (selected) 14.dp else 12.dp).clip(CircleShape)) {
                    if (selected) {
                        drawCircle(color = GuesserOrangeTop)
                        drawCircle(
                            color = GuesserBronzeDark,
                            radius = size.minDimension * 0.5f,
                            center = Offset(size.width / 2, size.height / 2),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                        )
                    } else {
                        drawCircle(
                            color = GuesserGoldCream.copy(alpha = 0.5f),
                            radius = size.minDimension * 0.5f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx()),
                        )
                    }
                }
            }
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

private val ClueRightColor = Color(0xFF8EE06A)
private val ClueWrongColor = Color(0xFFFF7A66)
private val ClueSmyleColor = Color(0xFF4FD1C5)
private val ClueSmyleMeaningColor = Color(0xFFFFD43B)
private val RightClueWords = Regex("\\b(RIGHT|1R|2R)\\b")
private val WrongClueWords = Regex("\\b(WRONG|1W|2W|3W)\\b")
private val SmyleWord = Regex("\\bSMYLE\\b")
private val SmyleMeaning = Regex("\\bSimply Make Your Life Easy\\b")

/**
 * Makes the clue words stand out in bold: RIGHT, 1R and 2R in green; WRONG, 1W, 2W and 3W in
 * red; SMYLE in teal and "Simply Make Your Life Easy" in yellow. Only styling is added; the
 * text is unchanged.
 */
internal fun highlightClueWords(text: String): AnnotatedString = buildAnnotatedString {
    append(text)
    listOf(
        RightClueWords to ClueRightColor,
        WrongClueWords to ClueWrongColor,
        SmyleWord to ClueSmyleColor,
        SmyleMeaning to ClueSmyleMeaningColor,
    ).forEach { (words, color) ->
        words.findAll(text).forEach { match ->
            addStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold), match.range.first, match.range.last + 1)
        }
    }
}

@Composable
internal fun GuideBody(text: AnnotatedString, modifier: Modifier = Modifier) {
    Text(text, modifier, color = GuesserGoldCream, fontSize = 16.sp, lineHeight = 24.sp)
}

@Composable
internal fun GuideBody(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, color = GuesserGoldCream, fontSize = 16.sp, lineHeight = 24.sp)
}

/**
 * One rule in a numbered list: a gold-framed number badge beside the rule text, on a softly
 * inset card. The pair is read as a single item by accessibility services.
 */
@Composable
internal fun GuideNumberedRule(number: Int, text: AnnotatedString, modifier: Modifier = Modifier) {
    val cardShape = RoundedCornerShape(14.dp)
    Row(
        modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .background(GuesserChocolateDark.copy(alpha = 0.55f), cardShape)
            .border(1.dp, GuesserGoldBorder.copy(alpha = 0.35f), cardShape)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .squareAtLeast(34.dp)
                .shadow(4.dp, CircleShape, clip = false)
                .background(GoldFrameBrush, CircleShape)
                .padding(2.dp)
                .background(Brush.verticalGradient(listOf(GuesserOrangeTop, GuesserBronzeDark)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                number.toString(),
                color = GuesserChocolateDark,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
        GuideBody(text, Modifier.weight(1f))
    }
}

/** Sizes the element as a square that fits its content, so a circle stays round at any font scale. */
private fun Modifier.squareAtLeast(minSide: Dp): Modifier = layout { measurable, constraints ->
    val width = measurable.maxIntrinsicWidth(constraints.maxHeight)
    val height = measurable.maxIntrinsicHeight(width)
    val side = maxOf(width, height, minSide.roundToPx())
    val placeable = measurable.measure(Constraints.fixed(side, side))
    layout(side, side) { placeable.place(0, 0) }
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
    val feedback = rememberTapFeedback()
    Button(
        onClick = {
            feedback()
            onClick()
        },
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = GuesserOrangeTop, contentColor = GuesserChocolateDark,
            disabledContainerColor = GuesserGoldCream.copy(alpha = 0.2f),
            disabledContentColor = GuesserGoldCream.copy(alpha = 0.6f),
        ),
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

/**
 * A scored guess row. With [colorFeedbackEnabled] false it matches real rounds: every tile is
 * gray and only the remark is announced, so no per-digit hint is given.
 */
@Composable
internal fun GuideGuess(
    evaluation: GuessEvaluation,
    announce: Boolean = false,
    colorFeedbackEnabled: Boolean = true,
) {
    val description = if (colorFeedbackEnabled) {
        stringResource(
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
    } else {
        stringResource(R.string.guide_guess_remark_description, evaluation.guess, evaluation.remark)
    }
    // With colors on, a spoken clue for each digit conveys the same information as the tile colors.
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
                DigitTile(
                    TileState.Revealed(digit, evaluation.digits[index]),
                    56.dp,
                    false,
                    null,
                    colorFeedbackEnabled = colorFeedbackEnabled,
                )
            }
        }
        RemarkBox(evaluation.remark, evaluation.isWin, 52.dp, Modifier.fillMaxWidth())
    }
}
