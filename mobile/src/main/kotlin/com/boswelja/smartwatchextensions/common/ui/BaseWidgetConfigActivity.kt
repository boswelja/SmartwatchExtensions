package com.boswelja.smartwatchextensions.common.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.WatchWidgetProvider
import com.boswelja.smartwatchextensions.widget.widgetIdStore
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * An activity containing common code for widget configuration activities
 */
abstract class BaseWidgetConfigActivity : AppCompatActivity() {

    private val resultIntent = Intent()

    /**
     * The ID of the widget we're configuring
     */
    private var widgetId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the ID of the widget we're configuring
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_CANCELED, resultIntent)
    }

    /**
     * Perform finishing touches for the widget config
     * @param watch The [Watch] to configure the widget for.
     */
    internal fun finishWidgetConfig(watch: Watch) {
        Timber.d("finishWidgetConfig($watch) called")
        setResult(RESULT_OK, resultIntent)

        lifecycleScope.launch(Dispatchers.IO) {
            widgetIdStore.edit { widgetIds ->
                widgetIds[stringPreferencesKey(widgetId.toString())] = watch.id.toString()
            }
            WatchWidgetProvider.updateWidgets(
                this@BaseWidgetConfigActivity,
                intArrayOf(widgetId)
            )
            finish()
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun WatchWidgetConfigScreen(
    modifier: Modifier = Modifier,
    availableWatches: List<Watch>,
    selectedWatch: Watch?,
    onWatchSelected: (Watch) -> Unit,
) {
    Column(modifier) {
        Text(
            stringResource(R.string.widget_config_battery_stats_hint),
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        WatchPickerList(
            watches = availableWatches,
            selectedWatch = selectedWatch,
            onWatchSelected = onWatchSelected
        )
    }
}

@ExperimentalMaterialApi
@Composable
fun WatchPickerList(
    modifier: Modifier = Modifier,
    watches: List<Watch>,
    selectedWatch: Watch?,
    onWatchSelected: (Watch) -> Unit
) {
    LazyColumn(
        modifier.selectableGroup()
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
