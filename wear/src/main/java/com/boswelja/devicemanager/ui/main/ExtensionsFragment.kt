/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
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
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.ui.base.BaseSharedPreferenceFragment
import com.boswelja.devicemanager.ui.main.shortcuts.AppShortcutsFragment

class ExtensionsFragment : BaseSharedPreferenceFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_extensions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentFragmentManager.beginTransaction().apply {
            if (!sharedPreferences.getBoolean(PreferenceKey.BATTERY_SYNC_ENABLED_KEY, false) or
                    sharedPreferences.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)) {
                add(R.id.extensions_holder, LockPhoneFragment())
                add(R.id.extensions_holder, BatterySyncFragment())
            } else {
                add(R.id.extensions_holder, BatterySyncFragment())
                add(R.id.extensions_holder, LockPhoneFragment())
            }
            add(R.id.extensions_holder, AppShortcutsFragment())
        }.also {
            it.commit()
        }
    }
}
