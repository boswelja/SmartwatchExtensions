package com.boswelja.devicemanager.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.boswelja.devicemanager.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, SettingsFragment())
                .commit()
    }
}