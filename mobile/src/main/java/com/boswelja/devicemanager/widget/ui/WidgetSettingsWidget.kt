/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget.ui

import android.app.WallpaperManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.SettingsWidgetWidgetSettingsBinding

class WidgetSettingsWidget : Fragment() {

    private val viewModel: WidgetSettingsViewModel by activityViewModels()

    private lateinit var binding: SettingsWidgetWidgetSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                getString(
                    R.string.battery_sync_percent_short, BATTERY_WIDGET_PREVIEW_PERCENT.toString()
                )
        }
        viewModel.widgetBackgroundVisible.observe(viewLifecycleOwner) {
            setWidgetBackgroundEnabled(it)
        }
        viewModel.widgetBackgroundOpacity.observe(viewLifecycleOwner) {
            setWidgetBackgroundOpacity(it)
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

    private fun setWidgetBackgroundEnabled(backgroundVisible: Boolean) {
        val widgetBackgroundView =
            binding.widgetContainer.findViewById<AppCompatImageView>(R.id.widget_background)
        if (backgroundVisible) {
            widgetBackgroundView.setImageResource(R.drawable.widget_background)
        } else {
            widgetBackgroundView.setImageDrawable(null)
        }
    }

    private fun setWidgetBackgroundOpacity(backgroundOpacity: Int) {
        val widgetBackgroundView =
            binding.widgetContainer.findViewById<AppCompatImageView>(R.id.widget_background)
        widgetBackgroundView.alpha = backgroundOpacity / 100.0f
    }

    companion object {
        private const val BATTERY_WIDGET_PREVIEW_PERCENT = 50
    }
}
