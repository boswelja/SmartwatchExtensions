/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.os.Bundle
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity

class AppManagerActivity : BaseToolbarActivity() {

    override fun getContentViewId(): Int = R.layout.activity_app_manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_holder, AppManagerFragment())
                .commit()
    }
}
