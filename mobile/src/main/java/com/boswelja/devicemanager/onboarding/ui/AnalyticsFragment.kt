package com.boswelja.devicemanager.onboarding.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.analytics.Analytics
import com.boswelja.devicemanager.databinding.FragmentOnboardingAnalyticsBinding
import timber.log.Timber

class AnalyticsFragment : Fragment() {

    private lateinit var binding: FragmentOnboardingAnalyticsBinding

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }
    private val customTabsIntent: CustomTabsIntent by lazy {
        CustomTabsIntent.Builder().setShowTitle(true).build()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            nextButton.setOnClickListener {
                findNavController().navigate(AnalyticsFragmentDirections.toSetupFragment())
            }
            privPolicyButton.setOnClickListener {
                showPrivacyPolicy()
            }
            sendAnalyticsCheckbox.setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit {
                    putBoolean(Analytics.ANALYTICS_ENABLED_KEY, isChecked)
                }
            }
        }
    }

    /** Launches a custom tab that navigates to the Wearable Extensions privacy policy. */
    private fun showPrivacyPolicy() {
        Timber.d("showPrivacyPolicy() called")
        customTabsIntent.launchUrl(requireContext(), getString(R.string.privacy_policy_url).toUri())
    }
}
