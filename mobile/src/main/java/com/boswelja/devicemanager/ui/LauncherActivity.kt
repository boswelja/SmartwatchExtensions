package com.boswelja.devicemanager.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class LauncherActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}