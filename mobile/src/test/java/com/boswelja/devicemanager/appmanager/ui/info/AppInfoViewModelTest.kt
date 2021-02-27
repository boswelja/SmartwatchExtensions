package com.boswelja.devicemanager.appmanager.ui.info

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.common.appmanager.Messages.REQUEST_OPEN_PACKAGE
import com.boswelja.devicemanager.common.appmanager.Messages.REQUEST_UNINSTALL_PACKAGE
import com.google.android.gms.wearable.MessageClient
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class AppInfoViewModelTest {

    private val watchId = "12345"
    private val app = App(
        null,
        "v1.0",
        "com.dummy.app",
        "App 1",
        isSystemApp = false,
        hasLaunchActivity = false,
        installTime = 0,
        lastUpdateTime = 0,
        requestedPermissions = emptyArray()
    )

    @RelaxedMockK
    private lateinit var messageClient: MessageClient

    private lateinit var viewModel: AppInfoViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = AppInfoViewModel(ApplicationProvider.getApplicationContext(), messageClient)
        viewModel.watchId = watchId
    }

    @Test
    fun `sendUninstallRequestMessage does nothing with null app`() {
        viewModel.app = null
        viewModel.sendUninstallRequestMessage()
        verify(inverse = true) { messageClient.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `sendUninstallRequestMessage sends message correctly`() {
        viewModel.app = app
        viewModel.sendUninstallRequestMessage()
        verify(exactly = 1) {
            messageClient.sendMessage(
                watchId,
                REQUEST_UNINSTALL_PACKAGE,
                app.packageName.toByteArray(Charsets.UTF_8)
            )
        }
    }

    @Test
    fun `sendOpenRequestMessage does nothing with null app`() {
        viewModel.app = null
        viewModel.sendOpenRequestMessage()
        verify(inverse = true) { messageClient.sendMessage(any(), any(), any()) }
    }

    @Test
    fun `sendOpenRequestMessage sends message correctly`() {
        viewModel.app = app
        viewModel.sendOpenRequestMessage()
        verify(exactly = 1) {
            messageClient.sendMessage(
                watchId,
                REQUEST_OPEN_PACKAGE,
                app.packageName.toByteArray(Charsets.UTF_8)
            )
        }
    }
}
