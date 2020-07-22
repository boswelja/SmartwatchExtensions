/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.widget

import android.app.WallpaperManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.SettingsWidgetWidgetSettingsBinding

class WidgetSettingsWidget : Fragment() {

    private lateinit var binding: SettingsWidgetWidgetSettingsBinding

    private var backgroundVisible: Boolean = false
    private var backgroundOpacity: Int = 60

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SettingsWidgetWidgetSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.widgetContainer.apply {
            background = getDeviceWallpaper()
            findViewById<AppCompatImageView>(R.id.battery_indicator).apply {
                setImageResource(R.drawable.ic_watch_battery)
                setImageLevel(BATTERY_WIDGET_PREVIEW_PERCENT)
            }
            findViewById<TextView>(R.id.battery_indicator_text).text =
                    getString(R.string.battery_sync_percent_short, BATTERY_WIDGET_PREVIEW_PERCENT.toString())
        }
        updateWidgetBackground()
    }

    /**
     * Gets a [Drawable] that draws the device's current wallpaper.
     * This will fall back to the system default wallpaper if an error occurs.
     */
    private fun getDeviceWallpaper(): Drawable {
        val wallpaperManager = context?.getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
        return wallpaperManager.builtInDrawable
    }

    /**
     * Updates the widget background based on user preferences.
     */
    private fun updateWidgetBackground() {
        val widgetBackgroundView =
                binding.widgetContainer.findViewById<AppCompatImageView>(R.id.widget_background)
        if (backgroundVisible) {
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.widget_background)
            if (drawable != null) {
                val calculatedAlpha = ((backgroundOpacity / 100.0f) * 255).toInt()
                drawable.alpha = calculatedAlpha
                widgetBackgroundView.setImageDrawable(drawable)
            }
        } else {
            widgetBackgroundView.setImageDrawable(null)
        }
    }

    fun setWidgetBackgroundEnabled(backgroundVisible: Boolean) {
        if (backgroundVisible != this.backgroundVisible) {
            this.backgroundVisible = backgroundVisible
            updateWidgetBackground()
        }
    }

    fun setWidgetBackgroundOpacity(backgroundOpacity: Int) {
        if (backgroundOpacity != this.backgroundOpacity) {
            this.backgroundOpacity = backgroundOpacity
            updateWidgetBackground()
        }
    }

    companion object {
        private const val BATTERY_WIDGET_PREVIEW_PERCENT = 50
    }
}
