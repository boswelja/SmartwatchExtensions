/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.References
import com.boswelja.devicemanager.common.appmanager.AppManagerReferences
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable

class AppManagerActivity : BaseToolbarActivity() {

    override fun getContentViewId(): Int = R.layout.activity_app_manager

    private lateinit var messageClient: MessageClient

    private val appManagerFragment = AppManagerFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, appManagerFragment)
                .commit()

        messageClient = Wearable.getMessageClient(this)
        startAppManagerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        Wearable.getCapabilityClient(this)
                .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val node = it.result?.nodes?.firstOrNull { node -> node.isNearby }
                        if (node != null) {
                            messageClient.sendMessage(node.id, AppManagerReferences.STOP_SERVICE, null)
                        }
                    }
                }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_app_manager_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_item_show_system -> {
                item.isChecked = !item.isChecked
                val showSystemApps = item.isChecked
                appManagerFragment.setShowSystemApps(showSystemApps)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun startAppManagerService() {
        Wearable.getCapabilityClient(this)
                .getCapability(References.CAPABILITY_WATCH_APP, CapabilityClient.FILTER_REACHABLE)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val node = it.result?.nodes?.firstOrNull { node -> node.isNearby }
                        if (node != null) {
                            messageClient.sendMessage(node.id, AppManagerReferences.START_SERVICE, null)
                        }
                    }
                }
    }
}
