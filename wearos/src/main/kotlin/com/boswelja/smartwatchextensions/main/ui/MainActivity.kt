package com.boswelja.smartwatchextensions.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissValue
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.about.ui.AboutScreen
import com.boswelja.smartwatchextensions.common.ui.AppTheme
import com.boswelja.smartwatchextensions.common.ui.RotaryHandler
import com.boswelja.smartwatchextensions.common.ui.roundScreenPadding
import com.boswelja.smartwatchextensions.extensions.ui.Extensions
import com.boswelja.smartwatchextensions.onboarding.ui.OnboardingScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 * The main entry point of the app.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = getViewModel()
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

/**
 * The main Composable of the app.
 * @param modifier [Modifier].
 * @param contentPadding The padding around the content.
 * @param groupPadding The padding between groups.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    groupPadding: Dp = 8.dp
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var aboutVisible by remember {
        mutableStateOf(false)
    }

    RotaryHandler { delta ->
        coroutineScope.launch {
            scrollState.scrollBy(delta)
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(contentPadding)
    ) {
        Extensions(
            contentPadding = groupPadding
        )
        AppInfoChip(
            onAboutClick = {
                aboutVisible = true
            }
        )
    }

    AnimatedVisibility(
        visible = aboutVisible,
        enter = fadeIn(),
        exit = ExitTransition.None
    ) {
        val aboutBoxState = rememberSwipeToDismissBoxState(
            confirmStateChange = {
                if (it == SwipeToDismissValue.Dismissed) {
                    aboutVisible = false
                }
                true
            }
        )
        SwipeToDismissBox(state = aboutBoxState) {
            AboutScreen(modifier = Modifier.background(MaterialTheme.colors.background))
        }
    }
}

/**
 * A [Chip] for displaying app info.
 * @param modifier [Modifier].
 * @param onAboutClick Called when the chip is clicked.
 */
@Composable
fun AppInfoChip(
    modifier: Modifier = Modifier,
    onAboutClick: () -> Unit
) {
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
        onClick = onAboutClick
    )
}
