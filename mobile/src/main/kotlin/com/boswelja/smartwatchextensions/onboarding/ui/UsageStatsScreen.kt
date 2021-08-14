package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.analytics.getAnalytics
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.settings.appSettingsStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UsageStatsScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
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
            onShowPrivacyPolicy = onShowPrivacyPolicy
        )
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.button_next)) },
            icon = { Icon(Icons.Outlined.NavigateNext, null) },
            onClick = { onNavigateTo(OnboardingDestination.REGISTER_WATCHES) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UsageStatsCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp,
    onShowPrivacyPolicy: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val analytics = getAnalytics()
    var checkboxChecked by remember { mutableStateOf(true) }

    Card(
        modifier = modifier,
        header = {
            CardHeader(
                title = { Text(stringResource(R.string.share_usage_title)) }
            )
        }
    ) {
        Column(Modifier.padding(contentPadding)) {
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
                            coroutineScope.launch {
                                analytics.setAnalyticsEnabled(it)
                                context.appSettingsStore.updateData { settings ->
                                    settings.copy(analyticsEnabled = it)
                                }
                            }
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
