package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R

@Composable
fun OnboardingScreen() {
    LazyColumn {
        item {
            WelcomeScreen()
        }
        item {
            PhoneInstallInstructions()
        }
        item {
            PhoneSetupInstructions()
        }
    }
}

@Composable
fun WelcomeScreen() {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            context.packageManager.getApplicationIcon(context.packageName)
                .toBitmap().asImageBitmap(),
            null,
            Modifier.size(48.dp)
        )
        Text(
            stringResource(R.string.welcome_to_text),
            style = MaterialTheme.typography.body2
        )
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        DownArrow()
    }
}

@Composable
fun PhoneInstallInstructions() {
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.setup_phone_helper_text),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        DownArrow()
    }
}

@Composable
fun PhoneSetupInstructions() {
    val viewModel: OnboardingViewModel = viewModel()
    val watchName by viewModel.setupNameText.observeAsState()
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.setup_instructions_text),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
        Text(
            watchName ?: "Error",
            style = MaterialTheme.typography.h6
        )
        Text(
            stringResource(R.string.setup_hint_text),
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DownArrow() {
    Icon(
        Icons.Outlined.ArrowDownward,
        stringResource(R.string.content_description_scroll_down),
        Modifier.padding(8.dp).size(24.dp),
        tint = Color.White
    )
}
