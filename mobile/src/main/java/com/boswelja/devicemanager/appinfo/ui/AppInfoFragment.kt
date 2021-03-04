/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.appinfo.ui

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.activity.BasePreferenceFragment
import timber.log.Timber

class AppInfoFragment : BasePreferenceFragment(), Preference.OnPreferenceClickListener {

    private val viewModel: AppInfoViewModel by viewModels()

    private val watchVersionPreference: Preference by lazy { findPreference(WATCH_VERSION_KEY)!! }
    private val customTabsIntent: CustomTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        Timber.d("onPreferenceClick() called")
        when (preference?.key) {
            OPEN_PRIVACY_POLICY_KEY -> showPrivacyPolicy()
            SHARE_APP_KEY -> showShareMenu()
            OPEN_GITHUB_KEY -> showGitHubPage()
            OPEN_CHANGELOG_KEY -> showChangelog()
            OPEN_DONATE_KEY ->
                findNavController().navigate(AppInfoFragmentDirections.toDonateActivity())
            else -> return false
        }
        return true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_about)
        setupPreferences()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.watchAppVersion.observe(viewLifecycleOwner) {
            when {
                it?.first != null -> {
                    watchVersionPreference.title =
                        getString(R.string.pref_about_watch_version_title, it.first)
                    watchVersionPreference.summary = it.second
                }
                it == null -> {
                    watchVersionPreference.setTitle(R.string.pref_about_watch_version_failed)
                    watchVersionPreference.summary = null
                }
                else -> {
                    watchVersionPreference.setTitle(R.string.pref_about_watch_version_loading)
                    watchVersionPreference.summary = null
                }
            }
        }
        watchManager.selectedWatch.observe(viewLifecycleOwner) {
            it?.id?.let { id -> viewModel.requestUpdateWatchVersion(id) }
        }
    }

    private fun setupPreferences() {
        findPreference<Preference>(OPEN_PRIVACY_POLICY_KEY)!!
            .onPreferenceClickListener = this
        findPreference<Preference>(SHARE_APP_KEY)!!.apply {
            isEnabled = !BuildConfig.DEBUG
            onPreferenceClickListener = this@AppInfoFragment
        }
        findPreference<Preference>(OPEN_GITHUB_KEY)!!
            .onPreferenceClickListener = this
        findPreference<Preference>(OPEN_CHANGELOG_KEY)!!
            .onPreferenceClickListener = this
        findPreference<Preference>(OPEN_DONATE_KEY)!!.apply {
            // isEnabled = !BuildConfig.DEBUG
            onPreferenceClickListener = this@AppInfoFragment
        }
        findPreference<Preference>(PHONE_VERSION_KEY)!!.apply {
            title =
                getString(R.string.pref_about_phone_version_title).format(BuildConfig.VERSION_NAME)
            summary = BuildConfig.VERSION_CODE.toString()
        }
    }

    /** Launches a custom tab that navigates to the Wearable Extensions privacy policy. */
    private fun showPrivacyPolicy() {
        Timber.d("showPrivacyPolicy() called")
        customTabsIntent.launchUrl(requireContext(), getString(R.string.privacy_policy_url).toUri())
    }

    /** Shows the system Share sheet to share the Play Store link to Wearable Extensions. */
    private fun showShareMenu() {
        val shareTitle = getString(R.string.app_name)
        val shareDataUri =
            Uri.Builder()
                .apply {
                    scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    authority(resources.getResourcePackageName(R.mipmap.ic_launcher))
                    appendPath(resources.getResourceTypeName(R.mipmap.ic_launcher))
                    appendPath((resources.getResourceEntryName(R.mipmap.ic_launcher)))
                }
                .build()

        Intent()
            .apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.play_store_link))
                putExtra(Intent.EXTRA_TITLE, shareTitle)
                data = shareDataUri
                type = "text/plain"
            }
            .also {
                Intent.createChooser(it, null).also { shareIntent ->
                    Timber.i("Showing share sheet")
                    startActivity(shareIntent)
                }
            }
    }

    /** Launches a custom tab that navigates to the Wearable Extensions GitHub page. */
    private fun showGitHubPage() {
        customTabsIntent.launchUrl(requireContext(), getString(R.string.github_url).toUri())
    }

    private fun showChangelog() {
        customTabsIntent.launchUrl(requireContext(), getString(R.string.changelog_url).toUri())
    }

    companion object {
        const val OPEN_PRIVACY_POLICY_KEY = "privacy_policy"
        const val SHARE_APP_KEY = "share"
        const val OPEN_GITHUB_KEY = "github"
        const val OPEN_DONATE_KEY = "show_donate_dialog"
        const val OPEN_CHANGELOG_KEY = "changelog"
        const val PHONE_VERSION_KEY = "phone_app_version"
        const val WATCH_VERSION_KEY = "watch_app_version"
    }
}
