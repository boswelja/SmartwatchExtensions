package com.boswelja.devicemanager.messages.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.FragmentMessagesBinding
import com.boswelja.devicemanager.messages.Message
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class MessagesFragment : Fragment() {

    private val viewModel: MessagesViewModel by viewModels()
    private val adapter by lazy {
        MessagesAdapter { messageAction ->
            when (messageAction) {
                Message.Action.LAUNCH_NOTIFICATION_SETTINGS -> openNotiSettings()
                Message.Action.LAUNCH_CHANGELOG -> viewModel.showChangelog()
                Message.Action.INSTALL_UPDATE -> viewModel.startUpdateFlow(requireActivity())
            }
        }
    }
    private val swipeDismissCallback by lazy {
        ItemTouchHelper(
            SwipeDismissCallback(requireContext()) { position ->
                adapter.peek(position)?.let { message ->
                    canShowLoading = false
                    viewModel.dismissMessage(message.id)
                    adapter.notifyItemRemoved(position)
                }
            }
        )
    }

    private lateinit var binding: FragmentMessagesBinding
    private var canShowLoading: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        viewModel.messageDismissedEvent.observe(viewLifecycleOwner) {
            showMessageDismissedSnackbar(it!!)
        }
    }

    /**
     * Set up the messages [androidx.recyclerview.widget.RecyclerView].
     */
    private fun setupRecyclerView() {
        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        swipeDismissCallback.attachToRecyclerView(binding.messagesRecyclerView)
    }

    /**
     * Observe active messages in the database and pass them to [adapter].
     */
    private fun observeMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.activeMessagesPager.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    /**
     * Observe [adapter] loading state and update UI accordingly.
     */
    private fun observeLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                Timber.d("isLoading = $isLoading")
                binding.progressHorizontal.isVisible = isLoading && canShowLoading
                binding.noMessagesView.isVisible = !isLoading && adapter.itemCount <= 0
                if (!isLoading) canShowLoading = true
            }
        }
    }

    /**
     * Show a snackbar with an undo action when a message is dismissed.
     */
    private fun showMessageDismissedSnackbar(messageId: Long) {
        Snackbar.make(requireView(), R.string.message_dismissed, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.button_undo) {
                viewModel.restoreMessage(messageId)
            }
            .show()
    }

    /**
     * Open Android's notification settings for Wearable Extensions.
     */
    private fun openNotiSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            }
        } else {
            Intent("android.settings.APP_NOTIFICATION_SETTINGS").apply {
                putExtra("app_package", requireContext().packageName)
                putExtra("app_uid", requireContext().applicationInfo.uid)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
