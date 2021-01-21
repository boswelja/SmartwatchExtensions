package com.boswelja.devicemanager.common.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.boswelja.devicemanager.widget.database.WatchBatteryWidgetId
import com.boswelja.devicemanager.widget.database.WatchWidgetAssociation
import com.boswelja.devicemanager.widget.database.WidgetDatabase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockkObject
import org.junit.After
import org.junit.Before
import org.junit.Test

class BaseWidgetConfigActivityTest {

    private val dummyWatchId = "watch-id"
    private val dummyWidgetId = 1234
    private lateinit var dummyIntent: Intent

    private lateinit var widgetDatabase: WidgetDatabase
    private lateinit var scenario: ActivityScenario<ConcreteBaseWidgetConfigActivity>

    @Before
    fun setUp() {
        dummyIntent = Intent(
            ApplicationProvider.getApplicationContext(),
            ConcreteBaseWidgetConfigActivity::class.java
        ).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, dummyWidgetId)
        }

        widgetDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WidgetDatabase::class.java
        ).build()

        mockkObject(WidgetDatabase)
        coEvery { WidgetDatabase.open(any()) } returns widgetDatabase

        scenario = launchActivity(intent = dummyIntent)
    }

    @After
    fun tearDown() {
        widgetDatabase.clearAllTables()
        widgetDatabase.close()
    }

    @Test
    fun finishWithoutConfigReturnsID() {
        scenario.onActivity { it.finish() }
        assertThat(scenario.result.resultCode).isEqualTo(Activity.RESULT_CANCELED)
        assertThat(
            scenario.result.resultData.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                0
            )
        ).isEqualTo(dummyWidgetId)
    }

    @Test
    fun finishWithConfigReturnsOK() {
        checkCommonFinishWithConfig(WatchBatteryWidgetId(dummyWatchId, dummyWidgetId))
    }

    @Test
    fun finishWithWatchBatteryWidgetUpdatesDatabase() {
        val watchWidgetAssociation = WatchBatteryWidgetId(dummyWatchId, dummyWidgetId)
        checkCommonFinishWithConfig(watchWidgetAssociation)
        assertThat(widgetDatabase.watchBatteryWidgetDao().getAll()).contains(watchWidgetAssociation)
    }

    private fun checkCommonFinishWithConfig(config: WatchWidgetAssociation) {
        scenario.onActivity {
            it.finishWidgetConfig(config)
        }
        assertThat(scenario.result.resultCode).isEqualTo(Activity.RESULT_OK)
        assertThat(
            scenario.result.resultData.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                0
            )
        ).isEqualTo(dummyWidgetId)
    }
}
