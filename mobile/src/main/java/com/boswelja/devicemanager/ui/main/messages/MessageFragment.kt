/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.messages

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
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

class MessageFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: FragmentMessagesBinding

    private var messageDatabase: MessageDatabase? = null

    private val coroutineScope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        messageDatabase = MessageDatabase.open(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_messages, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.messagesRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MessagesAdapter(this@MessageFragment)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }.also {
            val itemTouchHelper = ItemTouchHelper(MessagesAdapter.SwipeDismissCallback(it.adapter as MessagesAdapter, context!!))
            itemTouchHelper.attachToRecyclerView(it)
        }

        refreshMessages()
    }

    internal fun dismissMessage(message: Message) {
        if (messageDatabase != null) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    messageDatabase?.deleteMessage(sharedPreferences, message)
                    withContext(Dispatchers.Main) {
                        setHasMessages(messageDatabase!!.countMessages() > 0)
                    }
                }
            }
        }
        (activity as MainActivity).updateMessagesBadge()
        Snackbar.make(view!!,
                getString(R.string.message_snackbar_undo_remove),
                Snackbar.LENGTH_LONG).apply {
            setAction(R.string.snackbar_action_undo) {
                if (messageDatabase != null) {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            messageDatabase?.restoreMessage(sharedPreferences, message)
                            withContext(Dispatchers.Main) {
                                (binding.messagesRecyclerview.adapter as MessagesAdapter).notifyMessage(message)
                                (activity as MainActivity).updateMessagesBadge()
                            }
                        }
                    }
                }
            }
        }.show()
    }

    internal fun setHasMessages(hasMessages: Boolean) {
        binding.noMessagesView.apply {
            visibility = if (hasMessages) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    private fun refreshMessages() {
        Log.i("MessageFragment", "Refreshing messages")
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val messages = messageDatabase?.getActiveMessages()
                withContext(Dispatchers.Main) {
                    if (!messages.isNullOrEmpty()) {
                        Log.i("MessageFragment", "Found ${messages.count()} messages")

                        for (message in messages) {
                            (binding.messagesRecyclerview.adapter as MessagesAdapter).notifyMessage(message)
                        }
                        setHasMessages(true)
                    } else {
                        Log.i("MessageFragment", "No messages")
                        setHasMessages(false)
                    }
                }
            }
        }
    }
}
