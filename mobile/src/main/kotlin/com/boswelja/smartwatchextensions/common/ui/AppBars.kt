package com.boswelja.smartwatchextensions.common.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
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
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.watchconnection.common.Watch

/**
 * An app bar with a navigate up action.
 * @param onNavigateUp Called when up navigation is requested.
 */
@Composable
fun UpNavigationAppBar(
    title: @Composable () -> Unit = { },
    actions: @Composable RowScope.() -> Unit = { },
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
        actions = actions
    )
}

/**
 * An app bar with a watch picker.
 * @param selectedWatch The currently selected watch.
 * @param watches The list of available watches.
 * @param onWatchSelected Called when a watch is selected.
 */
@Composable
fun WatchPickerAppBar(
    selectedWatch: Watch?,
    watches: List<Watch>?,
    onWatchSelected: (Watch) -> Unit
) {
    SmallTopAppBar(
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

/**
 * An app bar with a watch picker and navigate up action.
 * @param selectedWatch The currently selected watch.
 * @param watches The list of available watches.
 * @param onWatchSelected Called when a watch is selected.
 * @param onNavigateUp Called when up navigation is requested.
 */
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

/**
 * A dropdown for selecting a watch from a list.
 * @param selectedWatch The currently selected watch.
 * @param watches The list of available watches.
 * @param onWatchSelected Called when a watch was selected.
 */
@OptIn(ExperimentalMaterialApi::class)
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
                Text(selectedWatch?.name ?: stringResource(R.string.watch_status_connecting))
            },
            trailing = {
                Icon(Icons.Outlined.ArrowDropDown, null)
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
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
