package com.boswelja.smartwatchextensions.onboarding.ui

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.smartwatchextensions.onboarding.R
import com.boswelja.smartwatchextensions.onboarding.navigation.OnboardingDestination

/**
 * A Composable screen for displaying a greeting to the user.
 * @param modifier [Modifier].
 * @param contentPadding The screen padding.
 * @param onNavigateTo Called when navigation is requested.
 */
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onNavigateTo: (OnboardingDestination) -> Unit
) {
    val context = LocalContext.current
    val appName = remember(context) {
        context.packageManager.getApplicationLabel(
            context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.ApplicationInfoFlags.of(0L)
            )
        ).toString()
    }
    val appIcon = remember(context) {
        context.packageManager.getApplicationIcon(context.packageName).toBitmap().asImageBitmap()
    }

    Column(
        modifier.padding(contentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            appIcon,
            null,
            Modifier.size(180.dp)
        )
        Spacer(Modifier.height(contentPadding))
        Text(
            stringResource(R.string.welcome_to_text),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = appName,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(contentPadding))
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.get_started)) },
            icon = { Icon(Icons.Default.NavigateNext, null) },
            onClick = { onNavigateTo(OnboardingDestination.REGISTER_WATCHES) }
        )
    }
}
