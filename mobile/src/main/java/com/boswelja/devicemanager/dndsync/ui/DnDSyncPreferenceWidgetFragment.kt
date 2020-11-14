package com.boswelja.devicemanager.dndsync.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.databinding.SettingsWidgetDndSyncBinding

class DnDSyncPreferenceWidgetFragment : Fragment() {

    private val viewModel: DnDSyncPreferenceWidgetViewModel by viewModels()

    private lateinit var binding: SettingsWidgetDndSyncBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsWidgetDndSyncBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.drawableRes.observe(viewLifecycleOwner) {
            binding.dndSyncStatusIndicator.setImageResource(it)
        }
    }
}
