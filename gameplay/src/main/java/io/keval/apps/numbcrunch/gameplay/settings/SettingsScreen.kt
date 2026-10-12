package io.keval.apps.numbcrunch.gameplay.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.keval.apps.numbcrunch.core.ui.theme.GuesserBronzeDark
import io.keval.apps.numbcrunch.core.ui.theme.GuesserChocolateDark
import io.keval.apps.numbcrunch.core.ui.theme.GuesserGoldBorder
import io.keval.apps.numbcrunch.core.ui.theme.GuesserGoldCream
import io.keval.apps.numbcrunch.core.ui.theme.GuesserOrangeMid
import io.keval.apps.numbcrunch.core.ui.theme.GuesserOrangeTop
import io.keval.apps.numbcrunch.core.ui.theme.GuesserTheme
import io.keval.apps.numbcrunch.core.ui.theme.GuesserWoodBottom
import io.keval.apps.numbcrunch.domain.settings.AudioSettings
import io.keval.apps.numbcrunch.domain.usecase.ObserveAudioSettingsUseCase
import io.keval.apps.numbcrunch.domain.usecase.SetMusicEnabledUseCase
import io.keval.apps.numbcrunch.domain.usecase.SetSoundEffectsEnabledUseCase
import io.keval.apps.numbcrunch.gameplay.R
import io.keval.apps.numbcrunch.gameplay.audio.rememberTapFeedback
import io.keval.apps.numbcrunch.gameplay.common.GuideBody
import io.keval.apps.numbcrunch.gameplay.common.GuidePanel
import io.keval.apps.numbcrunch.gameplay.common.GuideScaffold
import java.io.IOException
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    observeAudioSettings: ObserveAudioSettingsUseCase,
    setMusicEnabled: SetMusicEnabledUseCase,
    setSoundEffectsEnabled: SetSoundEffectsEnabledUseCase,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by observeAudioSettings().collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    var saveFailed by remember { mutableStateOf(false) }
    // A failed write leaves the saved value, and so the switch, unchanged.
    val save: (suspend () -> Unit) -> Unit = { write ->
        scope.launch {
            saveFailed = try {
                write()
                false
            } catch (_: IOException) {
                true
            }
        }
    }
    SettingsScreen(
        settings = settings,
        onMusicChange = { enabled -> save { setMusicEnabled(enabled) } },
        onSoundEffectsChange = { enabled -> save { setSoundEffectsEnabled(enabled) } },
        onBack = onBack,
        modifier = modifier,
        saveFailed = saveFailed,
    )
}

/** [settings] is null until the saved values load; the toggles stay disabled until then. */
@Composable
fun SettingsScreen(
    settings: AudioSettings?,
    onMusicChange: (Boolean) -> Unit,
    onSoundEffectsChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    saveFailed: Boolean = false,
) {
    GuideScaffold(stringResource(R.string.settings_title), onBack, modifier) {
        GuidePanel(stringResource(R.string.settings_sound_title)) {
            SettingToggle(
                title = stringResource(R.string.settings_music),
                body = stringResource(R.string.settings_music_body),
                checked = settings?.musicEnabled == true,
                enabled = settings != null,
                onCheckedChange = onMusicChange,
            )
            SettingToggle(
                title = stringResource(R.string.settings_effects),
                body = stringResource(R.string.settings_effects_body),
                checked = settings?.soundEffectsEnabled == true,
                enabled = settings != null,
                onCheckedChange = onSoundEffectsChange,
            )
            if (saveFailed) {
                Text(
                    stringResource(R.string.settings_save_error),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    color = GuesserOrangeTop,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
private fun SettingToggle(
    title: String,
    body: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val feedback = rememberTapFeedback()
    // The whole row is one switch for touch and accessibility.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = {
                    feedback()
                    onCheckedChange(it)
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                color = GuesserGoldCream,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            GuideBody(body)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GuesserGoldCream,
                checkedTrackColor = GuesserOrangeMid,
                checkedBorderColor = GuesserGoldBorder,
                uncheckedThumbColor = GuesserGoldBorder,
                uncheckedTrackColor = GuesserChocolateDark,
                uncheckedBorderColor = GuesserBronzeDark,
                disabledUncheckedTrackColor = GuesserWoodBottom,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun SettingsScreenPreview() {
    GuesserTheme {
        SettingsScreen(
            settings = AudioSettings(musicEnabled = false, soundEffectsEnabled = true),
            onMusicChange = {},
            onSoundEffectsChange = {},
            onBack = {},
        )
    }
}
