package com.boswelja.devicemanager.watchmanager.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.LifecycleAwareTimer
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import com.boswelja.devicemanager.watchinfo.ui.WatchInfoActivity
import com.boswelja.devicemanager.watchmanager.WatchManager
import com.boswelja.devicemanager.watchmanager.item.Watch
import com.boswelja.devicemanager.watchmanager.ui.register.RegisterWatchActivity

class WatchManagerActivity : AppCompatActivity() {

    private val watchManager by lazy { WatchManager.getInstance(this) }
    private val refreshDataTimer = LifecycleAwareTimer(period = 20) {
        watchManager.refreshData()
    }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                Scaffold(
                    topBar = { UpNavigationAppBar(onNavigateUp = { finish() }) }
                ) {
                    val registeredWatches by watchManager.registeredWatches.observeAsState()
                    LazyColumn(Modifier.padding(vertical = 8.dp)) {
                        item {
                            ListItem(
                                text = {
                                    Text(stringResource(R.string.watch_manager_add_watch_title))
                                },
                                icon = {
                                    Icon(Icons.Outlined.Add, null)
                                },
                                modifier = Modifier.clickable {
                                    openWatchSetupActivity()
                                }
                            )
                        }
                        stickyHeader {
                            HeaderItem(
                                stringResource(R.string.watch_manager_registered_watch_header)
                            )
                        }
                        items(registeredWatches ?: emptyList()) { watch ->
                            ListItem(
                                icon = {
                                    Icon(
                                        Icons.Outlined.Watch,
                                        null,
                                        Modifier.size(40.dp)
                                    )
                                },
                                text = { Text(watch.name) },
                                secondaryText = { Text(stringResource(watch.status.stringRes)) },
                                modifier = Modifier.clickable {
                                    openWatchInfoActivity(watch)
                                }
                            )
                        }
                    }
                }
            }
        }

        lifecycle.addObserver(refreshDataTimer)
    }

    /** Opens a [RegisterWatchActivity]. */
    private fun openWatchSetupActivity() {
        Intent(this, RegisterWatchActivity::class.java)
            .also { startActivity(it) }
    }

    /** Opens a [WatchInfoActivity]. */
    private fun openWatchInfoActivity(watch: Watch) {
        Intent(this, WatchInfoActivity::class.java)
            .apply { putExtra(WatchInfoActivity.EXTRA_WATCH_ID, watch.id) }
            .also { startActivity(it) }
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
