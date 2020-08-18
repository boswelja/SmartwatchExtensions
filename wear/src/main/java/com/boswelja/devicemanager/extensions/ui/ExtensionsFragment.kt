/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.extensions.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.wear.widget.CurvingLayoutCallback
import androidx.wear.widget.WearableLinearLayoutManager
import com.boswelja.devicemanager.common.References.LOCK_PHONE_PATH
import com.boswelja.devicemanager.common.batterysync.References.REQUEST_BATTERY_UPDATE_PATH
import com.boswelja.devicemanager.databinding.FragmentMainBinding
import com.boswelja.devicemanager.extensions.ui.ExtensionItems.ABOUT_APP_ITEM_ID
import com.boswelja.devicemanager.extensions.ui.ExtensionItems.BATTERY_SYNC_ITEM_ID
import com.boswelja.devicemanager.extensions.ui.ExtensionItems.EXTENSIONS
import com.boswelja.devicemanager.extensions.ui.ExtensionItems.PHONE_LOCKING_ITEM_ID
import com.boswelja.devicemanager.extensions.ui.ExtensionItems.SETTINGS_ITEM_ID
import com.boswelja.devicemanager.extensions.ui.adapter.ExtensionsAdapter
import com.boswelja.devicemanager.service.ActionService
import timber.log.Timber

class ExtensionsFragment : Fragment() {

  private val viewModel: ExtensionsViewModel by viewModels()

  private lateinit var binding: FragmentMainBinding
  private val adapter by lazy {
    ExtensionsAdapter {
      when (it.id) {
        BATTERY_SYNC_ITEM_ID -> tryUpdateBatteryStats()
        PHONE_LOCKING_ITEM_ID -> tryLockPhone()
        SETTINGS_ITEM_ID ->
            findNavController().navigate(ExtensionsFragmentDirections.toSettingsActivity())
        ABOUT_APP_ITEM_ID ->
            findNavController().navigate(ExtensionsFragmentDirections.toAboutActivity())
      }
    }
  }

  override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
  ): View? {
    binding = FragmentMainBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    adapter.submitList(EXTENSIONS)
    binding.recyclerView.apply {
      layoutManager = WearableLinearLayoutManager(context, CurvingLayoutCallback(context))
      isEdgeItemsCenteringEnabled = true
      adapter = this@ExtensionsFragment.adapter
    }
    viewModel.phoneLockingEnabled.observe(viewLifecycleOwner) {
      Timber.i("phoneLockingEnabled: $it")
      setPhoneLockingEnabled(it)
    }
    viewModel.batterySyncEnabled.observe(viewLifecycleOwner) {
      Timber.i("batterySyncEnabled: $it")
      setBatterySyncEnabled(it)
    }
    viewModel.phoneBatteryPercent.observe(viewLifecycleOwner) {
      Timber.i("phoneBatteryPercent: $it")
      setBatteryPercent(it)
    }
  }

  private fun tryUpdateBatteryStats() {
    Intent(context, ActionService::class.java)
        .apply { putExtra(ActionService.EXTRA_ACTION, REQUEST_BATTERY_UPDATE_PATH) }
        .also { context?.startService(it) }
  }

  private fun tryLockPhone() {
    Intent(context, ActionService::class.java)
        .apply { putExtra(ActionService.EXTRA_ACTION, LOCK_PHONE_PATH) }
        .also { context?.startService(it) }
  }

  private fun setBatterySyncEnabled(batterySyncEnabled: Boolean) {
    (adapter.currentList.first { it.id == BATTERY_SYNC_ITEM_ID } as Item.Extension).isEnabled
        .postValue(batterySyncEnabled)
  }

  private fun setBatteryPercent(batteryPercent: Int) {
    (adapter.currentList.first { it.id == BATTERY_SYNC_ITEM_ID } as Item.Extension).extra
        .postValue(batteryPercent)
  }

  private fun setPhoneLockingEnabled(phoneLockingEnabled: Boolean) {
    (adapter.currentList.first { it.id == PHONE_LOCKING_ITEM_ID } as Item.Extension).isEnabled
        .postValue(phoneLockingEnabled)
  }
}
