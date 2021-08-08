package com.boswelja.smartwatchextensions.watchmanager.ui.register

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar

class RegisterWatchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: RegisterWatchViewModel = viewModel()
            AppTheme {
                Scaffold(
                    topBar = { UpNavigationAppBar(onNavigateUp = { finish() }) },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = { Text(stringResource(R.string.button_finish)) },
                            icon = { Icon(Icons.Outlined.Done, null) },
                            onClick = { finish() }
                        )
                    }
                ) {
                    RegisterWatchScreen(registeredWatches = viewModel.addedWatches)
                }
            }
        }
    }
}
