package com.boswelja.devicemanager.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.activity.ConfirmationActivity
import androidx.wear.widget.WearableRecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.prefsynclayer.PreferenceSyncLayer

class SettingsFragment :
        PreferenceFragmentCompat(),
        Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var prefs: SharedPreferences
    private lateinit var preferenceSyncLayer: PreferenceSyncLayer

    private lateinit var batterySyncEnabledPref: SwitchPreference
    private lateinit var batteryPhoneChargedNotiPref: CheckBoxPreference
    private lateinit var batteryWatchChargedNotiPref: CheckBoxPreference
    private lateinit var dndSyncPhoneToWatchPref: SwitchPreference
    private lateinit var dndSyncWatchToPhonePref: SwitchPreference
    private lateinit var dndSyncWithTheaterPref: SwitchPreference

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY -> {
                batterySyncEnabledPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY -> {
                batteryPhoneChargedNotiPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY -> {
                batteryWatchChargedNotiPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY -> {
                dndSyncPhoneToWatchPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY -> {
                dndSyncWatchToPhonePref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY -> {
                dndSyncWithTheaterPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        val key = preference?.key!!
        return when (key) {
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
            PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY,
            PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY -> {
                val value = newValue == true
                prefs.edit().putBoolean(key, value).apply()
                preferenceSyncLayer.updateData()
                false
            }
            PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY -> {
                val value = newValue == true
                if (value) {
                    val canEnableSync = Utils.checkDnDAccess(context!!)
                    if (canEnableSync) {
                        prefs.edit().putBoolean(key, value).apply()
                        preferenceSyncLayer.updateData()
                    } else {
                        val intent = Intent(context, ConfirmationActivity::class.java).apply {
                            putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION)
                            putExtra(ConfirmationActivity.EXTRA_MESSAGE, getString(R.string.additional_setup_required))
                        }
                        startActivity(intent)
                    }
                } else {
                    prefs.edit().putBoolean(key, value).apply()
                    preferenceSyncLayer.updateData()
                }
                false
            }
            PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY -> {
                false
            }
            PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY -> {
                false
            }
            else -> true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceSyncLayer = PreferenceSyncLayer(context!!)
        prefs = preferenceManager.sharedPreferences
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_battery_sync)
        setupBatterySyncPrefs()

        addPreferencesFromResource(R.xml.prefs_dnd_sync)
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
        batterySyncEnabledPref = findPreference(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)!!
        batterySyncEnabledPref.onPreferenceChangeListener = this

        batteryPhoneChargedNotiPref = findPreference(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY)!!
        batteryPhoneChargedNotiPref.onPreferenceChangeListener = this

        batteryWatchChargedNotiPref = findPreference(PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY)!!
        batteryWatchChargedNotiPref.onPreferenceChangeListener = this
    }

    private fun setupDnDSyncPrefs() {
        dndSyncPhoneToWatchPref = findPreference(PreferenceKey.DND_SYNC_PHONE_TO_WATCH_KEY)!!
        dndSyncPhoneToWatchPref.onPreferenceChangeListener = this

        dndSyncWatchToPhonePref = findPreference(PreferenceKey.DND_SYNC_WATCH_TO_PHONE_KEY)!!
        dndSyncWatchToPhonePref.onPreferenceChangeListener = this

        dndSyncWithTheaterPref = findPreference(PreferenceKey.DND_SYNC_WITH_THEATER_MODE_KEY)!!
        dndSyncWithTheaterPref.onPreferenceChangeListener = this
    }
}