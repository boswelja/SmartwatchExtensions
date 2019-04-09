package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import com.boswelja.devicemanager.R

abstract class BasePreferenceActivity : BaseToolbarActivity() {

    abstract fun createPreferenceFragment() : PreferenceFragmentCompat
    abstract fun createWidgetFragment() : Fragment?

    override fun getContentViewId(): Int {
        return R.layout.activity_settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, createPreferenceFragment())
                .commit()

        val widgetFragment = createWidgetFragment()
        if (widgetFragment != null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.widget_holder, widgetFragment)
                    .commit()
        } else {
            findViewById<View>(R.id.widget_divider).visibility = View.GONE
        }
    }
}