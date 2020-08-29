package com.boswelja.devicemanager.main.ui

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.phoneconnectionmanager.References
import com.google.common.truth.Truth.assertThat
import org.junit.Before

import org.junit.Test

class MainActivityTest {

  private lateinit var sharedPreferences: SharedPreferences
  private lateinit var navController: TestNavHostController

  @Before
  fun setUp() {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
    navController = TestNavHostController(ApplicationProvider.getApplicationContext())
  }

  @Test
  fun testSetupOpensIfNoPhoneID() {
    sharedPreferences.edit(commit = true) { remove(References.PHONE_ID_KEY) }
    val scenario = getScenario()
    assertThat(navController.currentDestination?.id).isEqualTo(R.id.setupFragment)
    scenario.close()
  }

  @Test
  fun testExtensionsOpenIfHasPhoneID() {
    sharedPreferences.edit(commit = true) { putString(References.PHONE_ID_KEY, "id123") }
    val scenario = getScenario()
    assertThat(navController.currentDestination?.id).isEqualTo(R.id.extensionsFragment)
    scenario.close()
  }

  private fun getScenario() = launchActivity<MainActivity>().apply {
    onActivity {
      navController.setGraph(R.navigation.main_graph)
      Navigation.setViewNavController(it.requireViewById(R.id.nav_host_fragment), navController)
    }
    moveToState(Lifecycle.State.STARTED)
  }
}