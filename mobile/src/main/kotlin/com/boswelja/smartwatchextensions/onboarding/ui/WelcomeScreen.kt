package com.boswelja.smartwatchextensions.onboarding.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp
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
        Text(
            stringResource(R.string.welcome_to_text),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(top = contentPadding)
        )
        Text(
            stringResource(R.string.app_name),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4
        )
    }
}
