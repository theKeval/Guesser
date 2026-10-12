package io.keval.apps.numbcrunch.gameplay.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.keval.apps.numbcrunch.core.ui.theme.GuesserBronzeDark
import io.keval.apps.numbcrunch.core.ui.theme.GuesserGoldBorder
import io.keval.apps.numbcrunch.core.ui.theme.GuesserGoldCream
import io.keval.apps.numbcrunch.core.ui.theme.GuesserInputFill
import io.keval.apps.numbcrunch.core.ui.theme.GuesserInputHint
import io.keval.apps.numbcrunch.core.ui.theme.GuesserOrangeTop
import io.keval.apps.numbcrunch.domain.game.CODE_LENGTH
import io.keval.apps.numbcrunch.gameplay.R
import io.keval.apps.numbcrunch.gameplay.audio.rememberTapFeedback
import io.keval.apps.numbcrunch.gameplay.game.DigitTile
import io.keval.apps.numbcrunch.gameplay.game.GameRoundIconButton
import io.keval.apps.numbcrunch.gameplay.game.KeyStyle
import io.keval.apps.numbcrunch.gameplay.game.KeypadKey
import io.keval.apps.numbcrunch.gameplay.game.TileState
import io.keval.apps.numbcrunch.gameplay.game.WoodPanel

private const val MASK = '●'
private const val SHEET_ENTER_MS = 320
private const val SHEET_EXIT_MS = 220
private val PillShape = RoundedCornerShape(percent = 50)
private val SheetMaxWidth = 520.dp
private val ScrimColor = Color(0x99000000)

private fun tileState(secret: String, index: Int, revealed: Boolean): TileState =
    secret.getOrNull(index)?.let { TileState.Pending(if (revealed) it else MASK) } ?: TileState.Empty

/**
 * Home's friend-secret field: three small game tiles in the input pill, masked once filled.
 * Tapping it opens [FriendSecretSheet].
 */
@Composable
fun FriendSecretSlot(
    secret: String,
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val feedback = rememberTapFeedback()
    val description = stringResource(R.string.cd_secret_slot, secret.length)
    BoxWithConstraints(
        modifier = modifier
            .heightIn(min = 48.dp)
            .shadow(8.dp, PillShape, clip = false, ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.3f))
            .border(2.dp, Brush.verticalGradient(listOf(GuesserBronzeDark, GuesserGoldBorder)), PillShape)
            .padding(2.dp)
            .background(GuesserInputFill, PillShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
            ) {
                feedback()
                onClick()
            }
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        if (secret.isEmpty()) {
            val hintSize = with(androidx.compose.ui.platform.LocalDensity.current) { (maxHeight * 0.28f).toSp() }
            Text(
                text = placeholder,
                color = GuesserInputHint,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = hintSize,
                maxLines = 1,
                softWrap = false,
            )
        } else {
            val tile = maxHeight * 0.68f
            Row(horizontalArrangement = Arrangement.spacedBy(tile * 0.22f)) {
                repeat(CODE_LENGTH) { index ->
                    DigitTile(
                        state = tileState(secret, index, revealed = false),
                        size = tile,
                        isCursor = false,
                        animateRevealDelayMs = null,
                    )
                }
            }
        }
    }
}

/**
 * Bottom sheet for the friend who hides the secret: the game's digit tiles and keypad on a
 * wood panel. Digits already used are disabled, so the code is always unique; DONE closes
 * the sheet once three digits are in. Every key plays the same note so the guesser cannot
 * hear which digits were chosen.
 */
@Composable
fun FriendSecretSheet(
    visible: Boolean,
    secret: String,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Not saveable: after any recreation the secret starts hidden again.
    var revealed by remember { mutableStateOf(false) }
    // Always start hidden so the secret is never left on show.
    if (!visible && revealed) revealed = false
    BackHandler(enabled = visible, onBack = onDismiss)

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(visible = visible, enter = fadeIn(tween(SHEET_ENTER_MS)), exit = fadeOut(tween(SHEET_EXIT_MS))) {
            Box(
                Modifier
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
            enter = slideInVertically(tween(SHEET_ENTER_MS)) { it } + fadeIn(tween(SHEET_ENTER_MS / 2)),
            exit = slideOutVertically(tween(SHEET_EXIT_MS)) { it } + fadeOut(tween(SHEET_EXIT_MS)),
        ) {
            val title = stringResource(R.string.secret_sheet_title)
            WoodPanel(
                textureRes = R.drawable.game_keypad_wood,
                overlay = Color(0xB3220F06),
                cornerRadius = 26.dp,
                modifier = Modifier
                    .widthIn(max = SheetMaxWidth)
                    .fillMaxWidth()
                    .semantics { paneTitle = title }
                    // Swallow taps so touching the sheet does not dismiss it via the scrim.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                SheetContent(
                    title = title,
                    secret = secret,
                    revealed = revealed,
                    onToggleReveal = { revealed = !revealed },
                    onDigit = onDigit,
                    onDelete = onDelete,
                    onDone = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun SheetContent(
    title: String,
    secret: String,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val keyHeight = (maxWidth * 0.125f).coerceIn(44.dp, 58.dp)
        val tileSize = (maxWidth * 0.16f).coerceIn(52.dp, 72.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = keyHeight * 0.4f, end = keyHeight * 0.4f, top = 10.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Grab handle, as on a native bottom sheet.
            Box(
                Modifier
                    .size(width = 44.dp, height = 5.dp)
                    .background(GuesserGoldBorder.copy(alpha = 0.55f), PillShape),
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = title,
                color = GuesserOrangeTop,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Black,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = stringResource(R.string.secret_sheet_body),
                color = GuesserGoldCream,
                fontFamily = FontFamily.Serif,
                style = TextStyle(fontSize = 15.sp, lineHeight = 20.sp),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.size(16.dp))
            SecretTiles(secret, revealed, tileSize, onToggleReveal)
            Spacer(Modifier.size(18.dp))
            SecretKeypad(secret, keyHeight, onDigit, onDelete, onDone)
        }
    }
}

@Composable
private fun SecretTiles(
    secret: String,
    revealed: Boolean,
    tileSize: Dp,
    onToggleReveal: () -> Unit,
) {
    val tilesDescription = if (revealed) {
        stringResource(R.string.cd_secret_tiles_revealed, secret.toList().joinToString(" "), secret.length)
    } else {
        stringResource(R.string.cd_secret_tiles, secret.length)
    }
    val eyeSize = 44.dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Balances the eye button so the tiles stay centered.
        Spacer(Modifier.width(eyeSize.coerceAtLeast(48.dp)))
        Row(
            modifier = Modifier
                .weight(1f)
                .clearAndSetSemantics { contentDescription = tilesDescription },
            horizontalArrangement = Arrangement.spacedBy(tileSize * 0.18f, Alignment.CenterHorizontally),
        ) {
            repeat(CODE_LENGTH) { index ->
                DigitTile(
                    state = tileState(secret, index, revealed),
                    size = tileSize,
                    isCursor = index == secret.length,
                    animateRevealDelayMs = null,
                )
            }
        }
        GameRoundIconButton(
            icon = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = stringResource(if (revealed) R.string.cd_hide_secret else R.string.cd_show_secret),
            size = eyeSize,
            onClick = onToggleReveal,
        )
    }
}

@Composable
private fun SecretKeypad(
    secret: String,
    keyHeight: Dp,
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
) {
    val rows = listOf("123", "456", "789")
    val gap = keyHeight * 0.4f
    Column(verticalArrangement = Arrangement.spacedBy(keyHeight * 0.22f)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { digit -> SecretDigitKey(digit, secret, keyHeight, onDigit, Modifier.weight(1f)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            KeypadKey(
                label = stringResource(R.string.key_delete),
                contentDescription = stringResource(R.string.cd_key_delete),
                style = KeyStyle.Delete,
                enabled = secret.isNotEmpty(),
                height = keyHeight,
                modifier = Modifier.weight(1f),
                onClick = onDelete,
            )
            SecretDigitKey('0', secret, keyHeight, onDigit, Modifier.weight(1f))
            KeypadKey(
                label = stringResource(R.string.secret_done),
                contentDescription = stringResource(R.string.cd_secret_done),
                style = KeyStyle.Enter,
                enabled = secret.length == CODE_LENGTH,
                height = keyHeight,
                modifier = Modifier.weight(1f),
                onClick = onDone,
            )
        }
    }
}

@Composable
private fun SecretDigitKey(
    digit: Char,
    secret: String,
    keyHeight: Dp,
    onDigit: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    KeypadKey(
        label = digit.toString(),
        contentDescription = stringResource(R.string.cd_key_digit, digit.toString()),
        style = KeyStyle.Digit,
        enabled = canAddSecretDigit(secret, digit),
        height = keyHeight,
        modifier = modifier,
        // One shared note for every digit, unlike the game's per-digit notes.
        soundPitch = 1f,
        // The view model applies the digit to its latest secret, so rapid taps are never lost.
        onClick = { onDigit(digit) },
    )
}
