/* Copyright (C) 2018 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.wear.widget.drawer.WearableNavigationDrawerView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.controls.ControlsFragment
import com.boswelja.devicemanager.ui.navigation.NavigationDrawerAdapter
import com.boswelja.devicemanager.ui.navigation.NavigationDrawerSections

class MainActivity : AppCompatActivity(), WearableNavigationDrawerView.OnItemSelectedListener {

    private lateinit var navigationDrawer: WearableNavigationDrawerView
    private val controlsFragment = ControlsFragment()
    private var settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        navigationDrawer = findViewById(R.id.navigation_drawer)
        navigationDrawer.setAdapter(NavigationDrawerAdapter(this))
        navigationDrawer.addOnItemSelectedListener(this)

        navigate(controlsFragment)

        // Check for companion app
//        Utils.getCompanionNode(this)
//                .addOnCompleteListener {
//                    val node = it.result?.nodes?.lastOrNull()
//                    if (node == null) {
//                        val intent = Intent(this@MainActivity, ConfirmInstallActivity::class.java)
//                        startActivity(intent)
//                        finish()
//                    }
//                }
    }

    private fun navigate(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_holder, fragment)
        transaction.commit()
    }

    override fun onItemSelected(pos: Int) {
        when (NavigationDrawerSections.values()[pos]) {
            NavigationDrawerSections.Controls -> {
                navigate(controlsFragment)
            }
            NavigationDrawerSections.Settings -> {
                navigate(settingsFragment)
            }
        }
    }

}
