package com.boswelja.devicemanager.donate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.BottomSheetListBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

class DonateSheet : BottomSheetDialogFragment() {

  private val viewModel: DonateViewModel by viewModels()
  private val adapter by lazy { DonateAdapter {
    viewModel.launchBillingFlow(requireActivity(), it)
  }}

  private lateinit var binding: BottomSheetListBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = BottomSheetListBinding.inflate(inflater, container, false)
    binding.title.setText(R.string.donate_title)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.skus.observe(viewLifecycleOwner) {
      if (it != null) adapter.submitList(it)
      else Timber.w("SKU list null")
    }
    viewModel.clientConnected.observe(viewLifecycleOwner) {
      Timber.i("Billing client connected: $it")
    }
  }
}