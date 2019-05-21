package com.boswelja.devicemanager.ui.appmanager

import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.ui.base.BaseToolbarActivity

class AppInfoActivity : BaseToolbarActivity() {

    override fun getContentViewId(): Int = R.layout.activity_app_info

    companion object {
        const val EXTRA_APP_INFO = "extra_app_info"

        const val RESPONSE_REQUEST_UNINSTALL = 718181
    }
}