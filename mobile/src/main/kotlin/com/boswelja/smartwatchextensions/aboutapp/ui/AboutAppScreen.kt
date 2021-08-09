package com.boswelja.smartwatchextensions.aboutapp.ui

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChangeHistory
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Source
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.BuildConfig
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.Card
import com.boswelja.smartwatchextensions.common.ui.CardHeader
import com.boswelja.smartwatchextensions.donate.ui.DonateActivity

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
            Versions()
        }
        item {
            SupportApp()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AboutApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val viewModel: AboutAppViewModel = viewModel()
    Card(
        modifier = modifier,
        header = {
            CardHeader(
                title = { Text(stringResource(R.string.about_category_about)) }
            )
        }
    ) {
        Column {
            ListItem(
                text = { Text(stringResource(R.string.about_priv_policy_title)) },
                icon = { Icon(Icons.Outlined.Policy, null) },
                modifier = Modifier.clickable {
                    viewModel.customTabsIntent.launchUrl(
                        context, context.getString(R.string.privacy_policy_url).toUri()
                    )
                }
            )
            ListItem(
                text = { Text(stringResource(R.string.about_github)) },
                icon = { Icon(Icons.Outlined.Source, null) },
                modifier = Modifier.clickable {
                    viewModel.customTabsIntent.launchUrl(
                        context, context.getString(R.string.github_url).toUri()
                    )
                }
            )
            ListItem(
                text = { Text(stringResource(R.string.about_changelog)) },
                icon = { Icon(Icons.Outlined.ChangeHistory, null) },
                modifier = Modifier.clickable {
                    viewModel.customTabsIntent.launchUrl(
                        context, context.getString(R.string.changelog_url).toUri()
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Versions(modifier: Modifier = Modifier) {
    val viewModelAbout: AboutAppViewModel = viewModel()
    val watchVersion by viewModelAbout.watchAppVersion.collectAsState(null)
    Card(
        modifier = modifier,
        header = {
            CardHeader(
                title = { Text(stringResource(R.string.about_category_versions)) }
            )
        }
    ) {
        Column {
            ListItem(
                text = {
                    Text(
                        stringResource(
                            R.string.about_phone_version_title, BuildConfig.VERSION_NAME
                        )
                    )
                },
                secondaryText = { Text(BuildConfig.VERSION_CODE.toString()) },
                icon = { Icon(Icons.Outlined.Info, null) }
            )
            ListItem(
                text = {
                    when {
                        watchVersion?.first != null -> {
                            Text(
                                stringResource(
                                    R.string.about_watch_version_title, watchVersion!!.first!!
                                )
                            )
                        }
                        watchVersion == null -> {
                            Text(
                                stringResource(R.string.about_watch_version_failed)
                            )
                        }
                        else -> {
                            Text(stringResource(R.string.about_watch_version_loading))
                        }
                    }
                },
                secondaryText = if (watchVersion?.second != null) {
                    { Text(watchVersion!!.second!!) }
                } else null,
                icon = { Icon(Icons.Outlined.Info, null) }
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
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
                appendPath((context.resources.getResourceEntryName(R.mipmap.ic_launcher)))
            }
            .build()

    Card(
        modifier = modifier,
        header = {
            CardHeader(title = { Text(stringResource(R.string.about_category_support)) })
        }
    ) {
        Column {
            ListItem(
                text = { Text(stringResource(R.string.about_donate_title)) },
                icon = { Icon(Icons.Outlined.FavoriteBorder, null) },
                modifier = Modifier.clickable {
                    context.startActivity<DonateActivity>()
                }
            )
            ListItem(
                text = { Text(stringResource(R.string.about_share_title)) },
                icon = { Icon(Icons.Outlined.Share, null) },
                modifier = Modifier.clickable {
                    Intent()
                        .apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.play_store_link))
                            putExtra(Intent.EXTRA_TITLE, shareTitle)
                            data = shareDataUri
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                            type = "text/plain"
                        }
                        .also {
                            context.startActivity(Intent.createChooser(it, null))
                        }
                }
            )
        }
    }
}
