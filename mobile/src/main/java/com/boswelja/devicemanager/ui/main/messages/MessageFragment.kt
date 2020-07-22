/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.messages

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.FragmentMessagesBinding
import com.boswelja.devicemanager.messages.Message
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MessageFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentMessagesBinding

    private var messageDatabase: MessageDatabase? = null

    private val coroutineScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        messageDatabase = MessageDatabase.open(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated() called")
        binding.messagesRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MessagesAdapter(this@MessageFragment)
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).also {
                addItemDecoration(it)
            }
        }.also {
            ItemTouchHelper(
                MessagesAdapter.SwipeDismissCallback(
                    it.adapter as MessagesAdapter,
                    requireContext()
                )
            ).apply {
                attachToRecyclerView(it)
            }
        }

        refreshMessages()
    }

    /**
     * Undo dismissing a [Message]. Handles database operations as well as updating the UI.
     * @param message The [Message] to un-dismiss.
     */
    private fun undoDismissMessage(message: Message) {
        Timber.i("Restoring message ${message.id}")
        coroutineScope.launch(Dispatchers.IO) {
            messageDatabase?.restoreMessage(sharedPreferences, message)
            withContext(Dispatchers.Main) {
                (binding.messagesRecyclerview.adapter as MessagesAdapter).notifyMessage(message)
                setHasMessages(messageDatabase!!.countMessages() > 0)
                (activity as MainActivity).updateMessagesBadge()
            }
        }
    }

    /**
     * Refresh the message list entirely.
     */
    private fun refreshMessages() {
        coroutineScope.launch(Dispatchers.IO) {
            val messages = messageDatabase?.getActiveMessages()
            withContext(Dispatchers.Main) {
                if (!messages.isNullOrEmpty()) {
                    for (message in messages) {
                        (binding.messagesRecyclerview.adapter as MessagesAdapter)
                            .notifyMessage(message)
                    }
                    setHasMessages(true)
                } else {
                    setHasMessages(false)
                }
            }
        }
    }

    /**
     * Dismiss a [Message].
     * @param message The [Message] to dismiss.
     */
    internal fun dismissMessage(message: Message) {
        coroutineScope.launch(Dispatchers.IO) {
            messageDatabase?.deleteMessage(sharedPreferences, message)
            withContext(Dispatchers.Main) {
                setHasMessages(messageDatabase!!.countMessages() > 0)
                (activity as MainActivity).updateMessagesBadge()
            }
        }
        Snackbar.make(
            requireView(),
            getString(R.string.message_snackbar_undo_remove),
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(R.string.snackbar_action_undo) {
                undoDismissMessage(message)
            }
        }.show()
    }

    /**
     * Sets whether the UI should show the message list, or a no message view.
     * @param hasMessages Whether there are messages to show.
     */
    internal fun setHasMessages(hasMessages: Boolean) {
        binding.noMessagesView.apply {
            visibility = if (hasMessages) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}
