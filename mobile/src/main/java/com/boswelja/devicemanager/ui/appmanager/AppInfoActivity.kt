/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.appmanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.Utils
import com.boswelja.devicemanager.common.appmanager.AppPackageInfo
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity
import com.google.android.material.button.MaterialButton

class AppInfoActivity : BaseToolbarActivity() {

    override fun getContentViewId(): Int = R.layout.activity_app_info

    private lateinit var app: AppPackageInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        app = intent?.extras?.getSerializable(EXTRA_APP_INFO) as AppPackageInfo

        setAppInfo()
        setupButtons()
        setMiscInfo()
    }

    private fun setAppInfo() {
        findViewById<AppCompatImageView>(R.id.app_icon).setImageDrawable(Utils.getAppIcon(this, app.packageName))
        findViewById<AppCompatTextView>(R.id.app_name).text = app.packageLabel
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.open_button).apply {
            setOnClickListener {
                Log.d("AppinfoActivity", app.packageName)
                val intent = Intent()
                intent.putExtra(EXTRA_APP_INFO, app)
                setResult(RESULT_REQUEST_OPEN, intent)
                finish()
            }
            isEnabled = app.hasLaunchActivity
        }
        findViewById<MaterialButton>(R.id.uninstall_button).apply {
            setOnClickListener {
                val intent = Intent()
                intent.putExtra(EXTRA_APP_INFO, app)
                setResult(RESULT_REQUEST_UNINSTALL, intent)
                finish()
            }
            isEnabled = (app.packageName != packageName && !app.isSystemApp)
        }
    }

    private fun setMiscInfo() {
        findViewById<AppCompatTextView>(R.id.app_version_view).apply {
            text = getString(R.string.app_info_version_prefix).format(app.versionName)
        }
    }

    companion object {
        const val EXTRA_APP_INFO = "extra_app_info"

        const val RESULT_REQUEST_UNINSTALL = 718181
        const val RESULT_REQUEST_OPEN = 181817
    }
}
