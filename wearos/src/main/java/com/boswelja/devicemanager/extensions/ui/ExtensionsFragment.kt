package com.boswelja.devicemanager.extensions.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.about.ui.AboutActivity
import com.boswelja.devicemanager.batterysync.ui.BatterySyncScreen
import com.boswelja.devicemanager.common.AppTheme
import com.boswelja.devicemanager.phonelocking.ui.PhoneLockingScreen

class ExtensionsFragment : Fragment() {
    @ExperimentalMaterialApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme {
                    ExtensionsScreen()
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun ExtensionsScreen() {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .verticalScroll(scrollState)
            .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 64.dp)
    ) {
        Extensions()
        Divider()
        Links()
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
