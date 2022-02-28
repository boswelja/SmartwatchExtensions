package com.boswelja.smartwatchextensions.aboutapp.ui

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.core.ui.settings.SettingsHeader
import com.boswelja.smartwatchextensions.core.ui.settings.ShortcutSetting

/**
 * A Composable screen for displaying app-related information.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 */
@Composable
fun AboutAppScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        item {
            AboutApp()
        }
        item {
            SupportApp()
        }
    }
}

/**
 * A Composable for displaying info about the app.
 * @param modifier [Modifier].
 */
@Composable
fun AboutApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier) {
        SettingsHeader(
            text = {
                Text(stringResource(R.string.about_category_about))
            }
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.about_priv_policy_title)) },
            onClick = {
                context.startActivity { intent ->
                    intent.action = Intent.ACTION_VIEW
                    intent.data = context.getString(R.string.privacy_policy_url).toUri()
                    intent
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.about_github)) },
            onClick = {
                context.startActivity { intent ->
                    intent.action = Intent.ACTION_VIEW
                    intent.data = context.getString(R.string.github_url).toUri()
                    intent
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.about_changelog)) },
            onClick = {
                context.startActivity { intent ->
                    intent.action = Intent.ACTION_VIEW
                    intent.data = context.getString(R.string.changelog_url).toUri()
                    intent
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * A Composable for displaying options for supporting the app.
 * @param modifier [Modifier].
 */
@Composable
fun SupportApp(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val shareTitle = stringResource(R.string.app_name)
    val shareDataUri =
        Uri.Builder()
            .apply {
                scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                authority(context.resources.getResourcePackageName(R.mipmap.ic_launcher))
                appendPath(context.resources.getResourceTypeName(R.mipmap.ic_launcher))
                appendPath(context.resources.getResourceEntryName(R.mipmap.ic_launcher))
            }
            .build()

    Column(modifier) {
        SettingsHeader(
            text = {
                Text(stringResource(R.string.about_category_support))
            }
        )
        ShortcutSetting(
            text = { Text(stringResource(R.string.about_share_title)) },
            onClick = {
                Intent()
                    .apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            context.getString(
                                com.boswelja.smartwatchextensions.core.common.R.string.play_store_link
                            )
                        )
                        putExtra(Intent.EXTRA_TITLE, shareTitle)
                        data = shareDataUri
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        type = "text/plain"
                    }
                    .also {
                        context.startActivity(Intent.createChooser(it, null))
                    }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
