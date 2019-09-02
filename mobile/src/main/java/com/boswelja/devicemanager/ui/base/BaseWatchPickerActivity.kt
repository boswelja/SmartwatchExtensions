package com.boswelja.devicemanager.ui.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import com.boswelja.devicemanager.R
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable

abstract class BaseWatchPickerActivity :
        BaseToolbarActivity(),
        AdapterView.OnItemSelectedListener {

    var connectedWatchId: String? = null

    override fun onItemSelected(adapterView: AdapterView<*>?, selectedView: View?, position: Int, id: Long) {
        Log.d("onItemSelected", "Selected watch $id at pos $position")

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadConnectedWatches()
    }

    private fun loadConnectedWatches() {
        Wearable.getCapabilityClient(this)
                .getCapability(com.boswelja.devicemanager.common.References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnSuccessListener {
                    Log.d("loadConnectedWatches", "Loading ${it.nodes.size} watches")
                    findViewById<AppCompatSpinner>(R.id.watch_picker_spinner).apply {
                        onItemSelectedListener = this@BaseWatchPickerActivity
                        adapter = WatchPickerAdapter(this@BaseWatchPickerActivity, it.nodes.toTypedArray())
                        setSelection(0)
                    }
                }
    }

    class WatchPickerAdapter(context: Context, private val connectedWatches: Array<Node>) : ArrayAdapter<Node>(context, 0) {

        private val layoutInflater = LayoutInflater.from(context)

        override fun getCount(): Int {
            return connectedWatches.count()
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                getItemView(position, convertView, parent)

        private fun getItemView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = layoutInflater.inflate(R.layout.common_spinner_item_two_line, parent, false)
            }
            view!!.findViewById<AppCompatTextView>(R.id.title).text = connectedWatches[position].displayName
            view!!.findViewById<AppCompatTextView>(R.id.subtitle).text = "Connected"
            return view
        }
    }
}