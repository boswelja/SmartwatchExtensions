package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.appmanager.App

class AppListFragment : Fragment() {

    private val viewModel: AppManagerViewModel by activityViewModels()

    private val onAppClick = { app: App ->
        findNavController()
            .navigate(AppListFragmentDirections.appListFragmentToAppInfoFragment(app))
    }

    @ExperimentalFoundationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
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
        }
    }

    @Composable
    fun AppItem(app: App, onClick: (App) -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onClick(app) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = app.icon?.bitmap?.asImageBitmap()
                ?: ImageBitmap.imageResource(R.drawable.android_head)
            Image(
                bitmap = icon,
                null,
                Modifier.size(56.dp)
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            ) {
                Text(
                    app.label,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    app.version,
                    style = MaterialTheme.typography.body2
                )
            }
        }
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
}
