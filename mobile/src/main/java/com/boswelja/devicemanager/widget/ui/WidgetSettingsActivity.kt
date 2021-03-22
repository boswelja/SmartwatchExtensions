package com.boswelja.devicemanager.widget.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.batterysync.widget.WatchBatteryWidget
import com.boswelja.devicemanager.common.ui.AppTheme
import com.boswelja.devicemanager.common.ui.UpNavigationAppBar

class WidgetSettingsActivity : AppCompatActivity() {

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Scaffold(
                    topBar = {
                        UpNavigationAppBar(
                            onNavigateUp = { finish() },
                            title = { Text(stringResource(R.string.widget_settings_title)) }
                        )
                    }
                ) {
                    Column(Modifier.fillMaxSize()) {
                        WidgetSettingsHeader()
                        Divider()
                        WidgetSettingsScreen()
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        WatchBatteryWidget.updateWidgets(this)
    }
}
