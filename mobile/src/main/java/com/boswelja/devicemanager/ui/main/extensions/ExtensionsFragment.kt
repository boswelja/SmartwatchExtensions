/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.extensions

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPrefFragment
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPreference
import com.boswelja.devicemanager.ui.appmanager.AppManagerActivity
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.ui.batterysync.BatterySyncPreferenceActivity
import com.boswelja.devicemanager.ui.dndsync.DnDSyncPreferenceActivity
import com.boswelja.devicemanager.ui.phonelocking.PhoneLockingPreferenceActivity
import timber.log.Timber

class ExtensionsFragment :
        BasePreferenceFragment(),
        Preference.OnPreferenceClickListener {

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            OPEN_BATTERY_SYNC_PREF_KEY -> {
                openBatterySyncActivity()
                true
            }
            OPEN_DND_SYNC_PREF_KEY -> {
                openDnDSyncActivity()
                true
            }
            OPEN_PHONE_LOCKING_PREF_KEY -> {
                openPhoneLockingActivity()
                true
            }
            OPEN_APP_MANAGER_KEY -> {
                openAppManagerActivity()
                true
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_main)
        findPreference<Preference>(OPEN_BATTERY_SYNC_PREF_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(OPEN_DND_SYNC_PREF_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(OPEN_PHONE_LOCKING_PREF_KEY)!!.onPreferenceClickListener = this
        findPreference<Preference>(OPEN_APP_MANAGER_KEY)!!.onPreferenceClickListener = this
    }

    override fun onDisplayPreferenceDialog(preference: Preference?) {
        when (preference) {
            is ConfirmationDialogPreference -> {
                showConfirmationDialogPrefFragment(preference)
            }
            else -> super.onDisplayPreferenceDialog(preference)
        }
    }

    /**
     * Opens a [BatterySyncPreferenceActivity].
     */
    private fun openBatterySyncActivity() {
        Intent(requireContext(), BatterySyncPreferenceActivity::class.java).also {
            Timber.i("Starting BatterySyncPreferenceActivity")
            startActivity(it)
        }
    }

    /**
     * Opens a [DnDSyncPreferenceActivity].
     */
    private fun openDnDSyncActivity() {
        Intent(requireContext(), DnDSyncPreferenceActivity::class.java).also {
            Timber.i("Starting DnDSyncPreferenceActivity")
            startActivity(it)
        }
    }

    /**
     * Opens a [PhoneLockingPreferenceActivity].
     */
    private fun openPhoneLockingActivity() {
        Intent(requireContext(), PhoneLockingPreferenceActivity::class.java).also {
            Timber.i("Starting PhoneLockingPreferenceActivity")
            startActivity(it)
        }
    }

    /**
     * Opens an [AppManagerActivity].
     */
    private fun openAppManagerActivity() {
        Intent(requireContext(), AppManagerActivity::class.java).apply {
            val connectedWatch = activity.watchConnectionManager?.getConnectedWatch()
            putExtra(AppManagerActivity.EXTRA_WATCH_ID, connectedWatch?.id)
            putExtra(AppManagerActivity.EXTRA_WATCH_NAME, connectedWatch?.name)
        }.also {
            Timber.i("Starting AppManagerActivity")
            startActivity(it)
        }
    }

    /**
     * Shows a [ConfirmationDialogPrefFragment] for the corresponding [ConfirmationDialogPreference].
     * @param preference The [ConfirmationDialogPreference] requesting the [ConfirmationDialogPrefFragment].
     */
    private fun showConfirmationDialogPrefFragment(preference: ConfirmationDialogPreference) {
        ConfirmationDialogPrefFragment.newInstance(preference.key).apply {
            setTargetFragment(this@ExtensionsFragment, 0)
            Timber.i("Showing ConfirmationDialogPrefFragment")
            show(parentFragmentManager)
        }
    }

    companion object {
        const val OPEN_BATTERY_SYNC_PREF_KEY = "show_battery_sync_prefs"
        const val OPEN_DND_SYNC_PREF_KEY = "show_interrupt_filter_sync_prefs"
        const val OPEN_PHONE_LOCKING_PREF_KEY = "show_phone_locking_prefs"
        const val OPEN_APP_MANAGER_KEY = "show_app_manager"
    }
}
