package com.boswelja.devicemanager.messages.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.boswelja.devicemanager.common.SwipeDismissCallback
import com.boswelja.devicemanager.databinding.FragmentMessagesBinding
import com.boswelja.devicemanager.messages.Message
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class MessagesFragment : Fragment() {

    private val viewModel: MessagesViewModel by viewModels()
    private val adapter by lazy {
        MessagesAdapter { messageAction ->
            when (messageAction) {
                Message.Action.LAUNCH_NOTIFICATION_SETTINGS -> TODO()
                Message.Action.LAUNCH_PLAY_STORE -> TODO()
                Message.Action.LAUNCH_CHANGELOG -> TODO()
                Message.Action.INSTALL_UPDATE -> TODO()
            }
        }
    }
    private val swipeDismissCallback = ItemTouchHelper(
        SwipeDismissCallback { position ->
            adapter.peek(position)?.let { message ->
                viewModel.dismissMessage(message.id)
                adapter.notifyItemRemoved(position)
            }
        }
    )

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

        setupRecyclerView()
        observeMessages()
        observeLoadState()

        binding.messageHistoryButton.setOnClickListener {
            findNavController().navigate(MessagesFragmentDirections.toMessageHistoryActivity())
        }
    }

    private fun setupRecyclerView() {
        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        swipeDismissCallback.attachToRecyclerView(binding.messagesRecyclerView)
    }

    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activeMessagesPager.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    private fun observeLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                Timber.d("isLoading = $isLoading")
                binding.progressHorizontal.isVisible = isLoading
                binding.noMessagesView.isVisible = !isLoading && adapter.itemCount <= 0
            }
        }
    }
}
