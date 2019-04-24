package com.boswelja.devicemanager.ui.base

import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.TypedValue
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.boswelja.devicemanager.R
import com.google.android.material.appbar.AppBarLayout

abstract class BaseToolbarActivity : BaseDayNightActivity() {

    private var toolbarElevated = false

    abstract fun getContentViewId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(getContentViewId())

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    fun elevateToolbar(elevate: Boolean) {
        if (toolbarElevated != elevate) {
            toolbarElevated = elevate
            val appBarLayout = findViewById<AppBarLayout>(R.id.appbarlayout)
            val elevation = if (elevate) {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics)
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