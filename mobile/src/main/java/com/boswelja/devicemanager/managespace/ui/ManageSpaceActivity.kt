package com.boswelja.devicemanager.managespace.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityManageSpaceBinding
import com.boswelja.devicemanager.databinding.ManageSpaceItemBinding

/**
 * An activity to present the user with options for resetting various data used by Wearable
 * Extensions.
 */
class ManageSpaceActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityManageSpaceBinding

    private val clearCacheSheet by lazy { ClearCacheBottomSheet() }
    private val resetSettingsSheet by lazy { ResetExtensionsBottomSheet() }
    private val resetAppSheet by lazy { ResetAppBottomSheet() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageSpaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showTitle = true, showUpButton = true)

        createClearCacheAction()
        createExtensionResetAction()
        createAppResetAction()
    }

    /**
     * Creates a [SpaceActionFragment] for clearing cache, and adds it to the view.
     */
    private fun createClearCacheAction() {
        val fragment = SpaceActionFragment(
            getString(R.string.clear_cache_title),
            getString(R.string.clear_cache_desc),
            getString(R.string.clear_cache_title)
        ) {
            clearCacheSheet.show(supportFragmentManager, clearCacheSheet::class.simpleName)
        }
        addAction(fragment)
    }

    /**
     * Creates a [SpaceActionFragment] for resetting extension related settings, and adds it to the
     * view.
     */
    private fun createExtensionResetAction() {
        val fragment = SpaceActionFragment(
            getString(R.string.reset_extensions_title),
            getString(R.string.reset_extensions_desc),
            getString(R.string.reset_extensions_title)
        ) {
            resetSettingsSheet.show(supportFragmentManager, resetSettingsSheet::class.simpleName)
        }
        addAction(fragment)
    }

    /**
     * Creates a [SpaceActionFragment] for resetting extension related settings, and adds it to the
     * view.
     */
    private fun createAppResetAction() {
        val fragment = SpaceActionFragment(
            getString(R.string.reset_app_title),
            getString(R.string.reset_app_desc),
            getString(R.string.reset_app_title)
        ) {
            resetAppSheet.show(supportFragmentManager, resetAppSheet::class.simpleName)
        }
        addAction(fragment)
    }

    /**
     * Adds a [SpaceActionFragment] to [R.id.action_container].
     */
    private fun addAction(fragment: SpaceActionFragment) {
        supportFragmentManager.commit {
            add(R.id.action_container, fragment)
        }
    }

    /**
     * A [Fragment] for displaying information about an action, and a button to execute said action.
     * @param title The title to display.
     * @param description The description of the action to display.
     * @param buttonLabel The button label to display
     * @param onButtonClick A function to be called when the button is clicked.
     */
    class SpaceActionFragment(
        private val title: String,
        private val description: String,
        private val buttonLabel: String,
        private val onButtonClick: () -> Unit
    ) : Fragment() {
        private lateinit var binding: ManageSpaceItemBinding

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            binding = ManageSpaceItemBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            binding.title.text = title
            binding.desc.text = description
            binding.button.text = buttonLabel
            binding.button.setOnClickListener { onButtonClick() }
        }
    }
}
