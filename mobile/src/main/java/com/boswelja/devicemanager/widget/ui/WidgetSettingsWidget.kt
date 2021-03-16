package com.boswelja.devicemanager.widget.ui

import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.WidgetWatchBatteryBinding

class WidgetSettingsWidget : Fragment() {

    private val viewModel: WidgetSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Column(Modifier.fillMaxWidth()) {
                    val backgroundVisible by viewModel.widgetBackgroundVisible.observeAsState()
                    val backgroundOpacity by viewModel.widgetBackgroundOpacity.observeAsState()
                    WidgetPreviews(
                        backgroundVisible = backgroundVisible ?: true,
                        backgroundOpacity = backgroundOpacity ?: 60
                    )
                    PreviewIndicator()
                }
            }
        }
    }

    /**
     * Gets a [Drawable] that draws the device's current wallpaper. This will fall back to the
     * system default wallpaper if an error occurs.
     */
    private fun getDeviceWallpaper(): Drawable {
        val wallpaperManager = requireContext().getSystemService<WallpaperManager>()!!
        return wallpaperManager.builtInDrawable
    }

    @Preview(showBackground = true)
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
        backgroundOpacity: Int
    ) {
        AndroidViewBinding(WidgetWatchBatteryBinding::inflate) {
            batteryIndicator.drawable.level = BATTERY_WIDGET_PREVIEW_PERCENT
            batteryIndicatorText.text = getString(
                R.string.battery_sync_percent_short, BATTERY_WIDGET_PREVIEW_PERCENT.toString()
            )
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
        backgroundOpacity: Int
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9)
        ) {
            Image(
                getDeviceWallpaper().toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
            BatteryWidgetPreview(
                backgroundVisible = backgroundVisible,
                backgroundOpacity = backgroundOpacity
            )
        }
    }

    companion object {
        private const val BATTERY_WIDGET_PREVIEW_PERCENT = 50
    }
}
