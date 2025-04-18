package com.boswelja.smartwatchextensions.about.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ChipDefaults.secondaryChipColors
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.GooglePlayUtils
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.RotaryHandler
import com.boswelja.smartwatchextensions.common.ui.RoundScreenDefaults.VerticalPadding
import com.boswelja.smartwatchextensions.common.ui.roundScreenPadding
import kotlinx.coroutines.launch

/**
 * A screen for displaying info about the app.
 */
@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    RotaryHandler { delta ->
        coroutineScope.launch {
            scrollState.scrollBy(delta)
        }
    }

    Column(
        modifier
            .verticalScroll(scrollState)
            .padding(8.dp)
            .roundScreenPadding(roundPaddingValues = PaddingValues(bottom = VerticalPadding)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppInfo(Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Links(Modifier.fillMaxWidth())
    }
}

/**
 * A Composable for showing some info about the app.
 */
@Composable
fun AppInfo(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier.size(42.dp),
            bitmap = context.packageManager.getApplicationIcon(context.packageName)
                .toBitmap().asImageBitmap(),
            contentDescription = null
        )
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.caption1,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.version_string, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.title2,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * A Composable for offering quick links to app-related pages.
 */
@Composable
fun Links(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chipColors = secondaryChipColors()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Chip(
            onClick = { context.startActivity(GooglePlayUtils.getPlayStoreIntent(context)) },
            label = {
                Text(stringResource(R.string.open_play_store_title))
            },
            icon = {
                Icon(
                    modifier = Modifier.size(ChipDefaults.IconSize),
                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                    contentDescription = null
                )
            },
            colors = chipColors
        )
        Chip(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply { data = Uri.fromParts("package", context.packageName, null) }
                context.startActivity(intent)
            },
            label = {
                Text(stringResource(R.string.open_app_info_title))
            },
            icon = {
                Icon(
                    modifier = Modifier.size(ChipDefaults.IconSize),
                    imageVector = Icons.Outlined.SettingsApplications,
                    contentDescription = null
                )
            },
            colors = chipColors
        )
    }
}
