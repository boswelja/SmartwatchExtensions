/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.main.messages

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.boswelja.devicemanager.databinding.FragmentMessagesBinding
import com.boswelja.devicemanager.messages.Action
import com.boswelja.devicemanager.messages.database.MessageDatabase
import com.boswelja.devicemanager.ui.changelog.ChangelogDialogFragment
import timber.log.Timber

@SuppressLint("BatteryLife")
class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessagesBinding

    private val messageDatabase by lazy { MessageDatabase.get(requireContext()) }
    private val adapter by lazy {
        MessagesAdapter {
            when (it.action) {
                Action.DISABLE_BATTERY_OPTIMISATION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context?.packageName}")
                        }.also { intent ->
                            Timber.i("Requesting ignore battery optimisation")
                            context?.startActivity(intent)
                        }
                    }
                }
                Action.LAUNCH_NOTIFICATION_SETTINGS -> {
                    Intent().apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                        } else {
                            action = "android.settings.APP_NOTIFICATION_SETTINGS"
                            putExtra("app_package", context?.packageName)
                            putExtra("app_uid", context?.applicationInfo?.uid)
                        }
                    }.also { intent ->
                        Timber.i("Launching notification settings")
                        context?.startActivity(intent)
                    }
                }
                Action.SHOW_CHANGELOG -> {
                    ChangelogDialogFragment().show(parentFragmentManager)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.d("onViewCreated() called")
        binding.messagesRecyclerview.apply {
            adapter = this@MessageFragment.adapter
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).also {
                addItemDecoration(it)
            }
        }.also {
            ItemTouchHelper(
                MessageTouchCallback(
                    adapter,
                    requireContext()
                )
            ).apply {
                attachToRecyclerView(it)
            }
        }

        messageDatabase.messageDao().getActiveMessagesObservable().observe(viewLifecycleOwner) {
            adapter.submitList(it)
            setHasMessages(it.isNotEmpty())
        }
    }

    /**
     * Sets whether the UI should show the message list, or a no message view.
     * @param hasMessages Whether there are messages to show.
     */
    private fun setHasMessages(hasMessages: Boolean) {
        binding.noMessagesView.apply {
            visibility = if (hasMessages) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }
}
