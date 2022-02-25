package com.boswelja.smartwatchextensions.aboutapp.ui

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader

/**
 * A Composable screen for displaying app-related information.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 */
@Composable
fun AboutAppScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
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
    Card(
        modifier = modifier,
        header = {
            CardHeader(
                title = { Text(stringResource(R.string.about_category_about)) }
            )
        }
    ) {
        Column {
            Row(
                modifier = Modifier.clickable {
                    context.startActivity { intent ->
                        intent.action = Intent.ACTION_VIEW
                        intent.data = context.getString(R.string.privacy_policy_url).toUri()
                        intent
                    }
                }
            ) {
                Icon(Icons.Default.Policy, null)
                Text(stringResource(R.string.about_priv_policy_title))
            }
            Row(
                modifier = Modifier.clickable {
                    context.startActivity { intent ->
                        intent.action = Intent.ACTION_VIEW
                        intent.data = context.getString(R.string.github_url).toUri()
                        intent
                    }
                }
            ) {
                Icon(Icons.Default.Source, null)
                Text(stringResource(R.string.about_github))
            }
            Row(
                modifier = Modifier.clickable {
                    context.startActivity { intent ->
                        intent.action = Intent.ACTION_VIEW
                        intent.data = context.getString(R.string.changelog_url).toUri()
                        intent
                    }
                }
            ) {
                Icon(Icons.Default.ChangeHistory, null)
                Text(stringResource(R.string.about_changelog))
            }
        }
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

    Card(
        modifier = modifier,
        header = {
            CardHeader(title = { Text(stringResource(R.string.about_category_support)) })
        }
    ) {
        Column {
            Row(
                modifier = Modifier.clickable {
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
                }
            ) {
                Icon(Icons.Default.Share, null)
                Text(stringResource(R.string.about_share_title))
            }
        }
    }
}
