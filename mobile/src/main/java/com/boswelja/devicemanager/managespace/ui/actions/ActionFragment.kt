package com.boswelja.devicemanager.managespace.ui.actions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.boswelja.devicemanager.databinding.ManageSpaceActionBinding
import com.boswelja.devicemanager.managespace.ui.sheets.BaseResetBottomSheet

/**
 * A [Fragment] for displaying information about an action, and a button to execute said action.
 */
abstract class ActionFragment : Fragment() {

    private val sheet by lazy { onCreateSheet() }

    internal lateinit var binding: ManageSpaceActionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ManageSpaceActionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.button.setOnClickListener {
            sheet.show(childFragmentManager, sheet::class.simpleName)
        }
    }

    /**
     * Called when [BaseResetBottomSheet] needs to be created.
     */
    abstract fun onCreateSheet(): BaseResetBottomSheet
}
