/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.content.SharedPreferences
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

    private var batterySyncFragment: BatterySyncFragment? = null
    private var lockPhoneFragment: LockPhoneFragment? = null

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == PreferenceKey.BATTERY_SYNC_ENABLED_KEY) {
            recreateView()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recreateView()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun addFragment(fragment: Fragment) {
        fragmentManager?.beginTransaction()!!
                .setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                .add(R.id.content, fragment)
                .commit()
    }

    private fun padContentTop(clearOtherPadding: Boolean) {
        val paddingDp = Utils.complexTypeDp(resources, 72.0f).toInt()
        view?.findViewById<LinearLayout>(R.id.content)!!.apply {
            if (clearOtherPadding) {
                setPadding(0, paddingDp, 0, 0)
            } else {
                setPadding(paddingLeft, paddingDp, paddingRight, paddingBottom)
            }
        }
    }

    private fun padContentBottom(clearOtherPadding: Boolean) {
        val paddingDp = Utils.complexTypeDp(resources, 72.0f).toInt()
        view?.findViewById<LinearLayout>(R.id.content)!!.apply {
            if (clearOtherPadding) {
                setPadding(0, 0, 0, paddingDp)
            } else {
                setPadding(paddingLeft, paddingTop, paddingRight, paddingDp)
            }
        }
    }

    private fun recreateView() {
        if (batterySyncFragment != null &&
                lockPhoneFragment != null) {
            fragmentManager?.beginTransaction()!!
                    .setCustomAnimations(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                    .remove(batterySyncFragment!!)
                    .remove(lockPhoneFragment!!)
                    .commit()
        }
        batterySyncFragment = BatterySyncFragment()
        lockPhoneFragment = LockPhoneFragment()
        if (!sharedPreferences.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false)) {
            padContentTop(false)
            padContentBottom(false)
            addFragment(lockPhoneFragment!!)
            addFragment(batterySyncFragment!!)
        } else {
            padContentBottom(true)
            addFragment(batterySyncFragment!!)
            addFragment(lockPhoneFragment!!)
        }
    }
}
