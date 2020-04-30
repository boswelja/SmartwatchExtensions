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
import androidx.wear.widget.WearableLinearLayoutManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.common.batterysync.References
import com.boswelja.devicemanager.databinding.FragmentMainBinding
import com.boswelja.devicemanager.service.ActionService
import com.boswelja.devicemanager.ui.base.BaseSharedPreferenceFragment
import com.boswelja.devicemanager.ui.common.recyclerview.CustomCurvingLayoutCallback
import com.boswelja.devicemanager.ui.common.recyclerview.CustomLinearSnapHelper
import com.boswelja.devicemanager.ui.main.MainItems.BATTERY_SYNC_ITEM_ID
import com.boswelja.devicemanager.ui.main.MainItems.PHONE_LOCKING_ITEM_ID
import com.boswelja.devicemanager.ui.main.MainItems.SETTINGS_ITEM_ID
import com.boswelja.devicemanager.ui.settings.SettingsActivity

class MainFragment :
        BaseSharedPreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        MainAdapter.ItemCallback {

    private lateinit var binding: FragmentMainBinding
    private lateinit var mainAdapter: MainAdapter

    private var phoneLockingEnabled: Boolean = false
    private var batterySyncEnabled: Boolean = false

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PHONE_LOCKING_ENABLED_KEY -> updatePhoneLockingEnabled()
            BATTERY_SYNC_ENABLED_KEY -> updateBatterySyncEnabled()
        }
    }

    override fun onClick(item: MainItem) {
        when (item.itemId) {
            BATTERY_SYNC_ITEM_ID -> tryUpdateBatteryStats()
            PHONE_LOCKING_ITEM_ID -> tryLockPhone()
            SETTINGS_ITEM_ID -> showSettings()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainAdapter = MainAdapter(this, MainItems.EXTENSIONS, MainItems.APP)
        binding.recyclerView.apply {
            layoutManager = WearableLinearLayoutManager(context, CustomCurvingLayoutCallback(context))
            isEdgeItemsCenteringEnabled = true
            adapter = mainAdapter
        }.also {
            CustomLinearSnapHelper().attachToRecyclerView(it)
        }
    }

    override fun onStart() {
        super.onStart()
        updatePhoneLockingEnabled()
        updateBatterySyncEnabled()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun showSettings() {
        Intent(context, SettingsActivity::class.java).also { intent ->
            context?.startActivity(intent)
        }
    }

    private fun tryUpdateBatteryStats() {
        Intent(context, ActionService::class.java).apply {
            putExtra(ActionService.EXTRA_ACTION, References.REQUEST_BATTERY_UPDATE_PATH)
        }.also {
            context?.startService(it)
        }
    }

    private fun tryLockPhone() {
        Intent(context, ActionService::class.java).apply {
            putExtra(ActionService.EXTRA_ACTION, com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH)
        }.also {
            context?.startService(it)
        }
    }

    private fun updatePhoneLockingEnabled() {
        phoneLockingEnabled = sharedPreferences.getBoolean(PHONE_LOCKING_ENABLED_KEY, false)
        val phoneLockingMainItem = if (phoneLockingEnabled) {
            MainItem(PHONE_LOCKING_ITEM_ID, R.string.lock_phone_label, R.drawable.ic_phone_lock)
        } else {
            MainItem(PHONE_LOCKING_ITEM_ID, R.string.lock_phone_disabled_message, R.drawable.ic_phone_lock)
        }
        mainAdapter.updateItem(phoneLockingMainItem)
    }

    private fun updateBatterySyncEnabled() {
        batterySyncEnabled = sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)
        val batterySyncMainItem = if (batterySyncEnabled) {
            val batteryPercent = sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0)
            MainItem(BATTERY_SYNC_ITEM_ID, R.string.phone_battery_percent, R.drawable.ic_phone_battery, extra = batteryPercent)
        } else {
            MainItem(BATTERY_SYNC_ITEM_ID, R.string.battery_sync_disabled, R.drawable.ic_phone_battery)
        }
        mainAdapter.updateItem(batterySyncMainItem)
    }
}
