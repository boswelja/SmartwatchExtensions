/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.navigation

import android.graphics.drawable.Drawable
import androidx.wear.widget.drawer.WearableNavigationDrawerView
import com.boswelja.devicemanager.ui.MainActivity

class NavigationDrawerAdapter(private val mainActivity: MainActivity) : WearableNavigationDrawerView.WearableNavigationDrawerAdapter() {

    private val sections = NavigationDrawerSections.values()

    override fun getCount(): Int = sections.size

    override fun getItemDrawable(pos: Int): Drawable {
        val item = sections[pos]
        return mainActivity.getDrawable(item.drawableRes)!!
    }

    override fun getItemText(pos: Int): CharSequence {
        val item = sections[pos]
        return mainActivity.getString(item.titleRes)
    }
}
