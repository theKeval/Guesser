package io.keval.apps.numbcrunch.gameplay.info

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.keval.apps.numbcrunch.core.ui.theme.GuesserTheme
import io.keval.apps.numbcrunch.gameplay.R
import io.keval.apps.numbcrunch.gameplay.common.GuideBody
import io.keval.apps.numbcrunch.gameplay.common.GuideGuess
import io.keval.apps.numbcrunch.gameplay.common.GuidePanel
import io.keval.apps.numbcrunch.gameplay.common.GuideScaffold

@Composable
fun GameplayInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GuideScaffold(stringResource(R.string.label_gameplay), onBack, modifier) {
        GuidePanel(stringResource(R.string.rules_goal_title)) {
            GuideBody(stringResource(R.string.rules_goal_body))
            GuideBody(stringResource(R.string.rules_code_body))
        }
        GuidePanel(stringResource(R.string.rules_clues_title)) {
            GuideBody(stringResource(R.string.rules_clues_body))
            GuideBody(stringResource(R.string.rules_smyle_body))
        }
        GuidePanel(stringResource(R.string.rules_examples_title)) {
            GuideBody(stringResource(R.string.rules_example_secret, LearningExamples.SECRET))
            val explanations = listOf(
                R.string.example_mixed, R.string.example_rearranged,
                R.string.example_absent, R.string.example_winner,
            )
            LearningExamples.evaluations.forEachIndexed { index, evaluation ->
                GuideGuess(evaluation)
                GuideBody(stringResource(explanations[index]))
            }
        }
        GuidePanel(stringResource(R.string.rules_modes_title)) {
            GuideBody(stringResource(R.string.rules_single_body))
            GuideBody(stringResource(R.string.rules_double_body))
        }
        GuidePanel(stringResource(R.string.rules_controls_title)) {
            GuideBody(stringResource(R.string.rules_controls_body))
        }
        GuidePanel(stringResource(R.string.rules_strategy_title)) {
            GuideBody(stringResource(R.string.rules_strategy_body))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Large text", widthDp = 320, heightDp = 568, fontScale = 2f)
@Preview(name = "Tablet", widthDp = 800, heightDp = 1280)
@Composable
private fun GameplayInfoPreview() {
    GuesserTheme { GameplayInfoScreen(onBack = {}) }
}
