package com.boswelja.devicemanager.common.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.watchmanager.item.Watch

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
        }
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
        }
    )
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