package com.boswelja.devicemanager.onboarding.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.common.rememberSharedPreferences

@ExperimentalMaterialApi
@Composable
fun UsageStatsScreen(
    onShowPrivacyPolicy: () -> Unit
) {
    val sharedPreferences = rememberSharedPreferences()
    val analytics = Analytics()
    var checkboxChecked by remember { mutableStateOf(true) }
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.Analytics,
            null,
            Modifier.size(180.dp)
        )
        Text(
            stringResource(R.string.share_usage_title),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            stringResource(R.string.share_usage_desc),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body1
        )
        ListItem(
            text = { Text(stringResource(R.string.share_usage_title)) },
            trailing = {
                Checkbox(
                    checked = checkboxChecked,
                    onCheckedChange = null // null recommended for accessibility with screenreaders
                )
            },
            modifier = Modifier
                .toggleable(
                    value = checkboxChecked,
                    onValueChange = {
                        checkboxChecked = it
                        analytics.setAnalyticsEnabled(it)
                        sharedPreferences.edit {
                            putBoolean(Analytics.ANALYTICS_ENABLED_KEY, it)
                        }
                    }
                )
                .padding(16.dp)
        )
        OutlinedButton(
            onClick = onShowPrivacyPolicy,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.about_priv_policy_title))
        }
    }
}
