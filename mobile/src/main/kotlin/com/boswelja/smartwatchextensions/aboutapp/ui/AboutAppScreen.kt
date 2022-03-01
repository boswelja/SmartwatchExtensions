package com.boswelja.smartwatchextensions.aboutapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.core.ui.settings.SettingsHeader
import com.boswelja.smartwatchextensions.core.ui.settings.ShortcutSetting

/**
 * A Composable screen for displaying app-related information.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 */
@Composable
fun AboutAppScreen(
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    Column(modifier.padding(contentPadding)) {
        SettingsHeader(
            text = {
                Text(stringResource(R.string.about_category_about))
            }
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.about_priv_policy_title)) },
            onClick = { onNavigateTo(AboutAppDestinations.PRIVACY_POLICY.route) },
            modifier = Modifier.fillMaxWidth()
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.about_github)) },
            onClick = { onNavigateTo(AboutAppDestinations.SOURCE.route) },
            modifier = Modifier.fillMaxWidth()
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.about_changelog)) },
            onClick = { onNavigateTo(AboutAppDestinations.CHANGELOG.route) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
