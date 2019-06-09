package com.boswelja.devicemanager.ui.main.messages

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.boswelja.devicemanager.R
import com.google.android.material.snackbar.Snackbar

class MessageFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view as RecyclerView
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MessagesAdapter(this@MessageFragment)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
        val itemTouchHelper = ItemTouchHelper(MessagesAdapter.SwipeDismissCallback(recyclerView.adapter as MessagesAdapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        checkBatteryOptimisation()
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
                getString(R.string.message_snackbar_undo_remove)
                        .format(getString(message.shortLabelRes)),
                Snackbar.LENGTH_LONG).apply {
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

    companion object {
        private const val IGNORE_BATTERY_OPT_WARNING_KEY = "ignore_battery_opt_warning"
    }
}