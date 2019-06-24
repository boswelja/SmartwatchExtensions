/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.messages

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
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
        sharedPreferences.edit().putInt(MESSAGE_COUNT_KEY, recyclerView.adapter!!.itemCount).apply()
    }

    private fun checkBatteryOptimisation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !sharedPreferences.getBoolean(IGNORE_BATTERY_OPT_WARNING_KEY, false)) {
            val isIgnoringBatteryOptimisation = (context?.getSystemService(Context.POWER_SERVICE) as PowerManager)
                    .isIgnoringBatteryOptimizations(context?.packageName!!)
            if (isIgnoringBatteryOptimisation) {
                (recyclerView.adapter as MessagesAdapter).dismissMessage(Message.BatteryOptWarning)
            } else {
                (recyclerView.adapter as MessagesAdapter).notifyMessage(Message.BatteryOptWarning)
            }
        }
    }

    internal fun dismissMessage(message: Message) {
        when (message) {
            Message.BatteryOptWarning ->
                sharedPreferences.edit().putBoolean(IGNORE_BATTERY_OPT_WARNING_KEY, true).apply()
        }
        Snackbar.make(view!!,
                getString(R.string.message_snackbar_undo_remove).format(getString(message.shortLabelRes)),
                Snackbar.LENGTH_INDEFINITE).apply {
            setAction(R.string.snackbar_action_undo) {
                when (message) {
                    Message.BatteryOptWarning -> {
                        sharedPreferences.edit().putBoolean(IGNORE_BATTERY_OPT_WARNING_KEY, false)
                                .apply()
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

    companion object {
        const val MESSAGE_COUNT_KEY = "message_count"

        fun updateMessageCount(context: Context) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            var messages = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !sharedPreferences.getBoolean(IGNORE_BATTERY_OPT_WARNING_KEY, false)) {
                val isIgnoringBatteryOptimisation = (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                        .isIgnoringBatteryOptimizations(context.packageName!!)
                if (!isIgnoringBatteryOptimisation) {
                    messages += 1
                }
            }
            sharedPreferences.edit()
                    .putInt(MESSAGE_COUNT_KEY, messages)
                    .apply()
        }

        private const val IGNORE_BATTERY_OPT_WARNING_KEY = "ignore_battery_opt_warning"
    }
}
