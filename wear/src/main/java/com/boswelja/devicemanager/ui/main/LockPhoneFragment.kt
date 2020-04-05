/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.PreferenceKey
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH
import com.boswelja.devicemanager.databinding.FragmentLockPhoneBinding
import com.boswelja.devicemanager.service.ActionService
import com.boswelja.devicemanager.ui.base.BaseSharedPreferenceFragment

class LockPhoneFragment : BaseSharedPreferenceFragment() {

    private val lockPhoneEnabledChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            PreferenceKey.PHONE_LOCKING_ENABLED_KEY -> updatePhoneLockingView()
        }
    }

    private lateinit var binding: FragmentLockPhoneBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentLockPhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.phoneLockingView.setOnClickListener {
            val intent = Intent(context, ActionService::class.java)
            intent.putExtra(ActionService.EXTRA_ACTION, LOCK_PHONE_PATH)
            context?.startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updatePhoneLockingView()
        sharedPreferences.registerOnSharedPreferenceChangeListener(lockPhoneEnabledChangeListener)
    }

    override fun onPause() {
        super.onPause()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(lockPhoneEnabledChangeListener)
    }

    private fun updatePhoneLockingView() {
        val phoneLockingEnabled = sharedPreferences.getBoolean(PreferenceKey.PHONE_LOCKING_ENABLED_KEY, false)
        binding.phoneLockingText.text = if (phoneLockingEnabled) {
            getString(R.string.lock_phone_label)
        } else {
            getString(R.string.lock_phone_disabled_message)
        }
    }
}
