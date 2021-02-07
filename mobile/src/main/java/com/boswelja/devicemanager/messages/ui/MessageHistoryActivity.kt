package com.boswelja.devicemanager.messages.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityMessageHistoryBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class MessageHistoryActivity : BaseToolbarActivity() {

    private val viewModel: MessageHistoryViewModel by viewModels()
    private val adapter: MessagesAdapter by lazy { MessagesAdapter(false) { } }

    private lateinit var binding: ActivityMessageHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMessageHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(
            binding.toolbarLayout.toolbar,
            showTitle = true,
            showUpButton = true
        )

        setupRecyclerView()
        observeMessages()
        observeLoadState()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_history_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                viewModel.clearMessageHistory()
                Snackbar.make(
                    binding.root,
                    R.string.message_history_cleared,
                    Snackbar.LENGTH_LONG
                ).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Set up the message [androidx.recyclerview.widget.RecyclerView].
     */
    private fun setupRecyclerView() {
        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    /**
     * Observe dismissed messages in the database and pass them to [adapter].
     */
    private fun observeMessages() {
        lifecycleScope.launch {
            viewModel.dismissedMessagesPager.collectLatest {
                adapter.submitData(it)
            }
        }
    }

    /**
     * Observe the [adapter] loading state and update the UI accordingly.
     */
    private fun observeLoadState() {
        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                val isLoading = loadStates.refresh is LoadState.Loading
                Timber.d("isLoading = $isLoading")
                binding.progressHorizontal.isVisible = isLoading
                binding.noMessagesView.isVisible = !isLoading && adapter.itemCount <= 0
            }
        }
    }
}
