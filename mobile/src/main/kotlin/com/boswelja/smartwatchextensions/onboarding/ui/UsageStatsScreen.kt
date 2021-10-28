package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R

/**
 * A Composable screen for displaying analytics information, and allowing the user to opt-in/out.
 * @param modifier [Modifier].
 * @param onShowPrivacyPolicy Called when the user requests the privacy policy.
 * @param onNavigateNext Called when navigation is requested.
 */
@Composable
fun UsageStatsScreen(
    modifier: Modifier = Modifier,
    onShowPrivacyPolicy: () -> Unit,
    onNavigateNext: () -> Unit,
    onOptOut: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Analytics,
            null,
            Modifier.size(180.dp)
        )
        AnalyticsInformation(
            modifier = Modifier.fillMaxWidth(),
            onShowPrivacyPolicy = onShowPrivacyPolicy
        )
        AnalyticsActionButtons(
            modifier = Modifier.fillMaxWidth(),
            onNavigateNext = onNavigateNext,
            onOptOut = onOptOut
        )
    }
}

/**
 * Display actions for the user to take regarding analytics.
 * @param modifier [Modifier].
 * @param onNavigateNext Called when the user accepts and continues.
 * @param onOptOut Called when the user opts out and continues.
 */
@Composable
fun AnalyticsActionButtons(
    modifier: Modifier = Modifier,
    onNavigateNext: () -> Unit,
    onOptOut: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onOptOut) {
            Text(text = stringResource(R.string.button_opt_out))
        }
        Button(
            onClick = onNavigateNext,
            contentPadding = PaddingValues(start = 16.dp, end = 24.dp)
        ) {
            Icon(Icons.Default.NavigateNext, null)
            Spacer(Modifier.width(8.dp))
            Text(text = stringResource(R.string.button_next))
        }
    }
}

/**
 * A Composable for displaying a brief overview of analytics, along with a quick opt-in/out switch.
 * @param modifier [Modifier].
 * @param onShowPrivacyPolicy Called when the user requests the privacy policy.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AnalyticsInformation(
    modifier: Modifier = Modifier,
    onShowPrivacyPolicy: () -> Unit
) {
    Column(modifier) {
        Text(stringResource(R.string.share_usage_desc))
        TextButton(
            onClick = onShowPrivacyPolicy,
        ) {
            Text(stringResource(R.string.about_priv_policy_title))
        }
    }
}
