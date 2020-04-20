/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.dndsync

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.common.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.devicemanager.dndsync.DnDLocalChangeService
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.ui.dndsync.helper.DnDSyncHelperActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class DnDSyncPreferenceFragment :
        BasePreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    private val coroutineScope = MainScope()

    private lateinit var notificationManager: NotificationManager

    private lateinit var dndSyncToWatchPreference: SwitchPreference
    private lateinit var dndSyncToPhonePreference: SwitchPreference
    private lateinit var dndSyncWithTheaterPreference: SwitchPreference

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DND_SYNC_TO_WATCH_KEY -> {
                dndSyncToWatchPreference.isChecked =
                        sharedPreferences?.getBoolean(key, false)!!
            }
            DND_SYNC_TO_PHONE_KEY -> {
                dndSyncToPhonePreference.isChecked =
                        sharedPreferences?.getBoolean(key, false)!!
            }
            DND_SYNC_WITH_THEATER_KEY -> {
                dndSyncWithTheaterPreference.isChecked =
                        sharedPreferences?.getBoolean(key, false)!!
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (val key = preference?.key) {
            DND_SYNC_TO_WATCH_KEY -> {
                val enabled = newValue == true
                if (enabled) {
                    startDnDSyncHelper()
                } else {
                    setDnDSyncToWatch(enabled)
                }
                false
            }
            DND_SYNC_TO_PHONE_KEY,
            DND_SYNC_WITH_THEATER_KEY -> {
                val enabled = newValue == true
                setDnDSyncFromWatchPreference(key, enabled)
                false
            }
            else -> true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate() called")
        notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        Timber.d("onCreatePreferences() called")
        addPreferencesFromResource(R.xml.prefs_interrupt_filter_sync)

        dndSyncToWatchPreference = findPreference(DND_SYNC_TO_WATCH_KEY)!!
        dndSyncToPhonePreference = findPreference(DND_SYNC_TO_PHONE_KEY)!!
        dndSyncWithTheaterPreference = findPreference(DND_SYNC_WITH_THEATER_KEY)!!

        dndSyncToWatchPreference.onPreferenceChangeListener = this
        dndSyncToPhonePreference.onPreferenceChangeListener = this
        dndSyncWithTheaterPreference.onPreferenceChangeListener = this
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart() called")
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop() called")
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult() called")
        when (requestCode) {
            HELPER_REQUEST_CODE -> {
                val shouldEnable = resultCode == DnDSyncHelperActivity.RESULT_OK
                setDnDSyncToWatch(shouldEnable)
            }
        }
    }

    /**
     * Sets whether DnD Sync to watch is enabled.
     * @param enabled true if DnD Sync to Watch should be enabled, false otherwise.
     */
    private fun setDnDSyncToWatch(enabled: Boolean) {
        Timber.i("Setting DnD Sync to watch to $enabled")
        dndSyncToWatchPreference.isChecked = enabled
        coroutineScope.launch(Dispatchers.IO) {
            sharedPreferences.edit(commit = true) {
                putBoolean(DND_SYNC_TO_WATCH_KEY, enabled)
            }
            getWatchConnectionManager()?.updatePreferenceOnWatch(DND_SYNC_TO_WATCH_KEY)
        }
        if (enabled) {
            Timber.i("Starting DnDLocalChangeService")
            Intent(context!!, DnDLocalChangeService::class.java).also {
                Compat.startForegroundService(context!!, it)
            }
        }
    }

    /**
     * Sets whether a DnD Sync from watch preference is enabled.
     * @param key The key of the preference to set.
     * @param enabled true if the preference should be enabled, false otherwise.
     */
    private fun setDnDSyncFromWatchPreference(key: String, enabled: Boolean) {
        Timber.i("Setting $key to $enabled")
        coroutineScope.launch(Dispatchers.IO) {
            sharedPreferences.edit(commit = true) {
                putBoolean(key, enabled)
            }
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !notificationManager.isNotificationPolicyAccessGranted) {
                Timber.i("Missing notification policy access, requesting")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context,
                            getString(R.string.interrupt_filter_sync_request_policy_access_message),
                            Toast.LENGTH_SHORT).show()
                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).also {
                        startActivity(it)
                    }
                }
            } else {
                getWatchConnectionManager()?.updatePreferenceOnWatch(key)
            }
        }
    }

    /**
     * Starts a new [DnDSyncHelperActivity] instance and shows it.
     */
    private fun startDnDSyncHelper() {
        Intent(context, DnDSyncHelperActivity::class.java).also {
            startActivityForResult(it, HELPER_REQUEST_CODE)
        }
    }

    companion object {
        private const val HELPER_REQUEST_CODE = 12345
    }
}
