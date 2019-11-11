/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.interruptfiltersync

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.Compat
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY
import com.boswelja.devicemanager.common.PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY
import com.boswelja.devicemanager.service.InterruptFilterLocalChangeListener
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.ui.interruptfiltersync.helper.InterruptFilterSyncHelperActivity

class InterruptFilterSyncPreferenceFragment :
        BasePreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    private lateinit var notificationManager: NotificationManager

    private lateinit var interruptFilterSyncToWatchPreference: SwitchPreference
    private lateinit var interruptFilterSyncToPhonePreference: SwitchPreference
    private lateinit var interruptFilterOnWithTheaterPreference: SwitchPreference

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            INTERRUPT_FILTER_SYNC_TO_WATCH_KEY -> {
                interruptFilterSyncToWatchPreference.isChecked = sharedPreferences?.getBoolean(key, false)!!
            }
            INTERRUPT_FILTER_SYNC_TO_PHONE_KEY -> {
                interruptFilterSyncToPhonePreference.isChecked = sharedPreferences?.getBoolean(key, false)!!
            }
            INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                interruptFilterOnWithTheaterPreference.isChecked = sharedPreferences?.getBoolean(key, false)!!
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (val key = preference?.key) {
            INTERRUPT_FILTER_SYNC_TO_WATCH_KEY -> {
                val value = newValue == true
                if (value) {
                    startActivityForResult(Intent(context, InterruptFilterSyncHelperActivity::class.java), HELPER_REQUEST_CODE)
                } else {
                    preference.sharedPreferences.edit()
                            .putBoolean(key, value).apply()
                    getWatchConnectionManager()?.updatePreference(key)
                    context?.stopService(Intent(context!!, InterruptFilterLocalChangeListener::class.java))
                }
                false
            }
            INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
            INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                val value = newValue == true
                preference.sharedPreferences.edit().putBoolean(key, value).apply()
                if (value) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || notificationManager.isNotificationPolicyAccessGranted) {
                        getWatchConnectionManager()?.updatePreference(key)
                    } else {
                        Toast.makeText(context, getString(R.string.interrupt_filter_sync_request_policy_access_message), Toast.LENGTH_SHORT).show()
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                    }
                } else {
                    getWatchConnectionManager()?.updatePreference(key)
                }
                false
            }
            else -> true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val notificationPolicyAccessGranted = notificationManager.isNotificationPolicyAccessGranted
            if (!notificationPolicyAccessGranted) {
                preferenceManager.sharedPreferences.edit()
                        .putBoolean(INTERRUPT_FILTER_SYNC_TO_PHONE_KEY, false)
                        .putBoolean(INTERRUPT_FILTER_ON_WITH_THEATER_KEY, false)
                        .apply()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_interrupt_filter_sync)

        interruptFilterSyncToWatchPreference = findPreference(INTERRUPT_FILTER_SYNC_TO_WATCH_KEY)!!
        interruptFilterSyncToPhonePreference = findPreference(INTERRUPT_FILTER_SYNC_TO_PHONE_KEY)!!
        interruptFilterOnWithTheaterPreference = findPreference(INTERRUPT_FILTER_ON_WITH_THEATER_KEY)!!

        interruptFilterSyncToWatchPreference.onPreferenceChangeListener = this
        interruptFilterSyncToPhonePreference.onPreferenceChangeListener = this
        interruptFilterOnWithTheaterPreference.onPreferenceChangeListener = this
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            HELPER_REQUEST_CODE -> {
                val enabled = resultCode == InterruptFilterSyncHelperActivity.RESULT_OK
                interruptFilterSyncToWatchPreference.isChecked = enabled
                interruptFilterSyncToWatchPreference.sharedPreferences.edit()
                        .putBoolean(INTERRUPT_FILTER_SYNC_TO_WATCH_KEY, enabled).apply()
                getWatchConnectionManager()?.updatePreference(INTERRUPT_FILTER_SYNC_TO_WATCH_KEY)
                if (enabled) {
                    Compat.startForegroundService(context!!, Intent(context!!, InterruptFilterLocalChangeListener::class.java))
                }
            }
        }
    }

    companion object {
        private const val HELPER_REQUEST_CODE = 12345
    }
}
