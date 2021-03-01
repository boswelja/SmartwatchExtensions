package com.boswelja.devicemanager.appmanager.ui.info

import android.Manifest
import android.app.Application
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.ui.info.AppInfoActivity.Companion.EXTRA_APP_INFO
import com.boswelja.devicemanager.common.SerializableBitmap
import com.boswelja.devicemanager.common.appmanager.App
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matchers.not
import org.junit.Test

class AppInfoActivityTest {

    private val appIcon = ContextCompat.getDrawable(
        ApplicationProvider.getApplicationContext(),
        R.mipmap.ic_launcher
    )?.toBitmap()?.let { SerializableBitmap(it) }

    @Test
    fun viewsVisible() {
        val app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = true,
            lastUpdateTime = System.currentTimeMillis() + 1000,
            requestedPermissions = arrayOf(Manifest.permission.INTERNET)
        )
        launchActivity(app)

        onView(withId(R.id.app_icon)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_name)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.open_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.uninstall_button)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.permissions_info)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_install_time)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_last_updated_time)).check(matches(isCompletelyDisplayed()))
        onView(withId(R.id.app_version_view)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun uninstallButtonDisabledForSystemApps() {
        val app = createApp(
            appIcon,
            isSystemApp = true,
            hasLaunchActivity = true,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = emptyArray()
        )
        launchActivity(app)

        onView(withId(R.id.uninstall_button)).check(matches(not(isEnabled())))
    }

    @Test
    fun uninstallButtonEnabledForUserApps() {
        val app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = true,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = emptyArray()
        )
        launchActivity(app)

        onView(withId(R.id.uninstall_button)).check(matches(isEnabled()))
    }

    @Test
    fun openButtonEnabledIfHasLaunchIntent() {
        val app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = true,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = emptyArray()
        )
        launchActivity(app)

        onView(withId(R.id.open_button)).check(matches(isEnabled()))
    }

    @Test
    fun openButtonDisabledIfHasNoLaunchIntent() {
        val app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = false,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = emptyArray()
        )
        launchActivity(app)

        onView(withId(R.id.open_button)).check(matches(not(isEnabled())))
    }

    @Test
    fun requestedPermissionFragmentShowsIfHasRequestedPermissions() {
        val app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = false,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = arrayOf(Manifest.permission.INTERNET)
        )
        val scenario = launchActivity(app)

        onView(withId(R.id.permissions_info)).perform(click())
        scenario.onActivity {
            assertThat(
                it.supportFragmentManager.findFragmentByTag("RequestedPermissionsDialog")
            ).isInstanceOf(AppPermissionDialogFragment::class.java)
        }
    }

    @Test
    fun requestedPermissionFragmentDoesntShowIfNoRequestedPermissions() {
        val app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = false,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = emptyArray()
        )
        val scenario = launchActivity(app)

        onView(withId(R.id.permissions_info)).perform(click())
        scenario.onActivity {
            assertThat(
                it.supportFragmentManager.findFragmentByTag("RequestedPermissionsDialog")
            ).isNull()
        }
    }

    @Test
    fun appInfoHeaderHasCorrectInfo() {
        val app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = false,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = emptyArray()
        )
        launchActivity(app)

        onView(withId(R.id.app_name)).check(matches(withText(app.label)))
        onView(withId(R.id.app_icon)).check { view, noViewFoundException ->
            if (view !is ImageView) {
                throw noViewFoundException
            }
            val drawable = view.drawable
            if (drawable !is BitmapDrawable) {
                throw noViewFoundException
            }
            assertThat(drawable.bitmap.sameAs(appIcon?.bitmap)).isTrue()
        }
    }

    @Test
    fun permissionInfoTextCorrect() {
        var app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = false,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = emptyArray()
        )
        var scenario = launchActivity(app)
        onView(withId(R.id.top_line))
            .check(matches(withText(R.string.app_info_requested_permissions_title)))
        onView(withId(R.id.bottom_line))
            .check(matches(withText(R.string.app_info_requested_permissions_none)))
        scenario.moveToState(Lifecycle.State.DESTROYED)

        app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = false,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = arrayOf(Manifest.permission.INTERNET)
        )
        scenario = launchActivity(app)
        var expectedBottomLine = ApplicationProvider.getApplicationContext<Application>().resources
            .getQuantityString(
                R.plurals.app_info_requested_permissions_count,
                app.requestedPermissions.count(),
                app.requestedPermissions.count()
            )
        onView(withId(R.id.top_line))
            .check(matches(withText(R.string.app_info_requested_permissions_title)))
        onView(withId(R.id.bottom_line))
            .check(matches(withText(expectedBottomLine)))
        scenario.moveToState(Lifecycle.State.DESTROYED)

        app = createApp(
            appIcon,
            isSystemApp = false,
            hasLaunchActivity = false,
            lastUpdateTime = System.currentTimeMillis(),
            requestedPermissions = arrayOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NOTIFICATION_POLICY
            )
        )
        launchActivity(app)
        expectedBottomLine = ApplicationProvider.getApplicationContext<Application>().resources
            .getQuantityString(
                R.plurals.app_info_requested_permissions_count,
                app.requestedPermissions.count(),
                app.requestedPermissions.count()
            )
        onView(withId(R.id.top_line))
            .check(matches(withText(R.string.app_info_requested_permissions_title)))
        onView(withId(R.id.bottom_line))
            .check(matches(withText(expectedBottomLine)))
    }

    private fun launchActivity(app: App): ActivityScenario<AppInfoActivity> {
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            AppInfoActivity::class.java
        ).apply {
            putExtra(EXTRA_APP_INFO, app)
        }
        return launchActivity(intent)
    }

    private fun createApp(
        icon: SerializableBitmap?,
        isSystemApp: Boolean,
        hasLaunchActivity: Boolean,
        lastUpdateTime: Long,
        requestedPermissions: Array<String>
    ): App {
        return App(
            icon,
            "v1.0.0",
            "com.dummy.app",
            "Dummy App 1",
            isSystemApp,
            hasLaunchActivity,
            System.currentTimeMillis(),
            lastUpdateTime,
            requestedPermissions
        )
    }
}
