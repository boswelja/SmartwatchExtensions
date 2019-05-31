/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

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
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncLayer
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class SettingsFragment :
        PreferenceFragmentCompat(),
        Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var prefs: SharedPreferences
    private lateinit var preferenceSyncLayer: PreferenceSyncLayer
    private lateinit var messageClient: MessageClient

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
            BATTERY_PHONE_FULL_CHARGE_NOTI_KEY,
            BATTERY_WATCH_FULL_CHARGE_NOTI_KEY,
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
            BATTERY_PHONE_FULL_CHARGE_NOTI_KEY,
            BATTERY_WATCH_FULL_CHARGE_NOTI_KEY -> {
                val value = newValue == true
                prefs.edit().putBoolean(key, value).apply()
                preferenceSyncLayer.pushNewData()
                false
            }
            INTERRUPT_FILTER_SYNC_TO_WATCH_KEY -> {
                val value = newValue == true
                if (value) {
                    val canEnableSync = Utils.checkDnDAccess(context!!)
                    if (canEnableSync) {
                        prefs.edit().putBoolean(key, value).apply()
                        preferenceSyncLayer.pushNewData()
                    } else {
                        notifyAdditionalSetupRequired(key)
                    }
                } else {
                    prefs.edit().putBoolean(key, value).apply()
                    preferenceSyncLayer.pushNewData()
                }
                false
            }
            INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
            INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                val value = newValue == true
                if (value) {
                    changingKey = key
                    messageClient.addListener(interruptFilterAccessListener)
                    Utils.getCompanionNode(context!!).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val nodes = it.result?.nodes
                            if (!nodes.isNullOrEmpty()) {
                                val node = nodes.first { node -> node.isNearby }
                                messageClient.sendMessage(node?.id!!, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
                            } else {
                                notifyError()
                            }
                        } else {
                            notifyError()
                        }
                    }
                } else {
                    prefs.edit().putBoolean(key, value).apply()
                    preferenceSyncLayer.pushNewData()
                }
                false
            }
            else -> true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceSyncLayer = PreferenceSyncLayer(context!!)
        prefs = preferenceManager.sharedPreferences
        messageClient = Wearable.getMessageClient(context!!)
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
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    private fun setupBatterySyncPrefs() {
        findPreference<TwoStatePreference>(BATTERY_SYNC_ENABLED_KEY)!!
                .onPreferenceChangeListener = this

        findPreference<TwoStatePreference>(BATTERY_PHONE_FULL_CHARGE_NOTI_KEY)!!
                .onPreferenceChangeListener = this

        findPreference<TwoStatePreference>(BATTERY_WATCH_FULL_CHARGE_NOTI_KEY)!!
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
        messageClient.removeListener(interruptFilterAccessListener)
        if (hasAccess) {
            prefs.edit().putBoolean(changingKey, hasAccess).apply()
            preferenceSyncLayer.pushNewData()
        } else {
            notifyAdditionalSetupRequired(changingKey)
        }
    }
}
