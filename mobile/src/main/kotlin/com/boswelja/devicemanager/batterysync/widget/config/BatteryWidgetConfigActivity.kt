package com.boswelja.devicemanager.batterysync.widget.config

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.BaseWidgetConfigActivity
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar
import com.boswelja.devicemanager.watchmanager.item.Watch

class BatteryWidgetConfigActivity : BaseWidgetConfigActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val (selectedWatch, onWatchSelected) = remember { mutableStateOf<Watch?>(null) }
            AppTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(onNavigateUp = { finish() })
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_finish)) },
                            icon = { Icon(Icons.Outlined.Check, null) },
                            onClick = {
                                selectedWatch?.let {
                                    finishWidgetConfig(it)
                                }
                            }
                        )
                    }
                ) {
                    Column(Modifier.fillMaxSize()) {
                        val viewModel: BatteryWidgetConfigViewModel = viewModel()
                        val registeredWatches by viewModel.registeredWatches.observeAsState()
                        Text(
                            stringResource(R.string.widget_config_battery_stats_hint),
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                        WatchPickerList(
                            watches = registeredWatches ?: emptyList(),
                            selectedWatch = selectedWatch,
                            onWatchSelected = onWatchSelected
                        )
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun WatchPickerList(
        watches: List<Watch>,
        selectedWatch: Watch?,
        onWatchSelected: (Watch) -> Unit
    ) {
        LazyColumn(
            Modifier
                .selectableGroup()
                .fillMaxSize()
        ) {
            items(watches) { watch ->
                ListItem(
                    text = { Text(watch.name) },
                    icon = {
                        RadioButton(
                            selected = watch == selectedWatch,
                            onClick = null
                        )
                    },
                    modifier = Modifier.clickable { onWatchSelected(watch) }
                )
            }
        }
    }
}
