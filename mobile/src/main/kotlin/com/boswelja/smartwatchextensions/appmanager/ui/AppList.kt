package com.boswelja.smartwatchextensions.appmanager.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.appmanager.App
import com.boswelja.smartwatchextensions.common.ui.HeaderItem
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun AppList(viewModel: AppManagerViewModel, onAppClick: (App) -> Unit) {
    val userApps by viewModel.userApps.observeAsState()
    val systemApps by viewModel.systemApps.observeAsState()
    LazyColumn {
        userApps?.let { apps ->
            stickyHeader {
                HeaderItem(stringResource(R.string.app_manager_section_user_apps))
            }
            items(apps) { app ->
                AppItem(app, onAppClick)
            }
        }
        systemApps?.let { apps ->
            stickyHeader {
                HeaderItem(stringResource(R.string.app_manager_section_system_apps))
            }
            items(apps) { app ->
                AppItem(app, onAppClick)
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun AppItem(app: App, onClick: (App) -> Unit) {
    ListItem(
        text = { Text(app.label) },
        secondaryText = { Text(app.version) },
        icon = {
            if (app.icon != null) {
                Image(
                    app.icon.asImageBitmap(),
                    contentDescription = null,
                    Modifier.size(40.dp)
                )
            } else {
                Image(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    Modifier.size(40.dp)
                )
            }
        },
        modifier = Modifier.clickable { onClick(app) }
    )
}
