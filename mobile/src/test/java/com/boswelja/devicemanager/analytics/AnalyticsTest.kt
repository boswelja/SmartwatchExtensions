package com.boswelja.devicemanager.analytics

import android.os.Build
import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.matchBundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.MockKAnnotations
import io.mockk.confirmVerified
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class AnalyticsTest {

    @RelaxedMockK private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var analytics: Analytics

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        analytics = Analytics(firebaseAnalytics)
    }

    @Test
    fun `logStorageManagerAction correctly logs an event`() {
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
        analytics.logWatchRemoved()
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(Analytics.EVENT_WATCH_REMOVED, null)
        }
        confirmVerified()
    }

    @Test
    fun `logWatchRenamed correctly logs an event`() {
        analytics.logWatchRenamed()
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(Analytics.EVENT_WATCH_RENAMED, null)
        }
        confirmVerified()
    }

    @Test
    fun `logWatchRegistered correctly logs an event`() {
        analytics.logWatchRegistered()
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(Analytics.EVENT_WATCH_REGISTERED, null)
        }
        confirmVerified()
    }

    @Test
    fun `logAppSettingChanged correctly logs an event`() {
        val key = "key"
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, key)

        analytics.logAppSettingChanged(key)
        verify(exactly = 1) {
            firebaseAnalytics.logEvent(Analytics.EVENT_APP_SETTING_CHANGED, matchBundle(bundle))
        }
        confirmVerified()
    }

    @Test
    fun `logExtensionSettingChanged correctly logs an event`() {
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
}
