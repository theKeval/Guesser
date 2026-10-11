package io.keval.apps.guesser.gameplay.game

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import io.keval.apps.guesser.core.ui.theme.GuesserGoldCream
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeMid
import io.keval.apps.guesser.core.ui.theme.GuesserOrangeTop
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

private const val CONFETTI_DURATION_S = 4.2f
private const val FADE_S = 0.9f

// Physics in dp: gravity pulls down, linear air drag slows pieces to a gentle flutter.
private const val GRAVITY = 560f
private const val DRAG = 2.5f

private val ConfettiColors = listOf(
    GuesserOrangeTop,
    GuesserOrangeMid,
    GuesserGoldCream,
    Color(0xFF4FD1C5),
    Color(0xFF9BDC62),
    Color(0xFFF2734F),
    Color(0xFFB79CFF),
)

private class ConfettiPiece(
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val delay: Float,
    val color: Color,
    val width: Float,
    val height: Float,
    val isRound: Boolean,
    val rotation: Float,
    val spin: Float,
    val flipSpeed: Float,
    val wobbleAmplitude: Float,
    val wobbleSpeed: Float,
    val phase: Float,
)

/** Pieces are placed in fractions of the canvas size; speeds and sizes are in dp. */
private fun createConfetti(random: Random): List<ConfettiPiece> {
    fun piece(startX: Float, startY: Float, velocityX: Float, velocityY: Float, delay: Float): ConfettiPiece {
        val isRound = random.nextFloat() < 0.25f
        val width = if (isRound) random.nextFloat() * 3f + 6f else random.nextFloat() * 5f + 7f
        return ConfettiPiece(
            startX = startX,
            startY = startY,
            velocityX = velocityX,
            velocityY = velocityY,
            delay = delay,
            color = ConfettiColors.random(random),
            width = width,
            height = if (isRound) width else width * (random.nextFloat() * 0.25f + 0.4f),
            isRound = isRound,
            rotation = random.nextFloat() * 360f,
            spin = (random.nextFloat() - 0.5f) * 720f,
            flipSpeed = random.nextFloat() * 8f + 4f,
            wobbleAmplitude = random.nextFloat() * 14f + 4f,
            wobbleSpeed = random.nextFloat() * 4f + 3f,
            phase = random.nextFloat() * 6.28f,
        )
    }

    val pieces = ArrayList<ConfettiPiece>(170)
    // Two cannons fire up and inward from the lower corners.
    for (side in listOf(-1f, 1f)) {
        repeat(60) {
            val speed = random.nextFloat() * 800f + 1100f
            val angle = Math.toRadians((random.nextFloat() * 28f + 58f).toDouble())
            pieces += piece(
                startX = if (side < 0) -0.02f else 1.02f,
                startY = 0.78f + random.nextFloat() * 0.06f,
                velocityX = -side * speed * cos(angle).toFloat(),
                velocityY = -speed * sin(angle).toFloat(),
                delay = random.nextFloat() * 0.18f,
            )
        }
    }
    // A light shower from the top fills the middle.
    repeat(45) {
        pieces += piece(
            startX = random.nextFloat(),
            startY = -0.03f,
            velocityX = (random.nextFloat() - 0.5f) * 120f,
            velocityY = random.nextFloat() * 120f,
            delay = 0.25f + random.nextFloat() * 1.1f,
        )
    }
    return pieces
}

/** A one-shot celebratory confetti burst that draws over the whole [modifier] area. */
@Composable
fun ConfettiBurst(modifier: Modifier = Modifier) {
    val pieces = remember { createConfetti(Random(System.nanoTime())) }
    var elapsed by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (elapsed < CONFETTI_DURATION_S) {
            withFrameNanos { now -> elapsed = (now - start) / 1_000_000_000f }
        }
    }
    if (elapsed >= CONFETTI_DURATION_S) return

    Canvas(modifier = modifier) {
        val dp = density
        val fade = ((CONFETTI_DURATION_S - elapsed) / FADE_S).coerceIn(0f, 1f)
        pieces.forEach { piece ->
            val t = elapsed - piece.delay
            if (t <= 0f) return@forEach
            val damping = (1f - exp(-DRAG * t)) / DRAG
            val terminal = GRAVITY / DRAG
            val x = piece.startX * size.width +
                dp * (piece.velocityX * damping + piece.wobbleAmplitude * sin(piece.wobbleSpeed * t + piece.phase))
            val y = piece.startY * size.height +
                dp * (terminal * t + (piece.velocityY - terminal) * damping)
            if (y > size.height + 40f * dp) return@forEach

            val w = piece.width * dp
            val h = piece.height * dp
            translate(x, y) {
                rotate(piece.rotation + piece.spin * t, pivot = Offset.Zero) {
                    // Squashing one axis makes the paper look like it is tumbling.
                    scale(1f, abs(cos(piece.flipSpeed * t + piece.phase)).coerceAtLeast(0.15f), pivot = Offset.Zero) {
                        if (piece.isRound) {
                            drawCircle(piece.color, radius = w / 2f, center = Offset.Zero, alpha = fade)
                        } else {
                            drawRect(piece.color, topLeft = Offset(-w / 2f, -h / 2f), size = Size(w, h), alpha = fade)
                        }
                    }
                }
            }
        }
    }
}
