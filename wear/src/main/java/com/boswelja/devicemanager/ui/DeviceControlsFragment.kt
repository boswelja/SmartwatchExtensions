package com.boswelja.devicemanager.ui

import android.app.Fragment
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.wear.widget.WearableLinearLayoutManager
import android.support.wear.widget.WearableRecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.boswelja.devicemanager.common.Config
import com.boswelja.devicemanager.MainOption
import com.boswelja.devicemanager.R

class DeviceControlsFragment: Fragment() {

    private var recyclerView: WearableRecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_device_controls, container, false)
        recyclerView = view?.findViewById(R.id.recycler_view)
        recyclerView?.layoutManager = WearableLinearLayoutManager(context, CustomScrollingLayoutCallback())
        recyclerView?.isEdgeItemsCenteringEnabled = true
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val optionsList = ArrayList<MainOption>()
        optionsList.add(MainOption(R.drawable.ic_phonelink_lock, getString(R.string.lock_phone_label), Config.TYPE_LOCK_PHONE))
        recyclerView?.adapter = MainAdapter(optionsList)
    }

    private class CustomScrollingLayoutCallback: WearableLinearLayoutManager.LayoutCallback() {

        private val MAX_ICON_PROGRESS = 0.7f
        private var mProgressToCenter: Float = 0.0f
        override fun onLayoutFinished(child: View?, parent: RecyclerView?) {
            // Figure out % progress from top to bottom
            val centerOffset = child?.height?.toFloat()!! / 2.0f / parent?.height?.toFloat()!!
            val yRelativeToCenterOffset = child.y / parent.height + centerOffset

            // Normalize for center
            mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset)
            // Adjust to the maximum scale
            mProgressToCenter = Math.min(mProgressToCenter, MAX_ICON_PROGRESS)

            child.scaleX = 1 - mProgressToCenter
            child.scaleY = 1 - mProgressToCenter
        }

    }
}