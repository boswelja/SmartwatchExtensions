package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.appmanager.State
import com.boswelja.devicemanager.common.appmanager.App
import com.boswelja.devicemanager.databinding.FragmentAppManagerInfoBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A fragment for showing detailed info about a single [App].
 */
class AppInfoFragment : Fragment() {

    private val viewModel: AppManagerViewModel by activityViewModels()
    private val args: AppInfoFragmentArgs by navArgs()
    private val continueOnWatchSnackbar by lazy {
        Snackbar.make(
            requireView(),
            R.string.watch_manager_action_continue_on_watch,
            Snackbar.LENGTH_LONG
        )
    }
    private lateinit var binding: FragmentAppManagerInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppManagerInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        args.app.let {
            setupAppInfo(it)
            setupButtons(it)
            setupRequestedPermissions(it)
            setupPackageInfoViews(it)
        }
    }

    /**
     * Sets up the header view containing the app icon and name.
     * @param app The [App] to use data from.
     */
    private fun setupAppInfo(app: App) {
        binding.apply {
            app.icon?.bitmap?.let {
                appIcon.setImageBitmap(it)
            }
            appName.text = app.label
        }
    }

    /**
     * Sets up button views.
     * @param app The [App] to use data from.
     */
    private fun setupButtons(app: App) {
        binding.apply {
            openButton.isEnabled = app.hasLaunchActivity && viewModel.state.value == State.READY
            openButton.setOnClickListener {
                viewModel.sendOpenRequest(app)
                continueOnWatchSnackbar.show()
            }
            uninstallButton.isEnabled = !app.isSystemApp && viewModel.state.value == State.READY
            uninstallButton.setOnClickListener {
                viewModel.sendUninstallRequest(app)
                continueOnWatchSnackbar.show()
            }
        }
    }

    /**
     * Sets up the requested permissions view.
     * @param app The [App] to use data from.
     */
    private fun setupRequestedPermissions(app: App) {
        val requestsPermissions = app.requestedPermissions.isNotEmpty()
        binding.apply {
            permissionsInfo.topLine.text =
                getString(R.string.app_info_requested_permissions_title)
            permissionsInfo.bottomLine.text =
                if (requestsPermissions) {
                    val requestedPermissionCount = app.requestedPermissions.size
                    resources.getQuantityString(
                        R.plurals.app_info_requested_permissions_count,
                        requestedPermissionCount,
                        requestedPermissionCount
                    )
                } else {
                    getString(R.string.app_info_requested_permissions_none)
                }
            permissionsInfo.root.setOnClickListener {
                if (requestsPermissions) {
                    findNavController().navigate(
                        AppInfoFragmentDirections.appInfoFragmentToAppPermissionDialogFragment(app)
                    )
                }
            }
        }
    }

    /**
     * Sets up package info views.
     * @param app The [App] to use data from.
     */
    private fun setupPackageInfoViews(app: App) {
        binding.apply {
            appInstallTime.text = getString(
                R.string.app_info_first_installed_prefix,
                viewModel.formatDate(app.installTime)
            )
            appLastUpdatedTime.isVisible = app.installTime != app.lastUpdateTime
            appLastUpdatedTime.text = getString(
                R.string.app_info_last_updated_prefix,
                viewModel.formatDate(app.lastUpdateTime)
            )
            appVersionView.text = getString(R.string.app_info_version_prefix, app.version)
        }
    }
}
