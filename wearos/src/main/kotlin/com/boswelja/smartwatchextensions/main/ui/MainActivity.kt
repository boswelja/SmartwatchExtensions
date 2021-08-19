package com.boswelja.smartwatchextensions.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.about.ui.AboutActivity
import com.boswelja.smartwatchextensions.common.startActivity
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.RotaryHandler
import com.boswelja.smartwatchextensions.common.ui.roundScreenPadding
import com.boswelja.smartwatchextensions.extensions.ui.Extensions
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isRegistered by viewModel.isRegistered.collectAsState(true, Dispatchers.IO)

            AppTheme {
                Crossfade(targetState = isRegistered) {
                    if (it) {
                        MainScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .roundScreenPadding()
                        )
                    } else {
                        OnboardingScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    groupPadding: Dp = 8.dp
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    RotaryHandler { delta ->
        coroutineScope.launch {
            scrollState.scrollBy(delta)
        }
    }

    Column(
        modifier = Modifier.verticalScroll(scrollState).then(modifier),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        Extensions(
            contentPadding = groupPadding
        )
        AppInfoChip()
    }
}

@Composable
fun AppInfoChip(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Chip(
        modifier = modifier,
        colors = ChipDefaults.secondaryChipColors(),
        label = {
            Text(stringResource(R.string.about_app_title))
        },
        icon = {
            Icon(
                modifier = Modifier.size(ChipDefaults.IconSize),
                imageVector = Icons.Outlined.Info,
                contentDescription = null
            )
        },
        onClick = {
            context.startActivity<AboutActivity>()
        }
    )
}
