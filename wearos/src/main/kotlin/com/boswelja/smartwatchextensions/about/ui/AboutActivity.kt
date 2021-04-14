package com.boswelja.smartwatchextensions.about.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.GooglePlayUtils
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.AppTheme

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                AboutScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .verticalScroll(scrollState)
            .padding(top = 8.dp, bottom = 72.dp, start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WearableExtensionsInfo()
        Links()
    }
}

@Preview(showBackground = true)
@Composable
fun WearableExtensionsInfo() {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            context.packageManager.getApplicationIcon(context.packageName)
                .toBitmap().asImageBitmap(),
            null
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.version_string, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Links() {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxWidth().padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { context.startActivity(GooglePlayUtils.getPlayStoreIntent(context)) }
        ) {
            Icon(Icons.Outlined.OpenInNew, null)
            Text(stringResource(R.string.open_play_store_title))
        }
        OutlinedButton(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply { data = Uri.fromParts("package", context.packageName, null) }
                context.startActivity(intent)
            }
        ) {
            Icon(Icons.Outlined.OpenInNew, null)
            Text(stringResource(R.string.open_app_info_title))
        }
    }
}
