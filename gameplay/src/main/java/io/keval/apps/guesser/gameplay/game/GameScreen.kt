package io.keval.apps.guesser.gameplay.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.keval.apps.guesser.core.ui.theme.GuesserTheme
import io.keval.apps.guesser.domain.game.CODE_LENGTH
import io.keval.apps.guesser.domain.game.GuessEvaluation
import io.keval.apps.guesser.domain.game.GuessRules
import io.keval.apps.guesser.gameplay.R
import io.keval.apps.guesser.gameplay.home.PlayerMode
import kotlinx.coroutines.delay
import kotlin.math.floor

private const val ROW_NUMBER_UNITS = 0.55f
private const val REMARK_UNITS = 2.15f
private const val INFO_UNITS = 0.5f
private const val ROW_UNITS = CODE_LENGTH + ROW_NUMBER_UNITS + REMARK_UNITS + INFO_UNITS

// Gaps between row number, digit tiles, remark box and info button.
private const val ROW_GAP_COUNT = CODE_LENGTH + 2
private const val MIN_VISIBLE_ROWS = 5
private const val TILE_REVEAL_STAGGER_MS = 180
private const val WIN_DIALOG_DELAY_MS = 1_100L
private val MaxContentWidth = 520.dp
private val DialogButtonHeight = 48.dp
private val BoardOverlay = Color(0xA81A0B04)
private val KeypadOverlay = Color(0x66140802)

@Composable
fun GameRoute(
    friendSecretNumber: String?,
    onExitToHome: () -> Unit,
    onHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GameViewModel = viewModel(factory = GameViewModel.factory(friendSecretNumber))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val requestExit = {
        if (viewModel.onBackRequested()) onExitToHome()
    }
    BackHandler(onBack = requestExit)

    GameScreen(
        uiState = uiState,
        onBack = requestExit,
        onHelp = onHelp,
        onDigit = viewModel::onDigit,
        onDelete = viewModel::onDelete,
        onEnter = viewModel::onEnter,
        onConfirmExit = {
            viewModel.onDismissExitConfirmation()
            onExitToHome()
        },
        onDismissExit = viewModel::onDismissExitConfirmation,
        onPlayAgain = viewModel::onPlayAgain,
        onWinHome = onExitToHome,
        onDismissWinDialog = viewModel::onDismissWinDialog,
        modifier = modifier,
    )
}

@Composable
fun GameScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismissExit: () -> Unit,
    onPlayAgain: () -> Unit,
    onWinHome: () -> Unit,
    onDismissWinDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var remarkInfo by remember { mutableStateOf<GuessEvaluation?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.home_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val contentWidth = minOf(maxWidth, MaxContentWidth)
            val metrics = remember(contentWidth, maxHeight) { GameMetrics.from(contentWidth, maxHeight) }

            Column(
                modifier = Modifier
                    .widthIn(max = MaxContentWidth)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GameHeader(
                    metrics = metrics,
                    onBack = onBack,
                    onHelp = onHelp,
                )
                Spacer(modifier = Modifier.height(metrics.sectionGap * 0.6f))
                GameModeBadge(
                    text = stringResource(
                        if (uiState.playerMode == PlayerMode.DOUBLE) R.string.game_mode_double else R.string.game_mode_single,
                    ),
                    height = metrics.badgeHeight,
                )
                Spacer(modifier = Modifier.height(metrics.sectionGap))
                GuessBoard(
                    uiState = uiState,
                    onRemarkInfo = { remarkInfo = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
                Spacer(modifier = Modifier.height(metrics.sectionGap))
                Keypad(
                    uiState = uiState,
                    keyHeight = metrics.keyHeight,
                    onDigit = onDigit,
                    onDelete = onDelete,
                    onEnter = onEnter,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        RemarkInfoPopup(
            evaluation = remarkInfo,
            onDismiss = { remarkInfo = null },
        )

        if (uiState.showExitConfirmation) {
            ExitConfirmationDialog(
                playerMode = uiState.playerMode,
                onConfirm = onConfirmExit,
                onDismiss = onDismissExit,
            )
        }

        var winDialogVisible by remember { mutableStateOf(false) }
        LaunchedEffect(uiState.showWinDialog) {
            if (uiState.showWinDialog) {
                if (uiState.lastRevealedIndex != null) delay(WIN_DIALOG_DELAY_MS)
                winDialogVisible = true
            } else {
                winDialogVisible = false
            }
        }
        if (winDialogVisible && uiState.showWinDialog) {
            WinDialog(
                uiState = uiState,
                onPlayAgain = onPlayAgain,
                onHome = onWinHome,
                onDismiss = onDismissWinDialog,
            )
        }
    }
}

private data class GameMetrics(
    val iconSize: Dp,
    val titleWidth: Dp,
    val titleHeight: Dp,
    val badgeHeight: Dp,
    val keyHeight: Dp,
    val sectionGap: Dp,
) {
    companion object {
        private const val TITLE_ASPECT = 450f / 107f

        // Keypad height = 4 key rows + 3 row gaps (0.22 key) + vertical padding (2 x 0.28 key).
        private const val KEYPAD_HEIGHT_IN_KEYS = 4f + 3 * 0.22f + 2 * 0.28f

        fun from(width: Dp, height: Dp): GameMetrics {
            val iconSize = (width * 0.13f).coerceIn(44.dp, 60.dp)
            val titleHeight = minOf((width * 0.6f) / TITLE_ASPECT, height * 0.085f)
            val badgeHeight = minOf((width * 0.085f).coerceIn(28.dp, 40.dp), height * 0.05f).coerceAtLeast(24.dp)
            val keyHeight = minOf(
                (width * 0.125f).coerceIn(40.dp, 64.dp),
                height * 0.34f / KEYPAD_HEIGHT_IN_KEYS,
            ).coerceAtLeast(36.dp)
            return GameMetrics(
                iconSize = iconSize,
                titleWidth = titleHeight * TITLE_ASPECT,
                titleHeight = titleHeight,
                badgeHeight = badgeHeight,
                keyHeight = keyHeight,
                sectionGap = (height * 0.014f).coerceIn(6.dp, 16.dp),
            )
        }
    }
}

@Composable
private fun GameHeader(
    metrics: GameMetrics,
    onBack: () -> Unit,
    onHelp: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GameRoundIconButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.cd_back_home),
            size = metrics.iconSize,
            onClick = onBack,
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.home_title),
                contentDescription = stringResource(R.string.cd_game_title),
                modifier = Modifier
                    .width(metrics.titleWidth)
                    .height(metrics.titleHeight),
                contentScale = ContentScale.Fit,
            )
        }
        GameRoundIconButton(
            icon = Icons.Filled.QuestionMark,
            contentDescription = stringResource(R.string.cd_help),
            size = metrics.iconSize,
            onClick = onHelp,
        )
    }
}

@Composable
private fun GuessBoard(
    uiState: GameUiState,
    onRemarkInfo: (GuessEvaluation) -> Unit,
    modifier: Modifier = Modifier,
) {
    WoodPanel(
        textureRes = R.drawable.game_board_wood,
        overlay = BoardOverlay,
        modifier = modifier,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val baseGap = (maxWidth * 0.022f).coerceIn(4.dp, 10.dp)
            val widthTile = (maxWidth - baseGap * ROW_GAP_COUNT) / ROW_UNITS
            val heightTile = (maxHeight - baseGap * (MIN_VISIBLE_ROWS - 1)) / MIN_VISIBLE_ROWS
            val tile = minOf(widthTile, heightTile).coerceAtLeast(24.dp)
            val rowWidth = tile * ROW_UNITS + baseGap * ROW_GAP_COUNT
            val visibleRows = floor(((maxHeight + baseGap) / (tile + baseGap)).toDouble()).toInt().coerceAtLeast(1)
            val rowGap = if (visibleRows > 1) {
                ((maxHeight - tile * visibleRows) / (visibleRows - 1)).coerceIn(baseGap, baseGap * 2.5f)
            } else {
                baseGap
            }

            val activeRow = if (uiState.isWon) null else uiState.guesses.size
            val usedRows = uiState.guesses.size + if (activeRow != null) 1 else 0
            val totalRows = maxOf(visibleRows, usedRows)
            val listState = rememberLazyListState()

            LaunchedEffect(usedRows, visibleRows) {
                val target = (usedRows - visibleRows).coerceAtLeast(0)
                if (target > 0 || listState.firstVisibleItemIndex > 0) {
                    listState.animateScrollToItem(target)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .width(rowWidth)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(rowGap),
            ) {
                items(count = totalRows) { index ->
                    GuessRow(
                        index = index,
                        uiState = uiState,
                        isActive = index == activeRow,
                        tile = tile,
                        gap = baseGap,
                        onRemarkInfo = onRemarkInfo,
                    )
                }
            }
        }
    }
}

@Composable
private fun GuessRow(
    index: Int,
    uiState: GameUiState,
    isActive: Boolean,
    tile: Dp,
    gap: Dp,
    onRemarkInfo: (GuessEvaluation) -> Unit,
) {
    val evaluation = uiState.guesses.getOrNull(index)
    val animateReveal = evaluation != null && uiState.lastRevealedIndex == index
    val rowDescription = when {
        evaluation != null -> stringResource(
            R.string.cd_guess_row,
            index + 1,
            evaluation.guess.toList().joinToString(" "),
            evaluation.remark,
        )
        isActive -> stringResource(R.string.cd_active_row, index + 1, uiState.currentInput.toList().joinToString(" "))
        else -> stringResource(R.string.cd_empty_row, index + 1)
    }

    Row(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = rowDescription },
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowNumber(number = index + 1, height = tile, modifier = Modifier.width(tile * ROW_NUMBER_UNITS))
        repeat(CODE_LENGTH) { column ->
            val state = when {
                evaluation != null -> TileState.Revealed(evaluation.guess[column], evaluation.digits[column])
                isActive && column < uiState.currentInput.length -> TileState.Pending(uiState.currentInput[column])
                else -> TileState.Empty
            }
            DigitTile(
                state = state,
                size = tile,
                isCursor = isActive && column == uiState.currentInput.length,
                animateRevealDelayMs = if (animateReveal) column * TILE_REVEAL_STAGGER_MS else null,
                colorFeedbackEnabled = false,
            )
        }
        RemarkBox(
            remark = evaluation?.remark,
            isWinner = evaluation?.isWin == true,
            height = tile * 0.74f,
            modifier = Modifier.width(tile * REMARK_UNITS),
            animateHeartbeat = animateReveal,
        )
        Box(
            modifier = Modifier
                .width(tile * INFO_UNITS)
                .height(tile),
            contentAlignment = Alignment.Center,
        ) {
            if (evaluation != null) {
                RemarkInfoButton(
                    contentDescription = stringResource(R.string.cd_remark_info, evaluation.remark),
                    size = tile * 0.42f,
                    modifier = Modifier.fillMaxSize(),
                    onClick = { onRemarkInfo(evaluation) },
                )
            }
        }
    }
}

@Composable
private fun Keypad(
    uiState: GameUiState,
    keyHeight: Dp,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(listOf('1', '2', '3'), listOf('4', '5', '6'), listOf('7', '8', '9'))
    WoodPanel(
        textureRes = R.drawable.game_keypad_wood,
        overlay = KeypadOverlay,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = keyHeight * 0.4f, vertical = keyHeight * 0.28f),
            verticalArrangement = Arrangement.spacedBy(keyHeight * 0.22f),
        ) {
            val horizontalGap = keyHeight * 0.45f
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(horizontalGap)) {
                    row.forEach { digit -> DigitKey(digit, uiState, keyHeight, onDigit, Modifier.weight(1f)) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(horizontalGap)) {
                KeypadKey(
                    label = stringResource(R.string.key_delete),
                    contentDescription = stringResource(R.string.cd_key_delete),
                    style = KeyStyle.Delete,
                    enabled = uiState.canDelete,
                    height = keyHeight,
                    modifier = Modifier.weight(1f),
                    onClick = onDelete,
                )
                DigitKey('0', uiState, keyHeight, onDigit, Modifier.weight(1f))
                KeypadKey(
                    label = stringResource(R.string.key_enter),
                    contentDescription = stringResource(R.string.cd_key_enter),
                    style = KeyStyle.Enter,
                    enabled = uiState.canEnter,
                    height = keyHeight,
                    modifier = Modifier.weight(1f),
                    onClick = onEnter,
                )
            }
        }
    }
}

@Composable
private fun DigitKey(
    digit: Char,
    uiState: GameUiState,
    keyHeight: Dp,
    onDigit: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeypadKey(
        label = digit.toString(),
        contentDescription = stringResource(R.string.cd_key_digit, digit.toString()),
        style = KeyStyle.Digit,
        enabled = uiState.isDigitEnabled(digit),
        height = keyHeight,
        modifier = modifier,
        onClick = { onDigit(digit) },
    )
}

@Composable
private fun ExitConfirmationDialog(
    playerMode: PlayerMode,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val modeName = stringResource(
        if (playerMode == PlayerMode.DOUBLE) R.string.game_mode_double else R.string.game_mode_single,
    )
    Dialog(onDismissRequest = onDismiss) {
        GuesserDialogFrame(
            textureRes = R.drawable.game_keypad_wood,
            title = stringResource(R.string.exit_title),
            message = stringResource(R.string.exit_message, modeName),
        ) {
            DialogButtons(
                secondaryText = stringResource(R.string.exit_cancel),
                onSecondary = onDismiss,
                primaryText = stringResource(R.string.exit_confirm),
                onPrimary = onConfirm,
            )
        }
    }
}

@Composable
private fun WinDialog(
    uiState: GameUiState,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
    onDismiss: () -> Unit,
) {
    val guessCount = uiState.guesses.size
    Dialog(onDismissRequest = onDismiss) {
        GuesserDialogFrame(
            textureRes = R.drawable.game_keypad_wood,
            title = stringResource(R.string.win_title),
            message = pluralStringResource(
                R.plurals.win_message,
                guessCount,
                uiState.revealedSecret.orEmpty(),
                guessCount,
            ),
        ) {
            if (uiState.playerMode == PlayerMode.SINGLE) {
                DialogButtons(
                    secondaryText = stringResource(R.string.win_home),
                    onSecondary = onHome,
                    primaryText = stringResource(R.string.win_play_again),
                    onPrimary = onPlayAgain,
                )
            } else {
                KeypadKey(
                    label = stringResource(R.string.win_home),
                    contentDescription = stringResource(R.string.win_home),
                    style = KeyStyle.Enter,
                    enabled = true,
                    height = DialogButtonHeight,
                    modifier = Modifier.fillMaxWidth(0.6f),
                    onClick = onHome,
                )
            }
        }
    }
}

@Composable
private fun DialogButtons(
    secondaryText: String,
    onSecondary: () -> Unit,
    primaryText: String,
    onPrimary: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KeypadKey(
            label = secondaryText,
            contentDescription = secondaryText,
            style = KeyStyle.Secondary,
            enabled = true,
            height = DialogButtonHeight,
            modifier = Modifier.weight(1f),
            onClick = onSecondary,
        )
        KeypadKey(
            label = primaryText,
            contentDescription = primaryText,
            style = KeyStyle.Enter,
            enabled = true,
            height = DialogButtonHeight,
            modifier = Modifier.weight(1f),
            onClick = onPrimary,
        )
    }
}

private fun previewState(): GameUiState {
    val secret = "573"
    return GameUiState(
        guesses = listOf("481", "219", "573").map { GuessRules.evaluate(secret, it) },
        isWon = true,
        revealedSecret = secret,
    )
}

@Composable
private fun GameScreenPreviewContent(uiState: GameUiState) {
    GuesserTheme {
        GameScreen(
            uiState = uiState,
            onBack = {},
            onHelp = {},
            onDigit = {},
            onDelete = {},
            onEnter = {},
            onConfirmExit = {},
            onDismissExit = {},
            onPlayAgain = {},
            onWinHome = {},
            onDismissWinDialog = {},
        )
    }
}

@Preview(showBackground = true, name = "In progress", widthDp = 411, heightDp = 891)
@Composable
private fun GameScreenInProgressPreview() {
    val secret = "573"
    GameScreenPreviewContent(
        GameUiState(
            guesses = listOf("481", "219").map { GuessRules.evaluate(secret, it) },
            currentInput = "57",
        ),
    )
}

@Preview(showBackground = true, name = "Won", widthDp = 411, heightDp = 891)
@Composable
private fun GameScreenWonPreview() {
    GameScreenPreviewContent(previewState())
}

@Preview(showBackground = true, name = "Small phone", widthDp = 320, heightDp = 568)
@Composable
private fun GameScreenSmallPreview() {
    GameScreenPreviewContent(GameUiState(playerMode = PlayerMode.DOUBLE, currentInput = "1"))
}

@Preview(showBackground = true, name = "Tablet", widthDp = 800, heightDp = 1280)
@Composable
private fun GameScreenTabletPreview() {
    GameScreenPreviewContent(previewState())
}
