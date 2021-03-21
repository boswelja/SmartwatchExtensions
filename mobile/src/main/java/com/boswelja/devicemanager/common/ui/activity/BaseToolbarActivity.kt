package com.boswelja.devicemanager.common.ui.activity

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

/**
 * An activity that automatically handles setting up a toolbar with optional elevation.
 */
abstract class BaseToolbarActivity : AppCompatActivity() {

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Timber.i("Navigating back")
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Create a [Snackbar] on the base content view.
     * @param message The message to show the [Snackbar] with.
     */
    fun createSnackBar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Set up the [MaterialToolbar].
     * @param toolbar The [MaterialToolbar] to use.
     * @param showTitle Whether the title is shown on the toolbar. The default is false.
     * @param showUpButton Whether the up button, or back button, is shown on the toolbar. The
     * default is false
     * @param toolbarSubtitle The subtitle to show on the toolbar, or null if none. The default is
     * null.
     */
    fun setupToolbar(
        toolbar: MaterialToolbar,
        showTitle: Boolean = false,
        showUpButton: Boolean = false,
        toolbarSubtitle: String? = null
    ) {
        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayShowTitleEnabled(showTitle)
            setDisplayHomeAsUpEnabled(showUpButton)
            subtitle = toolbarSubtitle
        }
    }
}
