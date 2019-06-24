/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.appinfo

import android.content.Intent
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.preference.Preference
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.ui.donate.DonationDialogFragment
import com.boswelja.devicemanager.ui.version.ChangelogDialogFragment

class AppInfoFragment :
        BasePreferenceFragment(),
        Preference.OnPreferenceClickListener {

    private var customTabsIntent: CustomTabsIntent? = null

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            OPEN_PRIVACY_POLICY_KEY -> {
                if (customTabsIntent == null) {
                    customTabsIntent = CustomTabsIntent.Builder().apply {
                        addDefaultShareMenuItem()
                        setShowTitle(true)
                    }.build()
                }
                customTabsIntent!!.launchUrl(context, getString(R.string.privacy_policy_url).toUri())
                true
            }
            LEAVE_REVIEW_KEY -> {
                Intent(Intent.ACTION_VIEW).apply {
                    data = "https://play.google.com/store/apps/details?id=${context?.packageName}".toUri()
                    setPackage("com.android.vending")
                }.also { startActivity(it) }
                true
            }
            OPEN_DONATE_DIALOG_KEY -> {
                DonationDialogFragment().show(activity?.supportFragmentManager!!, "DonationDialog")
                true
            }
            VERSION_KEY -> {
                ChangelogDialogFragment().show(activity?.supportFragmentManager!!, "ChangelogDialog")
                true
            }
            else -> false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_about)
        findPreference<Preference>(OPEN_PRIVACY_POLICY_KEY)!!.apply {
            onPreferenceClickListener = this@AppInfoFragment
        }
        findPreference<Preference>(LEAVE_REVIEW_KEY)!!.apply {
            isEnabled = !BuildConfig.DEBUG
            onPreferenceClickListener = this@AppInfoFragment
        }
        findPreference<Preference>(OPEN_DONATE_DIALOG_KEY)!!.apply {
            isEnabled = !BuildConfig.DEBUG
            onPreferenceClickListener = this@AppInfoFragment
        }
        findPreference<Preference>(VERSION_KEY)!!.apply {
            onPreferenceClickListener = this@AppInfoFragment
        }
    }

    override fun onPause() {
        super.onPause()
        customTabsIntent = null
    }

    companion object {
        const val OPEN_PRIVACY_POLICY_KEY = "privacy_policy"
        const val LEAVE_REVIEW_KEY = "review"
        const val OPEN_DONATE_DIALOG_KEY = "show_donate_dialog"
        const val VERSION_KEY = "version"
    }
}
