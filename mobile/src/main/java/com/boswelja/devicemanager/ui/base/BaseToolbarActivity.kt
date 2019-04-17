package com.boswelja.devicemanager.ui.base

import android.os.Bundle
import com.boswelja.devicemanager.R

abstract class BaseToolbarActivity : BaseDayNightActivity() {

    abstract fun getContentViewId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getContentViewId())

        setSupportActionBar(findViewById(R.id.toolbar))
    }

}