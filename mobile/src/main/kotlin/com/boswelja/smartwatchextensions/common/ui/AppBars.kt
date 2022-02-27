package com.boswelja.smartwatchextensions.common.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity

/**
 * An app bar with a navigate up action.
 * @param onNavigateUp Called when up navigation is requested.
 */
@Composable
fun UpNavigationAppBar(
    title: @Composable () -> Unit = { },
    onNavigateUp: () -> Unit
) {
    MediumTopAppBar(
        title = title,
        navigationIcon = {
            IconButton(onNavigateUp) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = { WikiAction() }
    )
}

@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    canNavigateUp: Boolean,
    onNavigateUp: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    MediumTopAppBar(
        title = title,
        navigationIcon = {
            AnimatedVisibility(
                visible = canNavigateUp,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(onNavigateUp) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = { WikiAction() },
        scrollBehavior = scrollBehavior
    )
}

/**
 * An app bar action for launching the wiki.
 */
@Composable
fun WikiAction() {
    val context = LocalContext.current
    IconButton(
        onClick = {
            context.startActivity { intent ->
                intent.action = Intent.ACTION_VIEW
                intent.data = context.getString(R.string.wiki_url).toUri()
                intent
            }
        }
    ) {
        Icon(Icons.Outlined.HelpOutline, stringResource(R.string.wiki_label))
    }
}
