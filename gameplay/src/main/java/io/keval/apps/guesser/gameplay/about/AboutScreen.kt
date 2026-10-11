package io.keval.apps.guesser.gameplay.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.keval.apps.guesser.core.ui.theme.GuesserTheme
import io.keval.apps.guesser.gameplay.R
import io.keval.apps.guesser.gameplay.common.GuideAction
import io.keval.apps.guesser.gameplay.common.GuideBody
import io.keval.apps.guesser.gameplay.common.GuidePanel
import io.keval.apps.guesser.gameplay.common.GuideScaffold

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preview = LocalInspectionMode.current
    val version = remember(context, preview) {
        if (preview) "1.0" else {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }
    }
    var linkError by remember { mutableStateOf(false) }
    val openLink: (String) -> Unit = { url ->
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            linkError = false
        } catch (error: ActivityNotFoundException) {
            Log.w("GuesserAbout", "No application can open the external link", error)
            linkError = true
        }
    }
    GuideScaffold(stringResource(R.string.label_about), onBack, modifier) {
        Image(
            painterResource(R.drawable.home_title), stringResource(R.string.cd_game_title),
            Modifier.fillMaxWidth().height(120.dp),
        )
        GuidePanel(stringResource(R.string.about_tagline)) {
            GuideBody(stringResource(R.string.about_intro))
        }
        GuidePanel(stringResource(R.string.about_story_title)) {
            GuideBody(stringResource(R.string.about_story_body))
        }
        GuidePanel(stringResource(R.string.about_mind_title)) {
            GuideBody(stringResource(R.string.about_mind_body))
        }
        GuidePanel(stringResource(R.string.about_credits_title)) {
            GuideBody(stringResource(R.string.about_credits_body))
            GuideBody(stringResource(R.string.about_version, version ?: stringResource(R.string.about_version_unknown)))
        }
        GuidePanel(stringResource(R.string.about_source_title)) {
            GuideBody(stringResource(R.string.about_source_body))
            GuideAction(stringResource(R.string.about_source_action), {
                openLink("https://github.com/theKeval/Guesser")
            }, Modifier.fillMaxWidth())
            GuideAction(stringResource(R.string.about_license_action), {
                openLink("https://www.apache.org/licenses/LICENSE-2.0")
            }, Modifier.fillMaxWidth())
            if (linkError) {
                GuideBody(stringResource(R.string.about_link_error))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Large text", widthDp = 320, heightDp = 568, fontScale = 2f)
@Preview(name = "Tablet", widthDp = 800, heightDp = 1280)
@Composable
private fun AboutPreview() {
    GuesserTheme { AboutScreen(onBack = {}) }
}
