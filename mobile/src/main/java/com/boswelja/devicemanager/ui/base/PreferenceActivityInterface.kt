package com.boswelja.devicemanager.ui.base

import androidx.fragment.app.Fragment

interface PreferenceActivityInterface {

    /**
     * Get an instance of a class that extends [BasePreferenceFragment] here.
     * Must not be null.
     */
    fun getPreferenceFragment(): BasePreferenceFragment

    /**
     * Get an instance of a class that extends [Fragment] here to be used as a settings widget.
     * Null if you do not need a settings widget.
     */
    fun getWidgetFragment(): Fragment?
}