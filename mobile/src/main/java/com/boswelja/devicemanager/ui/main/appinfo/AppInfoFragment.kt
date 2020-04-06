/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.common.donate.ui.DonationDialog
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.ui.base.BasePreferenceFragment
import com.boswelja.devicemanager.ui.changelog.ChangelogDialogFragment
import com.boswelja.devicemanager.watchconnectionmanager.Watch
import com.boswelja.devicemanager.watchconnectionmanager.WatchConnectionInterface
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppInfoFragment :
        BasePreferenceFragment(),
        Preference.OnPreferenceClickListener,
        WatchConnectionInterface {

    private var customTabsIntent: CustomTabsIntent? = null

    private lateinit var messageClient: MessageClient
    private lateinit var watchVersionPreference: Preference

    private val messageListener = MessageClient.OnMessageReceivedListener {
        when (it.path) {
            References.REQUEST_APP_VERSION -> {
                val data = String(it.data, Charsets.UTF_8).split("|")
                watchVersionPreference.apply {
                    title = getString(R.string.pref_about_watch_version_title).format(data[0])
                    summary = data[1]
                }
            }
        }
    }

    override fun onWatchAdded(watch: Watch) {} // Do nothing

    override fun onConnectedWatchChanging() {} // Do nothing

    override fun onConnectedWatchChanged(success: Boolean) {
        if (success) {
            updateWatchVersionPreference()
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            OPEN_PRIVACY_POLICY_KEY -> {
                if (customTabsIntent == null) {
                    customTabsIntent = CustomTabsIntent.Builder().apply {
                        addDefaultShareMenuItem()
                        setShowTitle(true)
                    }.build()
                }
                customTabsIntent!!.launchUrl(context!!, getString(R.string.privacy_policy_url).toUri())
                true
            }
            SHARE_APP_KEY -> {
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, getAppStoreLink())
                    putExtra(Intent.EXTRA_TITLE, preference.title)
                    type = "text/plain"
                }.also {
                    startActivity(Intent.createChooser(it, null))
                }
                true
            }
            LEAVE_REVIEW_KEY -> {
                Intent(Intent.ACTION_VIEW).apply {
                    data = getAppStoreLink().toUri()
                    setPackage("com.android.vending")
                }.also { startActivity(it) }
                true
            }
            OPEN_DONATE_DIALOG_KEY -> {
                DonationDialog(R.style.AppTheme_AlertDialog).show(activity.supportFragmentManager)
                true
            }
            OPEN_CHANGELOG_KEY -> {
                ChangelogDialogFragment().show(activity.supportFragmentManager, "ChangelogDialog")
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messageClient = Wearable.getMessageClient(context!!)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_about)
        findPreference<Preference>(OPEN_PRIVACY_POLICY_KEY)!!.apply {
            onPreferenceClickListener = this@AppInfoFragment
        }
        findPreference<Preference>(SHARE_APP_KEY)!!.apply {
            isEnabled = !BuildConfig.DEBUG
            title = getString(R.string.pref_about_share_title).format(getString(R.string.app_name))
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
        findPreference<Preference>(OPEN_CHANGELOG_KEY)!!.apply {
            onPreferenceClickListener = this@AppInfoFragment
        }
        findPreference<Preference>(PHONE_VERSION_KEY)!!.apply {
            title = getString(R.string.pref_about_phone_version_title).format(BuildConfig.VERSION_NAME)
            summary = BuildConfig.VERSION_CODE.toString()
        }
        watchVersionPreference = findPreference(WATCH_VERSION_KEY)!!
    }

    override fun onResume() {
        super.onResume()

        messageClient.addListener(messageListener)
        updateWatchVersionPreference()

        getWatchConnectionManager()?.registerWatchConnectionInterface(this)
    }

    override fun onPause() {
        super.onPause()

        customTabsIntent = null
        messageClient.removeListener(messageListener)

        getWatchConnectionManager()?.unregisterWatchConnectionInterface(this)
    }

    private fun getAppStoreLink(): String =
            "https://play.google.com/store/apps/details?id=${context?.packageName}"

    private fun updateWatchVersionPreference() {
        val connectedWatchId = getWatchConnectionManager()?.getConnectedWatchId()
        if (!connectedWatchId.isNullOrEmpty()) {
            messageClient.sendMessage(connectedWatchId, References.REQUEST_APP_VERSION, null)
                    .addOnFailureListener {
                        watchVersionPreference.title = getString(R.string.pref_about_watch_version_disconnected)
                    }
                    .addOnSuccessListener {
                        watchVersionPreference.title = getString(R.string.pref_about_watch_version_loading)
                    }
        } else {
            watchVersionPreference.title = getString(R.string.pref_about_watch_version_failed)
        }
        watchVersionPreference.summary = ""
    }

    companion object {
        const val OPEN_PRIVACY_POLICY_KEY = "privacy_policy"
        const val SHARE_APP_KEY = "share"
        const val LEAVE_REVIEW_KEY = "review"
        const val OPEN_DONATE_DIALOG_KEY = "show_donate_dialog"
        const val OPEN_CHANGELOG_KEY = "show_changelog"
        const val PHONE_VERSION_KEY = "phone_app_version"
        const val WATCH_VERSION_KEY = "watch_app_version"
    }
}
