package com.boswelja.devicemanager.managespace.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.databinding.BottomSheetConfirmationBinding
import com.boswelja.devicemanager.databinding.BottomSheetProgressBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber

/**
 * An abstract class for handling common interactions and UI flow for resetting things.
 */
abstract class BaseResetBottomSheet : BottomSheetDialogFragment() {

    internal lateinit var binding: BottomSheetProgressBinding
    internal val viewModel: ManageSpaceViewModel by activityViewModels()

    private lateinit var confirmationFragment: ConfirmationFragment
    private lateinit var progressFragment: ProgressFragment
    private lateinit var resultsFragment: ConfirmationFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            id = R.id.fragment_holder
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        confirmationFragment = onCreateConfirmation {
            // By default, we provide the callback to show the progress fragment.
            isCancelable = false
            progressFragment = onCreateProgress()
            showFragment(progressFragment)
            onStartWork()
        }
        showFragment(confirmationFragment)
    }

    /**
     * Called when an instance of [ConfirmationFragment] is required.
     * @param showProgress A function that shows the [ProgressFragment]. This should be passed to
     * [ConfirmationFragment], but can be overridden of necessary.
     * @return An instance of [ConfirmationFragment].
     */
    internal abstract fun onCreateConfirmation(showProgress: () -> Unit): ConfirmationFragment

    /**
     * Called when an instance of [ProgressFragment] is required.
     * @return An instance of [ProgressFragment].
     */
    internal abstract fun onCreateProgress(): ProgressFragment

    /**
     * Called when an instance of [ConfirmationFragment] is required.
     * @param dismissSheet A function that dismisses the current bottom sheet. This should be
     * passed to [ConfirmationFragment], but can be overridden if necessary.
     * @return An instance of [ConfirmationFragment].
     */
    internal abstract fun onCreateResult(dismissSheet: () -> Unit): ConfirmationFragment

    /**
     * Called when the bottom sheet is ready for work to start.
     */
    internal abstract fun onStartWork()

    /**
     * Set the progress fragment indicator progress. When progress is 0 or lower, the indicator is
     * indeterminate. When the progress is equal to or higher than 100, the result fragment is shown
     * @param progress The progress of the current operation. Should be in range 0-100.
     */
    internal fun setProgress(progress: Int) {
        Timber.d("setProgress($progress) called")
        if (progress >= 100) {
            resultsFragment = onCreateResult { dismiss() }
            showFragment(resultsFragment)
            isCancelable = true
        } else {
            try {
                progressFragment.setProgress(progress)
            } catch (e: UninitializedPropertyAccessException) {
                Timber.e(e)
            }
        }
    }

    /**
     * Show a fragment in the bottom sheet.
     */
    private fun showFragment(fragment: Fragment) {
        Timber.d("showFragment() called")
        childFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            replace(R.id.fragment_holder, fragment)
        }
    }

    internal class ConfirmationFragment(
        private val title: String,
        private val description: String,
        private val buttonLabel: String,
        private val buttonIcon: Drawable,
        private val onConfirmed: () -> Unit
    ) : Fragment() {

        private lateinit var binding: BottomSheetConfirmationBinding

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            binding = BottomSheetConfirmationBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            binding.title.text = title
            binding.desc.text = description
            binding.button.apply {
                text = buttonLabel
                icon = buttonIcon
                setOnClickListener { onConfirmed() }
            }
        }
    }

    internal class ProgressFragment(
        private val title: String,
        private val description: String
    ) : Fragment() {

        private lateinit var binding: BottomSheetProgressBinding

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            binding = BottomSheetProgressBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            binding.title.text = title
            binding.desc.text = description
        }

        internal fun setProgress(progress: Int) {
            Timber.d("setProgress($progress) called")
            binding.progress.progress = progress
            binding.progress.isIndeterminate = progress <= 0
        }
    }
}
