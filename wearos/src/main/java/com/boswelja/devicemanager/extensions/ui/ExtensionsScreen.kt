package com.boswelja.devicemanager.extensions.ui

import android.content.Intent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.about.ui.AboutActivity
import com.boswelja.devicemanager.batterysync.ui.BatterySyncScreen
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingScreen

@ExperimentalMaterialApi
@Composable
fun ExtensionsScreen() {
    val scrollState = rememberScrollState()
    ColumnInsetLayout(scrollState) {
        Extensions()
        Divider()
        Links()
    }
}

@Composable
fun ColumnInsetLayout(
    scrollState: ScrollState,
    content: @Composable ColumnScope.() -> Unit
) {
    if (LocalContext.current.resources.configuration.isScreenRound) {
        Column(
            Modifier
                .verticalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 64.dp),
            content = content
        )
    } else {
        Column(
            Modifier
                .verticalScroll(scrollState)
                .padding(8.dp),
            content = content
        )
    }
}
@ExperimentalMaterialApi
@Composable
fun Extensions() {
    Column {
        val viewModel: ExtensionsViewModel = viewModel()
        val batterySyncEnabled by viewModel.batterySyncEnabled.observeAsState()
        val batteryPercent by viewModel.batteryPercent.observeAsState()
        val phoneName by viewModel.phoneName.observeAsState()
        BatterySyncScreen(
            batterySyncEnabled = batterySyncEnabled == true,
            batteryPercent = batteryPercent ?: 0,
            phoneName = phoneName ?: stringResource(R.string.default_phone_name)
        ) {
            viewModel.updateBatteryStats()
        }
        val phoneLockingEnabled by viewModel.phoneLockingEnabled.observeAsState()
        PhoneLockingScreen(
            phoneLockingEnabled = phoneLockingEnabled == true,
            phoneName = phoneName ?: stringResource(R.string.default_phone_name)
        ) {
            viewModel.requestLockPhone()
        }
    }
}

@Composable
fun Links() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            content = {
                Icon(Icons.Outlined.Info, null)
                Text(stringResource(R.string.about_app_title))
            },
            onClick = {
                context.startActivity(Intent(context, AboutActivity::class.java))
            }
        )
    }
}
