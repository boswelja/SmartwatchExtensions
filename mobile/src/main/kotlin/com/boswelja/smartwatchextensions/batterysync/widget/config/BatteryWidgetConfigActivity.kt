package com.boswelja.smartwatchextensions.batterysync.widget.config

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.BaseWidgetConfigActivity
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.smartwatchextensions.core.ui.theme.HarmonizedTheme
import com.boswelja.watchconnection.common.Watch
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 * A [BaseWidgetConfigActivity] for configuring a Battery Sync widget.
 */
class BatteryWidgetConfigActivity : BaseWidgetConfigActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val (selectedWatch, onWatchSelected) = remember { mutableStateOf<Watch?>(null) }
            HarmonizedTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(onNavigateUp = { finish() })
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_finish)) },
                            icon = { Icon(Icons.Default.Check, null) },
                            onClick = {
                                selectedWatch?.let {
                                    finishWidgetConfig(it)
                                }
                            }
                        )
                    }
                ) {
                    Column(Modifier.fillMaxSize()) {
                        val viewModel: BatteryWidgetConfigViewModel = getViewModel()
                        val registeredWatches by viewModel.registeredWatches.collectAsState(
                            emptyList()
                        )
                        Text(
                            stringResource(R.string.widget_config_battery_stats_hint),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                        WatchPickerList(
                            watches = registeredWatches,
                            selectedWatch = selectedWatch,
                            onWatchSelected = onWatchSelected
                        )
                    }
                }
            }
        }
    }

    /**
     * Displays a column of watches and allows the user to select a watch.
     * @param watches The list of available watches.
     * @param selectedWatch The currently selected watch.
     * @param onWatchSelected Called when a new watch is selected.
     */
    @OptIn(ExperimentalMaterial3Api::class)
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
                Row(Modifier.clickable { onWatchSelected(watch) }) {
                    RadioButton(selected = watch == selectedWatch, onClick = null)
                    Text(watch.name)
                }
            }
        }
    }
}
