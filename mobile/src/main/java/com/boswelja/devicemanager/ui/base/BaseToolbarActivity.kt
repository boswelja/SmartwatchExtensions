package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.SettingsFragment.Companion.SWITCH_DAYNIGHT_MODE_KEY

abstract class BaseToolbarActivity : AppCompatActivity() {

    abstract fun getContentViewId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(PreferenceManager.getDefaultSharedPreferences(this).getInt(SWITCH_DAYNIGHT_MODE_KEY, AppCompatDelegate.MODE_NIGHT_NO))

        super.onCreate(savedInstanceState)

        setContentView(getContentViewId())

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_search -> {
                true
            }
            R.id.menu_help -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}