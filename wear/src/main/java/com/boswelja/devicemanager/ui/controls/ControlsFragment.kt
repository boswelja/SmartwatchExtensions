package com.boswelja.devicemanager.ui.controls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.boswelja.devicemanager.R

class ControlsFragment : Fragment() {

    private lateinit var recyclerView: WearableRecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_controls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<WearableRecyclerView>(R.id.recycler_view).apply {
            layoutManager = WearableLinearLayoutManager(context, CustomScrollingLayoutCallback())
            isEdgeItemsCenteringEnabled = true
            adapter = ControlsAdapter()
        }
        PagerSnapHelper().attachToRecyclerView(recyclerView)
    }

    class CustomScrollingLayoutCallback : WearableLinearLayoutManager.LayoutCallback() {

        private var mProgressToCenter: Float = 0f

        override fun onLayoutFinished(child: View, parent: RecyclerView) {
            child.apply {
                val centerOffset = height.toFloat() / 2.0f / parent.height.toFloat()
                val yRelativeToCenterOffset = y / parent.height + centerOffset

                mProgressToCenter = Math.abs(0.5f - yRelativeToCenterOffset)
                mProgressToCenter = Math.min(mProgressToCenter, 0.65f)

                scaleX = 1 - mProgressToCenter
                scaleY = 1 - mProgressToCenter
            }
        }
    }
}