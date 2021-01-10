/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.LoadingFragment
import timber.log.Timber

class RegisterWatchFragment : Fragment() {

    private val viewModel: RegisterWatchViewModel by viewModels()

    private val loadingFragment: LoadingFragment by lazy { LoadingFragment() }
    private val registerResultsFragment: WatchRegisterResultsFragment by lazy { WatchRegisterResultsFragment() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = FrameLayout(requireContext())
        view.id = R.id.fragment_holder
        view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.isWorking.observe(viewLifecycleOwner) {
            setLoading(it)
        }
    }

    /**
     * Sets whether the loading view should be shown.
     * @param loading true if the loading view should be shown, false otherwise
     */
    internal fun setLoading(loading: Boolean) {
        Timber.d("setLoading($loading)")
        childFragmentManager.commit {
            if (loading) {
                replace(requireView().id, loadingFragment)
            } else {
                replace(requireView().id, registerResultsFragment)
            }
            setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}
