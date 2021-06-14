package com.boswelja.smartwatchextensions.extensions.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.about.ui.AboutActivity
import com.boswelja.smartwatchextensions.batterysync.ui.BatterySyncScreen
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.phonelocking.ui.PhoneLockingScreen
import kotlinx.coroutines.Dispatchers

@ExperimentalMaterialApi
@Composable
fun ExtensionsScreen() {
    val scrollState = rememberScrollState()
    ColumnInsetLayout(scrollState) {
        Extensions()
        Divider(Modifier.padding(vertical = 8.dp))
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
                .padding(horizontal = 8.dp, vertical = 56.dp),
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
        val batterySyncEnabled by viewModel.batterySyncEnabled.collectAsState(false, Dispatchers.IO)
        val batteryPercent by viewModel.batteryPercent.collectAsState(0, Dispatchers.IO)
        val phoneName by viewModel.phoneName
            .collectAsState(stringResource(R.string.default_phone_name), Dispatchers.IO)
        BatterySyncScreen(
            batterySyncEnabled = batterySyncEnabled,
            batteryPercent = batteryPercent,
            phoneName = phoneName
        ) {
            viewModel.updateBatteryStats()
        }
        val phoneLockingEnabled by viewModel.phoneLockingEnabled
            .collectAsState(false, Dispatchers.IO)
        PhoneLockingScreen(
            phoneLockingEnabled = phoneLockingEnabled,
            phoneName = phoneName
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
                context.startActivity<AboutActivity>()
            }
        )
    }
}
