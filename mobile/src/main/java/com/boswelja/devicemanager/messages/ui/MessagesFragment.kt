package com.boswelja.devicemanager.messages.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.boswelja.devicemanager.databinding.FragmentMessagesBinding
import com.boswelja.devicemanager.messages.Message
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MessagesFragment : Fragment() {

    private val viewModel: MessagesViewModel by viewModels()
    private val adapter by lazy {
        MessagesAdapter { messageAction ->
            when (messageAction) {
                Message.Action.LAUNCH_NOTIFICATION_SETTINGS -> TODO()
                Message.Action.LAUNCH_PLAY_STORE -> TODO()
                Message.Action.LAUNCH_CHANGELOG -> TODO()
            }
        }
    }

    private lateinit var binding: FragmentMessagesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.messagesView.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activeMessagesPager.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun setHasMessages(hasMessages: Boolean) {
        binding.apply {
            if (hasMessages) {
                messagesView.visibility = View.VISIBLE
                noMessagesView.visibility = View.GONE
            } else {
                messagesView.visibility = View.GONE
                noMessagesView.visibility = View.VISIBLE
            }
        }
    }
}
