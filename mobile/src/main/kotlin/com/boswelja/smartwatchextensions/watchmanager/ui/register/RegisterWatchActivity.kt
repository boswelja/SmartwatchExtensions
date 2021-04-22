package com.boswelja.smartwatchextensions.watchmanager.ui.register

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.UpNavigationAppBar
import com.boswelja.watchconnection.core.Watch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RegisterWatchActivity : AppCompatActivity() {

    @ExperimentalCoroutinesApi
    private val viewModel: RegisterWatchViewModel by viewModels()

    @ExperimentalCoroutinesApi
    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val registeredWatches = mutableStateListOf<Watch>()

        setContent {
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
                    RegisterWatchScreen(registeredWatches = registeredWatches)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.registeredWatches.collect {
                registeredWatches.add(it)
            }
        }
    }
}
