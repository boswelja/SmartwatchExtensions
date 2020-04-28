/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.base

import android.view.MenuItem
import com.boswelja.devicemanager.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber

/**
 * An activity that extends [BaseDayNightActivity]. This automatically handles setting up a toolbar
 * with optional elevation.
 */
abstract class BaseToolbarActivity : BaseDayNightActivity() {

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
        Snackbar.make(findViewById(R.id.content_view), message, Snackbar.LENGTH_LONG).show()
    }

    /**
     * Set up the [MaterialToolbar].
     * @param toolbar The [MaterialToolbar] to use.
     * @param showTitle Whether the title is shown on the toolbar. The default is false.
     * @param showUpButton Whether the up button, or back button, is shown on the toolbar.
     * The default is false
     * @param toolbarSubtitle The subtitle to show on the toolbar, or null if none.
     * The default is null.
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
