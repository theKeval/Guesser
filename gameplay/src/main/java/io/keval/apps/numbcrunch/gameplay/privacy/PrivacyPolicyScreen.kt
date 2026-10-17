package io.keval.apps.numbcrunch.gameplay.privacy

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.keval.apps.numbcrunch.core.ui.theme.NumbCrunchTheme
import io.keval.apps.numbcrunch.gameplay.R
import io.keval.apps.numbcrunch.gameplay.common.GuideBody
import io.keval.apps.numbcrunch.gameplay.common.GuidePanel
import io.keval.apps.numbcrunch.gameplay.common.GuideScaffold
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Shows the privacy policy inside the game, styled like the other guide screens. The text is the
 * repository's PRIVACY.md, bundled at build time, so it always matches the published policy.
 */
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preview = LocalInspectionMode.current
    // Null while loading; a failed result keeps the screen useful with a short explanation.
    val document by produceState<Result<PrivacyPolicyDocument>?>(null, context, preview) {
        value = if (preview) Result.success(PrivacyPolicyDocument.parse(PreviewPolicy)) else loadPolicy(context)
    }
    GuideScaffold(stringResource(R.string.label_privacy_policy), onBack, modifier) {
        val policy = document?.getOrNull()
        when {
            policy != null -> SelectionContainer {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    if (policy.lead.isNotEmpty()) {
                        GuidePanel(policy.title) { PolicyBlocks(policy.lead) }
                    }
                    policy.sections.forEach { section ->
                        GuidePanel(section.title) { PolicyBlocks(section.blocks) }
                    }
                }
            }
            document != null -> GuidePanel(stringResource(R.string.label_privacy_policy)) {
                GuideBody(stringResource(R.string.privacy_policy_unavailable))
            }
        }
    }
}

@Composable
private fun PolicyBlocks(blocks: List<PolicyBlock>) {
    blocks.forEach { block ->
        when (block) {
            is PolicyBlock.Paragraph -> GuideBody(block.text)
            is PolicyBlock.Bullets -> block.items.forEach { PolicyBullet(it) }
        }
    }
}

@Composable
private fun PolicyBullet(text: AnnotatedString) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GuideBody("•", Modifier.clearAndSetSemantics {})
        GuideBody(text, Modifier.weight(1f))
    }
}

private suspend fun loadPolicy(context: Context): Result<PrivacyPolicyDocument> = withContext(Dispatchers.IO) {
    try {
        Result.success(
            context.assets.open(PRIVACY_POLICY_ASSET).bufferedReader().use { PrivacyPolicyDocument.parse(it.readText()) },
        )
    } catch (error: IOException) {
        Log.e("NumbCrunchPrivacy", "The bundled privacy policy could not be read", error)
        Result.failure(error)
    }
}

private const val PreviewPolicy = """# NumbCrunch Privacy Policy

**Effective date:** October 16, 2026

This privacy policy explains how NumbCrunch handles information.

## Summary

NumbCrunch does not collect or share any personal information.

## Information Stored Only on Your Device

- **Settings:** whether background music and sound effects are turned on.
- **Tutorial progress:** whether you have completed the first-launch tutorial.

## Contact

Email us at [hello@keval.io](mailto:hello@keval.io).
"""

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Large text", widthDp = 320, heightDp = 568, fontScale = 2f)
@Composable
private fun PrivacyPolicyPreview() {
    NumbCrunchTheme { PrivacyPolicyScreen(onBack = {}) }
}
