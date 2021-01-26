package com.boswelja.devicemanager.analytics

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.matchBundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class AnalyticsTest {

    @RelaxedMockK private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var analytics: Analytics

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        analytics = Analytics(firebaseAnalytics, sharedPreferences)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit(commit = true) { clear() }
    }

    @Test
    fun `Analytics are not logged if the user has them disabled`() {
        // Set send_analytics to false
        setAnalyticsEnabled(false)

        // Call every log function from Analytics
        analytics.logStorageManagerAction("action")
        analytics.logWatchRemoved()
        analytics.logWatchRegistered()
        analytics.logAppSettingChanged("key", "value")
        analytics.logExtensionSettingChanged("key", "value")

        // Verify firebaseAnalytics was not called
        verify { firebaseAnalytics wasNot Called }
    }

    @Test
    fun `Analytics are logged if the user has them enabled`() {
        // Set send_analytics to false
        setAnalyticsEnabled(true)

        // Call every log function from Analytics
        analytics.logStorageManagerAction("action")
        analytics.logWatchRemoved()
        analytics.logWatchRegistered()
        analytics.logAppSettingChanged("key", "value")
        analytics.logExtensionSettingChanged("key", "value")

        // Verify firebaseAnalytics was not called
        verify(exactly = 5) { firebaseAnalytics.logEvent(any(), any()) }
        confirmVerified()
    }

    @Test
    fun `logStorageManagerAction correctly logs an event`() {
        // Set send_analytics to false
        setAnalyticsEnabled(true)

        val action = "action"
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.METHOD, action)

        analytics.logStorageManagerAction(action)
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(Analytics.EVENT_STORAGE_MANAGER, matchBundle(bundle))
        }
        confirmVerified()
    }

    @Test
    fun `logWatchRemoved correctly logs an event`() {
        // Set send_analytics to false
        setAnalyticsEnabled(true)

        analytics.logWatchRemoved()
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(Analytics.EVENT_WATCH_REMOVED, null)
        }
        confirmVerified()
    }

    @Test
    fun `logWatchRegistered correctly logs an event`() {
        // Set send_analytics to false
        setAnalyticsEnabled(true)

        analytics.logWatchRegistered()
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(Analytics.EVENT_WATCH_REGISTERED, null)
        }
        confirmVerified()
    }

    @Test
    fun `logAppSettingChanged correctly logs an event`() {
        // Set send_analytics to false
        setAnalyticsEnabled(true)

        val key = "key"
        val value = true
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, key)
        bundle.putString(FirebaseAnalytics.Param.VALUE, value.toString())

        analytics.logAppSettingChanged(key, value)
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(Analytics.EVENT_APP_SETTING_CHANGED, matchBundle(bundle))
        }
        confirmVerified()
    }

    @Test
    fun `logExtensionSettingChanged correctly logs an event`() {
        // Set send_analytics to false
        setAnalyticsEnabled(true)

        val key = "key"
        val value = true
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, key)
        bundle.putString(FirebaseAnalytics.Param.VALUE, value.toString())

        analytics.logExtensionSettingChanged(key, value)
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(
                Analytics.EVENT_EXTENSION_SETTING_CHANGED,
                matchBundle(bundle)
            )
        }
        confirmVerified()
    }

    private fun setAnalyticsEnabled(enabled: Boolean) {
        sharedPreferences.edit(commit = true) {
            putBoolean(Analytics.ANALYTICS_ENABLED_KEY, enabled)
        }
    }
}
