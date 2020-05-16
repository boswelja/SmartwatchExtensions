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
import androidx.annotation.VisibleForTesting
import androidx.wear.widget.CurvingLayoutCallback
import androidx.wear.widget.WearableLinearLayoutManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PERCENT_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.PHONE_LOCKING_ENABLED_KEY
import com.boswelja.devicemanager.common.batterysync.References
import com.boswelja.devicemanager.common.recyclerview.adapter.SectionedAdapter.Companion.SECTION_HEADER_HIDDEN
import com.boswelja.devicemanager.databinding.FragmentMainBinding
import com.boswelja.devicemanager.service.ActionService
import com.boswelja.devicemanager.ui.about.AboutActivity
import com.boswelja.devicemanager.ui.base.BaseSharedPreferenceFragment
import com.boswelja.devicemanager.ui.main.MainItems.ABOUT_APP_ITEM_ID
import com.boswelja.devicemanager.ui.main.MainItems.APP
import com.boswelja.devicemanager.ui.main.MainItems.BATTERY_SYNC_ITEM_ID
import com.boswelja.devicemanager.ui.main.MainItems.EXTENSIONS
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
            ABOUT_APP_ITEM_ID -> showAbout()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val extensionsSection = Pair(SECTION_HEADER_HIDDEN, EXTENSIONS)
        val appSection = Pair(getString(R.string.section_text_app_info), APP)
        mainAdapter = MainAdapter(this, arrayListOf(extensionsSection, appSection))
        binding.recyclerView.apply {
            layoutManager = WearableLinearLayoutManager(context, CurvingLayoutCallback(context))
            isEdgeItemsCenteringEnabled = true
            adapter = mainAdapter
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
        Intent(context, SettingsActivity::class.java).also {
            startActivity(it)
        }
    }

    private fun showAbout() {
        Intent(context, AboutActivity::class.java).also {
            startActivity(it)
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
        updatePhoneLockingView(phoneLockingEnabled)
    }

    private fun updateBatterySyncEnabled() {
        batterySyncEnabled = sharedPreferences.getBoolean(BATTERY_SYNC_ENABLED_KEY, false)
        val batteryPercent = sharedPreferences.getInt(BATTERY_PERCENT_KEY, 0)
        updateBatterySyncView(batterySyncEnabled, batteryPercent)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateBatterySyncView(batterySyncEnabled: Boolean, batteryPercent: Int = 0) {
        val batterySyncMainItem = if (batterySyncEnabled) {
            MainItem(BATTERY_SYNC_ITEM_ID, R.string.phone_battery_percent, R.drawable.ic_phone_battery, extra = batteryPercent)
        } else {
            EXTENSIONS.first { it.itemId == BATTERY_SYNC_ITEM_ID }
        }
        mainAdapter.updateItem(batterySyncMainItem)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updatePhoneLockingView(phoneLockingEnabled: Boolean) {
        val phoneLockingMainItem = if (phoneLockingEnabled) {
            MainItem(PHONE_LOCKING_ITEM_ID, R.string.lock_phone_label, R.drawable.ic_phone_lock)
        } else {
            EXTENSIONS.first { it.itemId == PHONE_LOCKING_ITEM_ID }
        }
        mainAdapter.updateItem(phoneLockingMainItem)
    }
}
