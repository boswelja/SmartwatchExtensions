package com.boswelja.smartwatchextensions.widget.ui

import android.app.WallpaperManager
import android.view.LayoutInflater
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import com.boswelja.smartwatchextensions.R
import com.boswelja.smartwatchextensions.databinding.CommonWidgetBackgroundBinding
import com.boswelja.smartwatchextensions.databinding.WidgetWatchBatteryBinding
import kotlinx.coroutines.Dispatchers

@Composable
fun WidgetSettingsHeader() {
    Column(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9)
    ) {
        val viewModel: WidgetSettingsViewModel = viewModel()
        val backgroundVisible by viewModel.widgetBackgroundVisible
            .collectAsState(true, Dispatchers.IO)
        val backgroundOpacity by viewModel.widgetBackgroundOpacity
            .collectAsState(60, Dispatchers.IO)
        WidgetPreviews(
            backgroundVisible = backgroundVisible ?: true,
            backgroundOpacity = backgroundOpacity ?: 60,
            modifier = Modifier.weight(1f)
        )
        PreviewIndicator()
    }
}

@Composable
fun PreviewIndicator() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Info, null)
        Text(
            stringResource(R.string.widget_preview_info),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun BatteryWidgetPreview(
    backgroundVisible: Boolean,
    backgroundOpacity: Int,
    modifier: Modifier = Modifier
) {
    val previewPercent = 50
    AndroidViewBinding(
        CommonWidgetBackgroundBinding::inflate,
        modifier = modifier.size(width = 100.dp, height = 150.dp)
    ) {
        val batteryWidgetContent = WidgetWatchBatteryBinding
            .inflate(LayoutInflater.from(root.context)).apply {
                batteryIndicator.drawable.level = previewPercent
                batteryIndicatorText.text = root.context.getString(
                    R.string.battery_percent, previewPercent.toString()
                )
            }
        widgetContainer.addView(batteryWidgetContent.root)
        if (backgroundVisible) {
            widgetBackground.setImageResource(R.drawable.widget_background)
            widgetBackground.alpha = backgroundOpacity / 100f
        } else {
            widgetBackground.setImageDrawable(null)
        }
    }
}

@Composable
fun WidgetPreviews(
    backgroundVisible: Boolean,
    backgroundOpacity: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        val deviceWallpaper = LocalContext.current.getSystemService<WallpaperManager>()
            ?.builtInDrawable
        deviceWallpaper?.let {
            Image(
                it.toBitmap().asImageBitmap(),
                contentScale = ContentScale.FillBounds,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        BatteryWidgetPreview(
            backgroundVisible = backgroundVisible,
            backgroundOpacity = backgroundOpacity,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
