/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.settings

import android.content.Context
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
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.Extensions.fromByteArray
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_PHONE_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_SYNC_ENABLED_KEY
import com.boswelja.devicemanager.common.PreferenceKey.BATTERY_WATCH_CHARGE_NOTI_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.dndsync.References.REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH
import com.boswelja.devicemanager.phoneconnectionmanager.References.PHONE_ID_KEY
import com.boswelja.devicemanager.preferencesync.PreferenceSyncHelper
import com.boswelja.devicemanager.ui.ConfirmationActivityHandler
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

@Suppress("unused")
class SettingsFragment :
        PreferenceFragmentCompat(),
        Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var preferenceSyncHelper: PreferenceSyncHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var messageClient: MessageClient

    private var phoneId: String = ""
    private var changingKey = ""
    private var interruptFilterAccessListenerRegistered: Boolean = false

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
            DND_SYNC_TO_WATCH_KEY,
            DND_SYNC_TO_PHONE_KEY,
            DND_SYNC_WITH_THEATER_KEY
            -> {
                val newValue = sharedPreferences?.getBoolean(key, false) == true
                findPreference<TwoStatePreference>(key)?.isChecked = newValue
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (val key = preference?.key) {
            BATTERY_SYNC_ENABLED_KEY,
            BATTERY_PHONE_CHARGE_NOTI_KEY,
            BATTERY_WATCH_CHARGE_NOTI_KEY
            -> {
                val value = newValue == true
                sharedPreferences.edit().putBoolean(key, value).apply()
                preferenceSyncHelper.pushNewData(key)
                false
            }
            DND_SYNC_TO_WATCH_KEY -> {
                val value = newValue == true
                if (value) {
                    val canEnableSync = Compat.canSetDnD(requireContext())
                    if (canEnableSync) {
                        sharedPreferences.edit().putBoolean(key, value).apply()
                        preferenceSyncHelper.pushNewData(key)
                    } else {
                        notifyAdditionalSetupRequired(key)
                    }
                } else {
                    sharedPreferences.edit().putBoolean(key, value).apply()
                    preferenceSyncHelper.pushNewData(key)
                }
                false
            }
            DND_SYNC_TO_PHONE_KEY,
            DND_SYNC_WITH_THEATER_KEY
            -> {
                val value = newValue == true
                if (value) {
                    changingKey = key
                    messageClient.addListener(interruptFilterAccessListener)
                    interruptFilterAccessListenerRegistered = true
                    messageClient.sendMessage(phoneId, REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH, null)
                            .addOnFailureListener {
                                notifyError()
                            }
                } else {
                    sharedPreferences.edit().putBoolean(key, value).apply()
                    preferenceSyncHelper.pushNewData(key)
                }
                false
            }
            else -> true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        messageClient = Wearable.getMessageClient(requireContext())

        sharedPreferences = preferenceManager.sharedPreferences
        phoneId = sharedPreferences.getString(PHONE_ID_KEY, "") ?: ""
        preferenceSyncHelper = PreferenceSyncHelper(requireContext(), sharedPreferences, phoneId)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_battery_sync)
        addPreferencesFromResource(R.xml.prefs_interrupt_filter_sync)

        setupBatterySyncPrefs()
        setupDnDSyncPrefs()
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater?, parent: ViewGroup?, savedInstanceState: Bundle?): RecyclerView {
        return WearableRecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            isEdgeItemsCenteringEnabled = true
        }.also {
            it.requestFocus()
        }
    }

    override fun onStart() {
        super.onStart()
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        if (interruptFilterAccessListenerRegistered) {
            messageClient.removeListener(interruptFilterAccessListener)
        }
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
        findPreference<TwoStatePreference>(DND_SYNC_TO_WATCH_KEY)!!
                .onPreferenceChangeListener = this

        findPreference<TwoStatePreference>(DND_SYNC_TO_PHONE_KEY)!!
                .onPreferenceChangeListener = this

        findPreference<TwoStatePreference>(DND_SYNC_WITH_THEATER_KEY)!!
                .onPreferenceChangeListener = this
    }

    private fun notifyAdditionalSetupRequired(key: String) {
        launchMobileApp(requireContext(), phoneId, key)
        ConfirmationActivityHandler.openOnPhoneAnimation(requireContext(), getString(R.string.additional_setup_required))
    }

    private fun notifyError() {
        ConfirmationActivityHandler.failAnimation(requireContext(), getString(R.string.error))
    }

    private fun onInterruptFilterAccessResponse(hasAccess: Boolean) {
        interruptFilterAccessListenerRegistered = false
        messageClient.removeListener(interruptFilterAccessListener)
        if (changingKey.isNotEmpty()) {
            if (hasAccess) {
                sharedPreferences.edit().putBoolean(changingKey, hasAccess).apply()
                preferenceSyncHelper.pushNewData(changingKey)
            } else {
                notifyAdditionalSetupRequired(changingKey)
            }
        }
    }

    private fun launchMobileApp(context: Context, phoneId: String, key: String) {
        if (phoneId.isNotEmpty()) {
            Wearable.getMessageClient(context)
                    .sendMessage(phoneId, References.REQUEST_LAUNCH_APP_PATH, key.toByteArray(Charsets.UTF_8))
        }
    }
}
