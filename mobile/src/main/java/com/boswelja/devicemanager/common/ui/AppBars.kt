package com.boswelja.devicemanager.common.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun UpNavigationTopAppBar(onNavigateUp: () -> Unit) {
    TopAppBar(
        title = { },
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp,
        navigationIcon = {
            IconButton(onNavigateUp) {
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = null
                )
            }
        }
    )
}
