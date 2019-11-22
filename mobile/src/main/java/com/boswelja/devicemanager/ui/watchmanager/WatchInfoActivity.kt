package com.boswelja.devicemanager.ui.watchmanager

import android.os.Bundle
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity

class WatchInfoActivity : BaseToolbarActivity() {

    private var watchId: String? = null

    override fun getContentViewId(): Int = R.layout.activity_watch_info

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        watchId = intent?.getStringExtra(EXTRA_WATCH_ID)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = watchId
            setDisplayShowTitleEnabled(true)
        }
    }

    companion object {
        const val EXTRA_WATCH_ID = "extra_watch_id"
    }
}