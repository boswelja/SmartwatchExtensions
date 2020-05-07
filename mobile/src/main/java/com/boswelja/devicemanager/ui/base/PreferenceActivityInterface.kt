/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
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
