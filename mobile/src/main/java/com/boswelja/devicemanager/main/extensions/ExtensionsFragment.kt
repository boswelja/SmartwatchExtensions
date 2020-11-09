/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.main.extensions

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BasePreferenceFragment
import com.boswelja.devicemanager.dashboard.ui.DashboardFragmentDirections
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPrefFragment
import com.boswelja.devicemanager.preference.confirmationdialog.ConfirmationDialogPreference
import com.boswelja.devicemanager.watchmanager.WatchManager
import timber.log.Timber

class ExtensionsFragment : BasePreferenceFragment(), Preference.OnPreferenceClickListener {

    private val watchManager by lazy { WatchManager.get(requireContext()) }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            OPEN_BATTERY_SYNC_PREF_KEY -> {
                findNavController().navigate(DashboardFragmentDirections.toBatterySyncActivity())
                true
            }
            OPEN_DND_SYNC_PREF_KEY -> {
                findNavController().navigate(DashboardFragmentDirections.toDndSyncActivity())
                true
            }
            OPEN_PHONE_LOCKING_PREF_KEY -> {
                findNavController().navigate(DashboardFragmentDirections.toPhoneLockingActivity())
                true
            }
            OPEN_APP_MANAGER_KEY -> {
                watchManager.connectedWatch.value?.let {
                    findNavController()
                        .navigate(
                            DashboardFragmentDirections.toAppManagerActivity(
                                watchId = it.id, watchName = it.name
                            )
                        )
                }
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
     * Shows a [ConfirmationDialogPrefFragment] for the corresponding [ConfirmationDialogPreference]
     * .
     * @param preference The [ConfirmationDialogPreference] requesting the
     * [ConfirmationDialogPrefFragment].
     */
    private fun showConfirmationDialogPrefFragment(preference: ConfirmationDialogPreference) {
        ConfirmationDialogPrefFragment.newInstance(preference.key).apply {
            Timber.i("Showing ConfirmationDialogPrefFragment")
            show(childFragmentManager)
        }
    }

    companion object {
        const val OPEN_BATTERY_SYNC_PREF_KEY = "show_battery_sync_prefs"
        const val OPEN_DND_SYNC_PREF_KEY = "show_interrupt_filter_sync_prefs"
        const val OPEN_PHONE_LOCKING_PREF_KEY = "show_phone_locking_prefs"
        const val OPEN_APP_MANAGER_KEY = "show_app_manager"
    }
}
