package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.boswelja.smartwatchextensions.R

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
    Column(
        modifier.padding(contentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        Image(
            ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!.toBitmap().asImageBitmap(),
            null,
            Modifier.size(180.dp)
        )
        Spacer(Modifier.height(contentPadding))
        Text(
            stringResource(R.string.welcome_to_text),
            style = MaterialTheme.typography.h5
        )
        Text(
            stringResource(R.string.app_name),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4
        )
        Spacer(Modifier.height(contentPadding))
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.get_started)) },
            icon = { Icon(Icons.Outlined.NavigateNext, null) },
            onClick = { onNavigateTo(OnboardingDestination.REGISTER_WATCHES) }
        )
    }
}
