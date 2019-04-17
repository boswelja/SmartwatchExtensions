package com.boswelja.devicemanager.ui

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
import androidx.wear.widget.WearableRecyclerView
import com.boswelja.devicemanager.ConfirmationActivityHandler
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References
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

    private lateinit var batterySyncEnabledPref: SwitchPreference
    private lateinit var batteryPhoneChargedNotiPref: CheckBoxPreference
    private lateinit var batteryWatchChargedNotiPref: CheckBoxPreference
    private lateinit var dndSyncPhoneToWatchPref: SwitchPreference
    private lateinit var dndSyncWatchToPhonePref: SwitchPreference
    private lateinit var dndSyncWithTheaterPref: SwitchPreference

    private var changingKey = ""

    private val interruptFilterAccessListener = MessageClient.OnMessageReceivedListener {
        if (it.path == References.REQUEST_PHONE_DND_ACCESS_STATUS_PATH) {
            val hasAccess = it.data[0].toInt() == 1
            onInterruptFilterAccessResponse(hasAccess)
        }
    }

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
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY -> {
                dndSyncPhoneToWatchPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY -> {
                dndSyncWatchToPhonePref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
            PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                dndSyncWithTheaterPref.isChecked = sharedPreferences?.getBoolean(key, false) == true
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        return when (val key = preference?.key) {
            PreferenceKey.BATTERY_SYNC_ENABLED_KEY,
            PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY,
            PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY -> {
                val value = newValue == true
                prefs.edit().putBoolean(key, value).apply()
                preferenceSyncLayer.pushNewData()
                false
            }
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY -> {
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
            PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY,
            PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY -> {
                val value = newValue == true
                if (value) {
                    changingKey = key
                    messageClient.addListener(interruptFilterAccessListener)
                    Utils.getCompanionNode(context!!).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val nodes = it.result?.nodes
                            if (!nodes.isNullOrEmpty()) {
                                val node = nodes.first { node -> node.isNearby }
                                messageClient.sendMessage(node?.id!!, References.REQUEST_PHONE_DND_ACCESS_STATUS_PATH, null)
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
        batterySyncEnabledPref = findPreference(PreferenceKey.BATTERY_SYNC_ENABLED_KEY)!!
        batterySyncEnabledPref.onPreferenceChangeListener = this

        batteryPhoneChargedNotiPref = findPreference(PreferenceKey.BATTERY_PHONE_FULL_CHARGE_NOTI_KEY)!!
        batteryPhoneChargedNotiPref.onPreferenceChangeListener = this

        batteryWatchChargedNotiPref = findPreference(PreferenceKey.BATTERY_WATCH_FULL_CHARGE_NOTI_KEY)!!
        batteryWatchChargedNotiPref.onPreferenceChangeListener = this
    }

    private fun setupDnDSyncPrefs() {
        dndSyncPhoneToWatchPref = findPreference(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_WATCH_KEY)!!
        dndSyncPhoneToWatchPref.onPreferenceChangeListener = this

        dndSyncWatchToPhonePref = findPreference(PreferenceKey.INTERRUPT_FILTER_SYNC_TO_PHONE_KEY)!!
        dndSyncWatchToPhonePref.onPreferenceChangeListener = this

        dndSyncWithTheaterPref = findPreference(PreferenceKey.INTERRUPT_FILTER_ON_WITH_THEATER_KEY)!!
        dndSyncWithTheaterPref.onPreferenceChangeListener = this
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