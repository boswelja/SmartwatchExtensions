package com.boswelja.devicemanager.messages.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.databinding.FragmentMessagesBinding

class MessagesFragment : Fragment() {

    private val viewModel: MessagesViewModel by viewModels()

    private lateinit var binding: FragmentMessagesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }
}
