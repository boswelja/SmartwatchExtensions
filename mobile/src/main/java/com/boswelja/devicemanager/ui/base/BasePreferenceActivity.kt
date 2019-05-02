package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.main.MainActivity.Companion.EXTRA_PREFERENCE_KEY

abstract class BasePreferenceActivity : BaseToolbarActivity() {

    private lateinit var preferenceFragment: BasePreferenceFragment

    abstract fun createPreferenceFragment() : BasePreferenceFragment
    abstract fun createWidgetFragment() : Fragment?

    override fun getContentViewId(): Int = R.layout.activity_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        preferenceFragment = createPreferenceFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, preferenceFragment)
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

    override fun onResume() {
        super.onResume()
        if (intent != null && !intent.getStringExtra(EXTRA_PREFERENCE_KEY).isNullOrEmpty()) {
            val key = intent.getStringExtra(EXTRA_PREFERENCE_KEY)
            preferenceFragment.scrollToPreference(key)
        }
    }
}