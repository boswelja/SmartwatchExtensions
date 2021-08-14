package com.boswelja.smartwatchextensions.common.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.AppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.watchconnection.core.Watch

@Composable
fun UpNavigationAppBar(
    title: @Composable () -> Unit = { },
    actions: @Composable RowScope.() -> Unit = { },
    backgroundColor: Color = MaterialTheme.colors.background,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        title = title,
        backgroundColor = backgroundColor,
        elevation = elevation,
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

@Composable
fun WatchPickerAppBar(
    selectedWatch: Watch?,
    watches: List<Watch>?,
    backgroundColor: Color = MaterialTheme.colors.background,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onWatchSelected: (Watch) -> Unit
) {
    TopAppBar(
        backgroundColor = backgroundColor,
        elevation = elevation,
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
fun UpNavigationWatchPickerAppBar(
    selectedWatch: Watch?,
    watches: List<Watch>?,
    backgroundColor: Color = MaterialTheme.colors.background,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onWatchSelected: (Watch) -> Unit,
    onNavigateUp: () -> Unit
) {
    UpNavigationAppBar(
        onNavigateUp = onNavigateUp,
        backgroundColor = backgroundColor,
        elevation = elevation,
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
