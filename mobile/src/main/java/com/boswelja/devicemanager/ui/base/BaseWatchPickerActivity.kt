package com.boswelja.devicemanager.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.edit
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.References.CONNECTED_WATCH_ID_KEY
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

abstract class BaseWatchPickerActivity :
        BaseToolbarActivity(),
        AdapterView.OnItemSelectedListener {

    var connectedWatchId: String? = null

    private lateinit var watchPickerSpinner: AppCompatSpinner

    override fun onItemSelected(adapterView: AdapterView<*>?, selectedView: View?, position: Int, id: Long) {
        connectedWatchId = id.toString(36)
        sharedPreferences.edit {
            putString(CONNECTED_WATCH_ID_KEY, connectedWatchId)
            apply()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        watchPickerSpinner = findViewById<AppCompatSpinner>(R.id.watch_picker_spinner).apply {
            onItemSelectedListener = this@BaseWatchPickerActivity
            adapter = WatchPickerAdapter(this@BaseWatchPickerActivity)
        }

        connectedWatchId = sharedPreferences.getString(CONNECTED_WATCH_ID_KEY, "")

        loadConnectedWatches()
    }

    private fun loadConnectedWatches() {
        Wearable.getNodeClient(this)
                .connectedNodes
                .addOnSuccessListener { allConnectedNodes ->
                    Wearable.getCapabilityClient(this)
                            .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_ALL)
                            .addOnSuccessListener {
                                val allCapableNodes = it.nodes
                                for (watch in allConnectedNodes) {
                                    val index = (watchPickerSpinner.adapter as WatchPickerAdapter).add(Watch(watch, hasApp = allCapableNodes.contains(watch)))
                                    if (watch.id == connectedWatchId) {
                                        watchPickerSpinner.setSelection(index)
                                    }
                                }
                                (watchPickerSpinner.adapter as WatchPickerAdapter).notifyDataSetChanged()
                            }
                }
    }

    class Watch(val displayName: String, val id: String, val hasApp: Boolean) {
        constructor(node: Node, hasApp: Boolean) : this(node.displayName, node.id, hasApp)
    }

    class WatchPickerAdapter(context: Context, private val watches: ArrayList<Watch>) : ArrayAdapter<Watch>(context, 0) {

        constructor(context: Context) : this(context, ArrayList<Watch>())

        private val layoutInflater = LayoutInflater.from(context)
        private val watchConnectedString = context.getString(R.string.watch_status_connected)
        private val watchMissingAppString = context.getString(R.string.watch_status_missing_app)

        override fun getCount(): Int {
            return watches.count()
        }

        override fun getItemId(position: Int): Long {
            return watches[position].id.toLong(36)
        }

        fun add(newWatch: Watch): Int {
            watches.add(newWatch)
            return watches.indexOf(newWatch)
        }

        override fun addAll(collection: MutableCollection<out Watch>) {
            watches.addAll(collection)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        private fun getItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            val watch = watches[position]
            var view = convertView
            if (view == null) {
                view = layoutInflater.inflate(R.layout.common_spinner_item_two_line, parent, false)
            }
            view!!.findViewById<AppCompatTextView>(R.id.title).text = watches[position].displayName
            view.findViewById<AppCompatTextView>(R.id.subtitle).text = if (watch.hasApp) {
                 watchConnectedString
            } else {
                watchMissingAppString
            }
            return view
        }
    }
}
