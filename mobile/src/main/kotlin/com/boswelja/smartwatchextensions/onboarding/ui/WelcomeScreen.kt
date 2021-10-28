package com.boswelja.smartwatchextensions.onboarding.ui

import android.util.TypedValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
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
 * @param onNavigateNext Called when navigation is requested.
 */
@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    onNavigateNext: () -> Unit
) {
    Column(
        modifier.padding(contentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val appImageSizeDp = 180.dp
        Spacer(Modifier.weight(1f))
        Image(
            painterAppIcon(size = appImageSizeDp),
            null,
            Modifier.size(appImageSizeDp)
        )
        Spacer(Modifier.height(contentPadding))
        Text(
            stringResource(R.string.welcome_to_text),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            stringResource(R.string.app_name),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.weight(2f))
        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.get_started)) },
            icon = { Icon(Icons.Outlined.NavigateNext, null) },
            onClick = onNavigateNext
        )
        Spacer(Modifier.weight(1f))
    }
}

/**
 * Get an [ImageBitmap] for the app icon.
 * @param size The target icon size (width and height).
 * @return The [ImageBitmap] for the app icon.
 */
@Composable
fun painterAppIcon(
    size: Dp
): ImageBitmap {
    val context = LocalContext.current
    val appImageSizePx = remember(size) {
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            size.value,
            context.resources.displayMetrics
        ).toInt()
    }
    val drawable = remember(appImageSizePx, context) {
        ContextCompat.getDrawable(context, R.mipmap.ic_launcher)!!.toBitmap(
            width = appImageSizePx,
            height = appImageSizePx
        ).asImageBitmap()
    }
    return drawable
}
