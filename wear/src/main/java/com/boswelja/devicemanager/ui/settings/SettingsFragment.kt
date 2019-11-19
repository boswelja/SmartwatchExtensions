/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.TwoStatePreference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableRecyclerView
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.interruptfiltersync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.service.PreferenceSyncService
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class SettingsFragment :
        PreferenceFragmentCompat(),
        Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private val preferenceSyncServiceConnection = object : PreferenceSyncService.PreferenceSyncServiceConnection() {
        override fun onPreferenceSyncServiceBound(preferenceSyncService: PreferenceSyncService) {
            this@SettingsFragment.preferenceSyncService = preferenceSyncService
        }

        override fun onPreferenceSyncServiceUnbound() {
            preferenceSyncService = null
        }
    }
    private var preferenceSyncService: PreferenceSyncService? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var messageClient: MessageClient? = null

    private var changingKey = ""

    private val interruptFilterAccessListener = MessageClient.OnMessageReceivedListener {
        if (it.path == REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH) {
            val hasAccess = Boolean.fromByteArray(it.data)
            onInterruptFilterAccessResponse(hasAccess)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            BATTERY_SYNC_ENABLED_KEY,
            BATTERY_PHONE_CHARGE_NOTI_KEY,
            BATTERY_WATCH_CHARGE_NOTI_KEY,
            INTERRUPT_FILTER_SYNC_TO_WATCH_KEY,
            INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
            INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                val newValue = sharedPreferences?.getBoolean(key, false) == true
                findPreference<TwoStatePreference>(key)?.isChecked = newValue
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (val key = preference?.key) {
            BATTERY_SYNC_ENABLED_KEY,
            BATTERY_PHONE_CHARGE_NOTI_KEY,
            BATTERY_WATCH_CHARGE_NOTI_KEY -> {
                val value = newValue == true
                sharedPreferences.edit().putBoolean(key, value).apply()
                preferenceSyncService?.pushNewData(key)
                false
            }
            INTERRUPT_FILTER_SYNC_TO_WATCH_KEY -> {
                val value = newValue == true
                if (value) {
                    val canEnableSync = Utils.checkDnDAccess(context!!)
                    if (canEnableSync) {
                        sharedPreferences.edit().putBoolean(key, value).apply()
                        preferenceSyncService?.pushNewData(key)
                    } else {
                        notifyAdditionalSetupRequired(key)
                    }
                } else {
                    sharedPreferences.edit().putBoolean(key, value).apply()
                    preferenceSyncService?.pushNewData(key)
                }
                false
            }
            INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
            INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                val value = newValue == true
                if (value) {
                    messageClient = Wearable.getMessageClient(preference.context)
                    changingKey = key
                    messageClient!!.addListener(interruptFilterAccessListener)
                    Utils.getCompanionNode(context!!).addOnCompleteListener {
                        if (it.isSuccessful && it.result != null) {
                            val nodes = it.result?.nodes
                            if (!nodes.isNullOrEmpty()) {
                                val node = nodes.first { node -> node.isNearby }
                                messageClient!!.sendMessage(node?.id!!, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
                            } else {
                                notifyError()
                            }
                        } else {
                            notifyError()
                        }
                    }
                } else {
                    sharedPreferences.edit().putBoolean(key, value).apply()
                    preferenceSyncService?.pushNewData(key)
                }
                false
            }
            else -> true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.bindService(Intent(context, PreferenceSyncService::class.java), preferenceSyncServiceConnection, Context.BIND_AUTO_CREATE)
        sharedPreferences = preferenceManager.sharedPreferences
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_battery_sync)
        setupBatterySyncPrefs()

        addPreferencesFromResource(R.xml.prefs_interrupt_filter_sync)
        setupDnDSyncPrefs()
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        return WearableRecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            isEdgeItemsCenteringEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun setupBatterySyncPrefs() {
        findPreference<TwoStatePreference>(BATTERY_SYNC_ENABLED_KEY)!!
                .onPreferenceChangeListener = this

        findPreference<TwoStatePreference>(BATTERY_PHONE_CHARGE_NOTI_KEY)!!
                .onPreferenceChangeListener = this

        findPreference<TwoStatePreference>(BATTERY_WATCH_CHARGE_NOTI_KEY)!!
                .onPreferenceChangeListener = this
    }

    private fun setupDnDSyncPrefs() {
        findPreference<TwoStatePreference>(INTERRUPT_FILTER_SYNC_TO_WATCH_KEY)!!
                .onPreferenceChangeListener = this

        findPreference<TwoStatePreference>(INTERRUPT_FILTER_SYNC_TO_PHONE_KEY)!!
                .onPreferenceChangeListener = this

        findPreference<TwoStatePreference>(INTERRUPT_FILTER_ON_WITH_THEATER_KEY)!!
                .onPreferenceChangeListener = this
    }

    private fun notifyAdditionalSetupRequired(key: String) {
        Utils.launchMobileApp(context!!, key)
        ConfirmationActivityHandler.openOnPhoneAnimation(context!!, getString(R.string.additional_setup_required))
    }

    private fun notifyError() {
        ConfirmationActivityHandler.failAnimation(context!!, getString(R.string.error))
    }

    private fun onInterruptFilterAccessResponse(hasAccess: Boolean) {
        messageClient?.removeListener(interruptFilterAccessListener)
        if (changingKey.isNotEmpty()) {
            if (hasAccess) {
                sharedPreferences.edit().putBoolean(changingKey, hasAccess).apply()
                preferenceSyncService?.pushNewData(changingKey)
            } else {
                notifyAdditionalSetupRequired(changingKey)
            }
        }
    }
}
