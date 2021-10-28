package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.CardHeader

/**
 * A Composable screen for displaying analytics information, and allowing the user to opt-in/out.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onSetAnalyticsEnabled Called when analytics has been set.
 * @param onShowPrivacyPolicy Called when the user requests the privacy policy.
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun UsageStatsScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onSetAnalyticsEnabled: (Boolean) -> Unit,
    onShowPrivacyPolicy: () -> Unit,
    onNavigateTo: (OnboardingDestination) -> Unit
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.Analytics,
            null,
            Modifier.size(180.dp)
        )
        UsageStatsCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = contentPadding,
            onSetAnalyticsEnabled = onSetAnalyticsEnabled,
            onShowPrivacyPolicy = onShowPrivacyPolicy
        )
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.button_next)) },
            icon = { Icon(Icons.Outlined.NavigateNext, null) },
            onClick = { onNavigateTo(OnboardingDestination.REGISTER_WATCHES) }
        )
    }
}

/**
 * A Composable for displaying a brief overview of analytics, along with a quick opt-in/out switch.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onSetAnalyticsEnabled Called when analytics has been set.
 * @param onShowPrivacyPolicy Called when the user requests the privacy policy.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UsageStatsCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp,
    onSetAnalyticsEnabled: (Boolean) -> Unit,
    onShowPrivacyPolicy: () -> Unit
) {
    var checkboxChecked by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier
    ) {
        Column(Modifier.padding(contentPadding)) {
            CardHeader(
                title = { Text(stringResource(R.string.share_usage_title)) }
            )
            Text(stringResource(R.string.share_usage_desc))
            ListItem(
                text = { Text(stringResource(R.string.share_usage_title)) },
                trailing = {
                    Checkbox(checked = checkboxChecked, onCheckedChange = null)
                },
                modifier = Modifier
                    .toggleable(
                        value = checkboxChecked,
                        onValueChange = {
                            checkboxChecked = it
                            onSetAnalyticsEnabled(it)
                        }
                    )
                    .padding(contentPadding)
            )
            OutlinedButton(
                onClick = onShowPrivacyPolicy,
                modifier = Modifier.padding(top = contentPadding)
            ) {
                Text(stringResource(R.string.about_priv_policy_title))
            }
        }
    }
}
