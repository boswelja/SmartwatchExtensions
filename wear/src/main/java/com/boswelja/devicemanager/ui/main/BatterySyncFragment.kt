/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.batterysync.References
import com.boswelja.devicemanager.databinding.FragmentBatterySyncBinding
import com.boswelja.devicemanager.service.ActionService
import com.boswelja.devicemanager.ui.base.BaseSharedPreferenceFragment

class BatterySyncFragment : BaseSharedPreferenceFragment() {

    private val batteryInfoChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            BATTERY_SYNC_ENABLED_KEY,
            BATTERY_PERCENT_KEY -> {
                updatePhoneBatteryInfo()
            }
        }
    }

    private lateinit var binding: FragmentBatterySyncBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBatterySyncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnClickListener {
            val intent = Intent(context, ActionService::class.java)
            intent.putExtra(ActionService.EXTRA_ACTION, References.REQUEST_BATTERY_UPDATE_PATH)
            context?.startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updatePhoneBatteryInfo()
        sharedPreferences.registerOnSharedPreferenceChangeListener(batteryInfoChangeListener)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(batteryInfoChangeListener)
    }

    private fun updatePhoneBatteryInfo() {
        val batterySyncEnabled = sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)
        val batteryPercent = sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0)
        binding.apply {
            phoneBatteryIndicatorIcon.drawable.level = batteryPercent
            if (batterySyncEnabled) {
                phoneBatteryIndicatorTextView.text = getString(R.string.phone_battery_percent).format(batteryPercent)
            } else {
                phoneBatteryIndicatorTextView.text = getString(R.string.battery_sync_disabled)
            }
        }
    }
}
