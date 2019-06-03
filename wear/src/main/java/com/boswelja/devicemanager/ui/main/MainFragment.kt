/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.Utils
import com.boswelja.devicemanager.ui.base.BaseSharedPreferenceFragment

class MainFragment : BaseSharedPreferenceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!sharedPreferences.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)) {
            addFragment(LockPhoneFragment())
            addFragment(BatterySyncFragment())
            padContentTop()
        } else {
            addFragment(BatterySyncFragment())
            addFragment(LockPhoneFragment())
            padContentBottom()
        }
    }

    private fun addFragment(fragment: Fragment) {
        fragmentManager?.beginTransaction()!!
                .add(R.id.content, fragment)
                .commit()
    }

    private fun padContentTop() {
        view?.findViewById<LinearLayout>(R.id.content)!!.apply {
            setPadding(0, Utils.complexTypeDp(resources, 72.0f).toInt(), 0, 0)
        }
    }

    private fun padContentBottom() {
        view?.findViewById<LinearLayout>(R.id.content)!!.apply {
            setPadding(0, 0, 0, Utils.complexTypeDp(resources, 72.0f).toInt())
        }
    }
}
