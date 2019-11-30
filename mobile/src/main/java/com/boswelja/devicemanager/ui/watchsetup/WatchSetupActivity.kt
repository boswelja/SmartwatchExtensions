package com.boswelja.devicemanager.ui.watchsetup

import android.os.Bundle
import androidx.core.content.edit
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity

class WatchSetupActivity : BaseToolbarActivity() {

    private var useFirstFragmentAnimation = true

    override fun getContentViewId(): Int = R.layout.activity_watch_setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_NO_WATCH_ADDED)

        if (sharedPreferences.getBoolean(HAS_COMPLETED_FIRST_RUN_KEY, false)) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            showWatchSetupFragment()
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            showWelcomeFragment()
        }
    }

    private fun showWelcomeFragment() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_holder, WelcomeFragment())
            setCustomAnimations(R.anim.fade_in, R.anim.slide_out_right)
        }.also {
            it.commit()
        }
        useFirstFragmentAnimation = false
    }

    private fun showWatchSetupFragment() {
        supportActionBar?.title = getString(R.string.register_watch_toolbar_title)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_holder, WatchSetupFragment())
            if (useFirstFragmentAnimation) {
                setCustomAnimations(R.anim.fade_in, R.anim.slide_out_left)
                useFirstFragmentAnimation = false
            } else {
                setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }.also {
            it.commit()
        }
    }

    fun setFirstRunFinished() {
        sharedPreferences.edit {
            putBoolean(HAS_COMPLETED_FIRST_RUN_KEY, true)
        }
        showWatchSetupFragment()
    }

    companion object {
        private const val HAS_COMPLETED_FIRST_RUN_KEY = "has_completed_first_run"

        const val RESULT_WATCH_ADDED = 1
        const val RESULT_NO_WATCH_ADDED = 0
    }
}