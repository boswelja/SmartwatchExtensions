/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.boswelja.devicemanager.MainOption
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.DnDLocalChangeListener
import com.boswelja.devicemanager.common.References
import com.google.android.gms.wearable.Node

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: WearableRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        val optionsList = ArrayList<MainOption>()
        optionsList.add(MainOption(R.drawable.ic_phonelink_lock, getString(R.string.lock_phone_label), References.TYPE_LOCK_PHONE))
        optionsList.add(MainOption(R.drawable.ic_phone_battery, getString(R.string.phone_battery_unknown_long), References.TYPE_PHONE_BATTERY))
        recyclerView.apply {
            layoutManager = WearableLinearLayoutManager(context, CustomScrollingLayoutCallback())
            isEdgeItemsCenteringEnabled = true
            adapter = MainAdapter(optionsList)
        }
        PagerSnapHelper().attachToRecyclerView(recyclerView)

        Log.d("MainActivity", ComponentName(this, DnDLocalChangeListener::class.java).flattenToString())
    }

    override fun onResume() {
        super.onResume()
        checkForCompanion()
    }

    private fun checkForCompanion() {
        val capabilityCallbacks = object : Utils.CapabilityCallbacks {
            override fun capableDeviceFound(node: Node?) {}

            override fun noCapableDevices() {
                showInstallAppActivity()
            }
        }
        Utils.isCompanionAppInstalled(this, capabilityCallbacks)
    }

    private fun showInstallAppActivity() {
        val intent = Intent(this, ConfirmInstallActivity::class.java)
        startActivity(intent)
        finish()
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
