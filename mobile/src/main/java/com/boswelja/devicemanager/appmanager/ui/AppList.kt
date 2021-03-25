package com.boswelja.devicemanager.appmanager.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.App

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun AppList(viewModel: AppManagerViewModel, onAppClick: (App) -> Unit) {
    val userApps = viewModel.userApps.observeAsState()
    val systemApps = viewModel.systemApps.observeAsState()
    LazyColumn {
        stickyHeader {
            HeaderItem(stringResource(R.string.app_manager_section_user_apps))
        }
        userApps.value?.let {
            items(it) { app ->
                AppItem(app, onAppClick)
            }
        }
        stickyHeader {
            HeaderItem(stringResource(R.string.app_manager_section_system_apps))
        }
        systemApps.value?.let {
            items(it) { app ->
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
            if (app.icon?.bitmap != null) {
                Image(
                    app.icon!!.bitmap.asImageBitmap(),
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

@Composable
fun HeaderItem(text: String) {
    Box(Modifier.background(MaterialTheme.colors.background)) {
        Text(
            text,
            Modifier
                .fillMaxWidth()
                .padding(start = 72.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colors.primary
        )
    }
}
