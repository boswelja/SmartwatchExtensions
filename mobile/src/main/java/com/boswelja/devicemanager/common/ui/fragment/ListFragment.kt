package com.boswelja.devicemanager.common.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.boswelja.devicemanager.databinding.CommonFragmentListBinding

/**
 * A common [Fragment] with support for showing a [RecyclerView] and a loading indicator.
 */
open class ListFragment : Fragment() {

    internal lateinit var binding: CommonFragmentListBinding

    /**
     * Sets whether the loading indicator should be shown.
     */
    internal var isLoading: Boolean
        get() = binding.loadingIndicator.isVisible
        set(value) { binding.loadingIndicator.isVisible = value }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CommonFragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }
}
