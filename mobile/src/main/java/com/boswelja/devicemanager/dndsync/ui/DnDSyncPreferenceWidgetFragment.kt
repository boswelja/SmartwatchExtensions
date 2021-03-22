package com.boswelja.devicemanager.dndsync.ui

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.devicemanager.R

@Composable
fun DnDSyncSettingsHeader() {
    val viewModel: DnDSyncPreferenceViewModel = viewModel()
    val iconRes by viewModel.drawableRes.observeAsState()
    if (LocalConfiguration.current.orientation == ORIENTATION_PORTRAIT) {
        Row(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                ContextCompat
                    .getDrawable(LocalContext.current, R.drawable.ic_dnd_phone)!!
                    .toBitmap().asImageBitmap(),
                null,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                tint = MaterialTheme.colors.onBackground
            )
            iconRes?.let {
                Icon(
                    painterResource(it),
                    null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colors.onBackground
                )
            }
            Icon(
                ContextCompat
                    .getDrawable(LocalContext.current, R.drawable.ic_dnd_watch)!!
                    .toBitmap().asImageBitmap(),
                null,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                tint = MaterialTheme.colors.onBackground
            )
        }
    } else {
        Column(
            Modifier
                .fillMaxHeight()
                .aspectRatio(0.5f)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                ContextCompat
                    .getDrawable(LocalContext.current, R.drawable.ic_dnd_phone)!!
                    .toBitmap().asImageBitmap(),
                null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                tint = MaterialTheme.colors.onBackground
            )
            iconRes?.let {
                Icon(
                    painterResource(it),
                    null,
                    modifier = Modifier.size(40.dp).rotate(90f),
                    tint = MaterialTheme.colors.onBackground
                )
            }
            Icon(
                ContextCompat
                    .getDrawable(LocalContext.current, R.drawable.ic_dnd_watch)!!
                    .toBitmap().asImageBitmap(),
                null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                tint = MaterialTheme.colors.onBackground
            )
        }
    }
}
