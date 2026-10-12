package io.keval.apps.numbcrunch.gameplay.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Readability floor: body text (16 sp) never drops below 13 sp, and at most a quarter of the
// player's chosen font size is undone.
private const val BODY_TEXT_SP = 16f
private const val MIN_BODY_TEXT_SP = 13f
private const val MAX_SHRINK = 0.75f
private const val SEARCH_STEPS = 6

/** Smallest zoom allowed for the player's [fontScale]. */
internal fun minimumFitScale(fontScale: Float): Float =
    maxOf(MAX_SHRINK, MIN_BODY_TEXT_SP / (BODY_TEXT_SP * fontScale)).coerceAtMost(1f)

/**
 * Largest scale in [minScale]..1 for which [fits] holds. When even [minScale] does not fit,
 * returns 1: the content will scroll anyway, so it keeps its natural size.
 */
internal fun largestFittingScale(minScale: Float, steps: Int = SEARCH_STEPS, fits: (Float) -> Boolean): Float {
    if (fits(1f)) return 1f
    if (minScale >= 1f || !fits(minScale)) return 1f
    var low = minScale
    var high = 1f
    repeat(steps) {
        val mid = (low + high) / 2f
        if (fits(mid)) low = mid else high = mid
    }
    return low
}

/**
 * Shows [content] without scrolling by zooming it out just enough to fit the available height,
 * within a readability floor. Text, spacing and decoration shrink together, and text re-wraps
 * at the wider effective width. If it cannot fit, the content keeps its natural size and
 * scrolls, so nothing is ever clipped. Use only for non-interactive content, since zooming
 * also shrinks touch targets.
 */
@Composable
internal fun FitToHeight(
    modifier: Modifier = Modifier,
    spacing: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    @Composable
    fun Scaled(scale: Float, columnModifier: Modifier) {
        CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
            Column(columnModifier, verticalArrangement = Arrangement.spacedBy(spacing), content = content)
        }
    }

    SubcomposeLayout(modifier) { constraints ->
        val scale = if (!constraints.hasBoundedHeight) {
            1f
        } else {
            val natural = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
            var trial = 0
            largestFittingScale(minimumFitScale(density.fontScale)) { candidate ->
                val measured = subcompose("trial${trial++}") { Scaled(candidate, Modifier) }
                    .first()
                    .measure(natural)
                measured.height <= constraints.maxHeight
            }
        }
        val placeable = subcompose("content") { Scaled(scale, Modifier.verticalScroll(scrollState)) }
            .first()
            .measure(constraints)
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
}
