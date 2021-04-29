package com.boswelja.smartwatchextensions.dndsync

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.smartwatchextensions.common.Compat
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_TO_PHONE_KEY
import com.boswelja.smartwatchextensions.common.preference.PreferenceKey.DND_SYNC_WITH_THEATER_KEY
import com.boswelja.smartwatchextensions.dndsync.Utils.handleDnDStateChange
import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class DnDUtilsTest {

    private val watchId = UUID.randomUUID()

    @RelaxedMockK private lateinit var watchManager: WatchManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(Compat)
    }

    @Test
    fun `handleDnDStateChange disables preferences on failed`(): Unit = runBlocking {
        // Mock failure in setting DnD
        every { Compat.setInterruptionFilter(any(), any()) } returns false

        // Make the call with dndEnabled = true
        handleDnDStateChange(
            ApplicationProvider.getApplicationContext(),
            watchId,
            true,
            watchManager
        )

        // Verify calls to WatchManager
        coVerify { watchManager.updatePreference(DND_SYNC_TO_PHONE_KEY, false) }
        coVerify { watchManager.updatePreference(DND_SYNC_WITH_THEATER_KEY, false) }

        // Make the call with dndEnabled = false
        handleDnDStateChange(
            ApplicationProvider.getApplicationContext(),
            watchId,
            false,
            watchManager
        )

        // Verify calls to WatchManager
        coVerify { watchManager.updatePreference(DND_SYNC_TO_PHONE_KEY, false) }
        coVerify { watchManager.updatePreference(DND_SYNC_WITH_THEATER_KEY, false) }
    }
}
