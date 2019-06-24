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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar

class MessageFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var recyclerView: RecyclerView
    private lateinit var noMessagesView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        noMessagesView = view.findViewById(R.id.no_messages_view)
        recyclerView = view.findViewById(R.id.messages_recyclerview)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MessagesAdapter(this@MessageFragment)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        val itemTouchHelper = ItemTouchHelper(MessagesAdapter.SwipeDismissCallback(recyclerView.adapter as MessagesAdapter, context!!))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        checkBatteryOptimisation()
        (activity as MainActivity).updateMessagesBadge()
    }

    private fun checkBatteryOptimisation() {
        val shouldShowMessage = MessageChecker.shouldShowBatteryOptMessage(context!!)
        if (shouldShowMessage) {
            (recyclerView.adapter as MessagesAdapter).notifyMessage(Message.BatteryOptWarning)
        } else {
            (recyclerView.adapter as MessagesAdapter).dismissMessage(Message.BatteryOptWarning)
        }
    }

    internal fun dismissMessage(message: Message) {
        when (message) {
            Message.BatteryOptWarning ->
                MessageChecker.setIgnoreBatteryOpt(context!!, true)
        }
        Snackbar.make(view!!,
                getString(R.string.message_snackbar_undo_remove).format(getString(message.shortLabelRes)),
                Snackbar.LENGTH_INDEFINITE).apply {
            setAction(R.string.snackbar_action_undo) {
                when (message) {
                    Message.BatteryOptWarning -> {
                        MessageChecker.setIgnoreBatteryOpt(context, false)
                        checkBatteryOptimisation()
                    }
                }
            }
        }.show()
    }

    internal fun setHasMessages(hasMessages: Boolean) {
        noMessagesView.apply {
            visibility = if (hasMessages) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}
