package com.boswelja.devicemanager.main.ui

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.navigation.findNavController
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

  @Before
  fun setUp() {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
  }

  @Test
  fun testSetupOpensIfNoPhoneID() {
    sharedPreferences.edit(commit = true) { remove(References.PHONE_ID_KEY) }
    val scenario = getScenario()
    scenario.onActivity {
      assertThat(it.findNavController(R.id.nav_host_fragment).currentDestination?.id).isEqualTo(R.id.setupFragment)
    }
    scenario.close()
  }

  @Test
  fun testExtensionsOpenIfHasPhoneID() {
    sharedPreferences.edit(commit = true) { putString(References.PHONE_ID_KEY, "id123") }
    val scenario = getScenario()
    scenario.onActivity {
      assertThat(it.findNavController(R.id.nav_host_fragment).currentDestination?.id).isEqualTo(R.id.extensionsFragment)
    }
    scenario.close()
  }

  private fun getScenario() = launchActivity<MainActivity>()
}