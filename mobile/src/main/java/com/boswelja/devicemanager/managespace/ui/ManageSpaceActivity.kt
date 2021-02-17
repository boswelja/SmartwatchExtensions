package com.boswelja.devicemanager.managespace.ui

import android.os.Bundle
import androidx.fragment.app.commit
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityManageSpaceBinding
import com.boswelja.devicemanager.managespace.ui.actions.ClearCacheActionFragment
import com.boswelja.devicemanager.managespace.ui.actions.ResetAnalyticsActionFragment
import com.boswelja.devicemanager.managespace.ui.actions.ResetAppActionFragment
import com.boswelja.devicemanager.managespace.ui.actions.ResetAppSettingsActionFragment
import com.boswelja.devicemanager.managespace.ui.actions.ResetExtensionsActionFragment

/**
 * An activity to present the user with options for resetting various data used by Wearable
 * Extensions.
 */
class ManageSpaceActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityManageSpaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shouldAutoSwitchNightMode = false

        binding = ActivityManageSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.action_container, ClearCacheActionFragment())
                add(R.id.action_container, ResetAnalyticsActionFragment())
                add(R.id.action_container, ResetAppSettingsActionFragment())
                add(R.id.action_container, ResetExtensionsActionFragment())
                add(R.id.action_container, ResetAppActionFragment())
            }
        }
    }
}
