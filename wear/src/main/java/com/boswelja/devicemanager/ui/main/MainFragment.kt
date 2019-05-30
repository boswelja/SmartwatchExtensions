/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.batterysync.BatterySyncReferences
import com.boswelja.devicemanager.service.ActionService

class MainFragment : Fragment() {

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            BATTERY_SYNC_ENABLED_KEY,
            BATTERY_PERCENT_KEY -> {
                updatePhoneBatteryInfo()
            }
            PHONE_LOCKING_ENABLED_KEY -> updatePhoneLockingView()
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var phoneBatteryProgressBar: ProgressBar
    private lateinit var phoneBatteryIndicatorIconView: AppCompatImageView
    private lateinit var phoneBatteryIndicatorTextView: AppCompatTextView
    private lateinit var phoneBatteryTapToRefresh: AppCompatTextView

    private lateinit var phoneLockingLabelView: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPhoneBatteryInfo(view)
        setupPhoneLockingView(view)
    }

    override fun onResume() {
        super.onResume()
        updatePhoneBatteryInfo()
        updatePhoneLockingView()
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun setupPhoneBatteryInfo(view: View) {
        phoneBatteryProgressBar = view.findViewById(R.id.phone_battery_progress_bar)
        phoneBatteryIndicatorIconView = view.findViewById(R.id.phone_battery_indicator_icon)
        phoneBatteryIndicatorTextView = view.findViewById(R.id.phone_battery_indicator_text_view)
        phoneBatteryTapToRefresh = view.findViewById(R.id.phone_battery_indicator_tap_to_refresh_label)
        view.findViewById<RelativeLayout>(R.id.phone_battery_view).setOnClickListener {
            val intent = Intent(context, ActionService::class.java)
            intent.putExtra(ActionService.EXTRA_ACTION, BatterySyncReferences.REQUEST_BATTERY_UPDATE_PATH)
            context?.startService(intent)
        }
    }

    private fun setupPhoneLockingView(view: View) {
        phoneLockingLabelView = view.findViewById(R.id.phone_locking_text)
        view.findViewById<RelativeLayout>(R.id.phone_locking_view).setOnClickListener {
            val intent = Intent(context, ActionService::class.java)
            intent.putExtra(ActionService.EXTRA_ACTION, References.LOCK_PHONE_PATH)
            context?.startService(intent)
        }
    }

    private fun updatePhoneBatteryInfo() {
        val batterySyncEnabled = sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)
        if (batterySyncEnabled) {
            val batteryPercent = sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0)
            phoneBatteryProgressBar.progress = batteryPercent
            phoneBatteryIndicatorIconView.drawable.level = batteryPercent
            phoneBatteryIndicatorTextView.text = getString(R.string.phone_battery_percent).format(batteryPercent)
            phoneBatteryTapToRefresh.visibility = View.VISIBLE
        } else {
            phoneBatteryProgressBar.progress = 0
            phoneBatteryIndicatorIconView.drawable.level = 0
            phoneBatteryIndicatorTextView.text = getString(R.string.battery_sync_disabled)
            phoneBatteryTapToRefresh.visibility = View.GONE
        }
    }

    private fun updatePhoneLockingView() {
        val phoneLockingEnabled = sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false)
        phoneLockingLabelView.text = if (phoneLockingEnabled) {
            getString(R.string.lock_phone_label)
        } else {
            getString(R.string.phone_lock_disabled_message)
        }
    }
}
