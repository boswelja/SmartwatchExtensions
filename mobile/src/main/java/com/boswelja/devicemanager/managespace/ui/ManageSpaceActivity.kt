package com.boswelja.devicemanager.managespace.ui

import android.os.Bundle
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityManageSpaceBinding

/**
 * An activity to present the user with options for resetting various data used by Wearable
 * Extensions.
 */
class ManageSpaceActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityManageSpaceBinding

    private val clearCacheSheet by lazy { ClearCacheBottomSheet() }
    private val resetSettingsSheet by lazy { ResetSettingsBottomSheet() }
    private val resetAppSheet by lazy { ResetAppBottomSheet() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)

        binding.apply {
            clearCacheButton.setOnClickListener { showClearCacheSheet() }
            resetSettingsButton.setOnClickListener { showResetSettingsSheet() }
            resetAppButton.setOnClickListener { showResetAppSheet() }
        }
    }

    /** Shows a [ClearCacheBottomSheet]. */
    private fun showClearCacheSheet() {
        clearCacheSheet.show(supportFragmentManager, clearCacheSheet::class.simpleName)
    }

    /** Shows a [ResetSettingsBottomSheet]. */
    private fun showResetSettingsSheet() {
        resetSettingsSheet.show(supportFragmentManager, resetSettingsSheet::class.simpleName)
    }

    /** Shows a [ResetAppBottomSheet]. */
    private fun showResetAppSheet() {
        resetAppSheet.show(supportFragmentManager, resetAppSheet::class.simpleName)
    }
}
