package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.databinding.FragmentAppManagerErrorBinding

class ErrorFragment : Fragment() {

    private val viewModel: AppManagerViewModel by activityViewModels()

    private lateinit var binding: FragmentAppManagerErrorBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppManagerErrorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.retryButton.setOnClickListener {
            viewModel.startAppManagerService()
        }
    }
}
