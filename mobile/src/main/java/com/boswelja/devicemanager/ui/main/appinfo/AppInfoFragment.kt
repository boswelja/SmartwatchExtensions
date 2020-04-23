/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.appinfo

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
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
import timber.log.Timber

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
                val versionInfo = parseWatchVersionInfo(it.data)
                setWatchVersionInfo(versionInfo[0], versionInfo[1])
            }
        }
    }

    override fun onWatchAdded(watch: Watch) {} // Do nothing

    override fun onConnectedWatchChanging() {} // Do nothing

    override fun onConnectedWatchChanged(success: Boolean) {
        if (success) {
            requestUpdateWatchVersion()
        }
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        Timber.d("onPreferenceClick() called")
        when (preference?.key) {
            OPEN_PRIVACY_POLICY_KEY -> showPrivacyPolicy()
            SHARE_APP_KEY -> showShareMenu()
            LEAVE_REVIEW_KEY -> showPlayStorePage()
            OPEN_DONATE_DIALOG_KEY -> showDonationDialog()
            OPEN_CHANGELOG_KEY -> showChangelog()
            else -> return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        messageClient = Wearable.getMessageClient(requireContext())
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs_about)
        findPreference<Preference>(OPEN_PRIVACY_POLICY_KEY)!!.apply {
            onPreferenceClickListener = this@AppInfoFragment
        }
        findPreference<Preference>(SHARE_APP_KEY)!!.apply {
            isEnabled = !BuildConfig.DEBUG
            title = getString(R.string.pref_about_share_title)
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

    override fun onStart() {
        super.onStart()

        messageClient.addListener(messageListener)
        requestUpdateWatchVersion()

        getWatchConnectionManager()?.registerWatchConnectionInterface(this)
    }

    override fun onStop() {
        super.onStop()

        customTabsIntent = null
        messageClient.removeListener(messageListener)

        getWatchConnectionManager()?.unregisterWatchConnectionInterface(this)
    }

    /**
     * Gets the Play Store URL for Wearable Extensions.
     * @return The URL as a [String].
     */
    private fun getPlayStoreLink(): String =
            "https://play.google.com/store/apps/details?id=${context?.packageName}"

    /**
     * Requests the current app version info from the connected watch.
     * Result received in [messageListener] if sending the message was successful.
     */
    private fun requestUpdateWatchVersion() {
        Timber.d("requestUpdateWatchVersionPreference")
        val connectedWatchId = getWatchConnectionManager()?.getConnectedWatchId()
        if (!connectedWatchId.isNullOrEmpty()) {
            messageClient.sendMessage(connectedWatchId, References.REQUEST_APP_VERSION, null)
                    .addOnFailureListener {
                        Timber.w(it)
                        watchVersionPreference.title = getString(R.string.pref_about_watch_version_disconnected)
                    }
                    .addOnSuccessListener {
                        Timber.i("Message sent successfully")
                        watchVersionPreference.title = getString(R.string.pref_about_watch_version_loading)
                    }
        } else {
            Timber.w("connectedWatchId null or empty")
            watchVersionPreference.title = getString(R.string.pref_about_watch_version_failed)
        }
        watchVersionPreference.summary = ""
    }

    /**
     * Parse watch app version info from a given ByteArray.
     * @param byteArray The [ByteArray] received from the connected watch.
     * @return A [List] of [String] objects, count should always be 2.
     */
    private fun parseWatchVersionInfo(byteArray: ByteArray): List<String> {
        return String(byteArray, Charsets.UTF_8).split("|")
    }

    /**
     * Sets the watch version info shown to the user to the provided values.
     * @param versionName The version name of Wearable Extensions on
     * the connected watch (See [BuildConfig.VERSION_NAME].
     * @param versionCode The version code of Wearable Extensions on
     * the connected watch (See [BuildConfig.VERSION_CODE].
     */
    private fun setWatchVersionInfo(versionName: String, versionCode: String) {
        Timber.d("setWatchVersionInfo($versionName, $versionCode) called")
        watchVersionPreference.apply {
            title = getString(R.string.pref_about_watch_version_title).format(versionName)
            summary = versionCode
        }
    }

    /**
     * Launches a custom tab that navigates to the Wearable Extensions privacy policy.
     */
    private fun showPrivacyPolicy() {
        Timber.d("showPrivacyPolicy() called")
        if (customTabsIntent == null) {
            Timber.i("customTabsIntent null, creating new CustomTabsIntent")
            customTabsIntent = CustomTabsIntent.Builder().apply {
                addDefaultShareMenuItem()
                setShowTitle(true)
            }.build()
        }
        customTabsIntent!!.launchUrl(requireContext(), getString(R.string.privacy_policy_url).toUri())
    }

    /**
     * Shows the system Share sheet to share the Play Store link to Wearable Extensions.
     */
    private fun showShareMenu() {
        val shareTitle = getString(R.string.app_name)
        val shareDataUri = Uri.Builder().apply {
            scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            authority(resources.getResourcePackageName(R.mipmap.ic_launcher))
            appendPath(resources.getResourceTypeName(R.mipmap.ic_launcher))
            appendPath((resources.getResourceEntryName(R.mipmap.ic_launcher)))
        }.build()

        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getPlayStoreLink())
            putExtra(Intent.EXTRA_TITLE, shareTitle)
            data = shareDataUri
            type = "text/plain"
        }.also {
            Intent.createChooser(it, null).also { shareIntent ->
                Timber.i("Showing share sheet")
                startActivity(shareIntent)
            }
        }
    }

    /**
     * Opens the Play Store and navigates to the Wearable Extensions listing.
     */
    private fun showPlayStorePage() {
        Intent(Intent.ACTION_VIEW).apply {
            data = getPlayStoreLink().toUri()
            setPackage("com.android.vending")
        }.also {
            Timber.i("Opening Play Store")
            startActivity(it)
        }
    }

    /**
     * Create an instance of [DonationDialog] and shows it.
     */
    private fun showDonationDialog() {
        Timber.d("showDonationDialog() called")
        DonationDialog(R.style.AppTheme_AlertDialog).show(activity.supportFragmentManager)
    }

    /**
     * Creates an instance of [ChangelogDialogFragment] and shows it.
     */
    private fun showChangelog() {
        Timber.d("showChangelog() called")
        ChangelogDialogFragment().show(activity.supportFragmentManager)
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
