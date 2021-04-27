package com.boswelja.smartwatchextensions.common.ui

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.boswelja.smartwatchextensions.R
import com.boswelja.watchconnection.core.Watch

@Composable
fun UpNavigationAppBar(
    title: @Composable () -> Unit = { },
    actions: @Composable RowScope.() -> Unit = { },
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = title,
        backgroundColor = MaterialTheme.colors.background,
        navigationIcon = {
            IconButton(onNavigateUp) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = null
                )
            }
        },
        actions = actions
    )
}

@ExperimentalMaterialApi
@Composable
fun WatchPickerAppBar(
    selectedWatch: Watch?,
    watches: List<Watch>?,
    onWatchSelected: (Watch) -> Unit
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colors.background,
        title = {
            WatchPickerDropdown(
                selectedWatch = selectedWatch,
                watches = watches,
                onWatchSelected = onWatchSelected
            )
        },
        actions = { WikiAction() }
    )
}

@ExperimentalMaterialApi
@Composable
fun UpNavigationWatchPickerAppBar(
    selectedWatch: Watch?,
    watches: List<Watch>?,
    onWatchSelected: (Watch) -> Unit,
    onNavigateUp: () -> Unit
) {
    UpNavigationAppBar(
        onNavigateUp = onNavigateUp,
        title = {
            WatchPickerDropdown(
                selectedWatch = selectedWatch,
                watches = watches,
                onWatchSelected = onWatchSelected
            )
        },
        actions = { WikiAction() }
    )
}

@Composable
fun WikiAction() {
    val context = LocalContext.current
    val customTabsIntent = remember { CustomTabsIntent.Builder().build() }
    IconButton(
        onClick = {
            customTabsIntent.launchUrl(context, context.getString(R.string.wiki_url).toUri())
        }
    ) {
        Icon(Icons.Outlined.HelpOutline, stringResource(R.string.wiki_label))
    }
}

@ExperimentalMaterialApi
@Composable
fun WatchPickerDropdown(
    selectedWatch: Watch?,
    watches: List<Watch>?,
    onWatchSelected: (Watch) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        ListItem(
            modifier = Modifier
                .clickable { expanded = !expanded },
            text = {
                Text(selectedWatch?.name ?: stringResource(R.string.watch_status_unknown))
            },
            trailing = {
                Icon(Icons.Outlined.ArrowDropDown, null)
            }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            watches?.forEach {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onWatchSelected(it)
                    }
                ) {
                    Text(it.name)
                }
            }
        }
    }
}
