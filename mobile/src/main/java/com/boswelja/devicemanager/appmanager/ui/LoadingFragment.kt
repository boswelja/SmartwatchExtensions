package com.boswelja.devicemanager.appmanager.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.boswelja.devicemanager.common.ui.fragment.LoadingFragment

class LoadingFragment : LoadingFragment() {
    private val viewModel: AppManagerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.progress.observe(viewLifecycleOwner) {
            setProgress(it)
        }
    }
}
