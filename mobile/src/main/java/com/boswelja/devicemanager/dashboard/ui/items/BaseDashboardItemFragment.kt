package com.boswelja.devicemanager.dashboard.ui.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.databinding.DashboardItemBinding
import timber.log.Timber

abstract class BaseDashboardItemFragment : Fragment() {

    internal lateinit var binding: DashboardItemBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView() called")
        binding = DashboardItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    internal fun setupWidget(
        actionLabel: String,
        widgetContent: Fragment? = null,
        actionClickListener: () -> Unit
    ) {
        Timber.d("setupWidget() called")
        widgetContent?.let {
            childFragmentManager.beginTransaction()
                .replace(binding.itemContent.id, it)
                .commit()
        }
        binding.settingsAction.apply {
            setOnClickListener { actionClickListener() }
            text = actionLabel
        }
    }
}
