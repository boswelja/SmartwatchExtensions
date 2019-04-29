package com.boswelja.devicemanager.ui.base

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.MenuItem
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.google.android.material.appbar.AppBarLayout

abstract class BaseToolbarActivity : BaseDayNightActivity() {

    private var toolbarElevated = false

    abstract fun getContentViewId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getContentViewId())

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun elevateToolbar(elevate: Boolean) {
        if (toolbarElevated != elevate) {
            toolbarElevated = elevate
            val appBarLayout = findViewById<AppBarLayout>(R.id.appbarlayout)
            val elevation = if (elevate) {
                Utils.complexTypeDp(resources, 6f)
            } else {
                0f
            }
            ObjectAnimator.ofFloat(appBarLayout, "elevation", elevation).apply {
                duration = 250
                interpolator = FastOutSlowInInterpolator()
                start()
            }
        }
    }

}