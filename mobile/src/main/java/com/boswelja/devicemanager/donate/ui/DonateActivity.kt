package com.boswelja.devicemanager.donate.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityDonateBinding
import com.google.android.material.tabs.TabLayoutMediator

class DonateActivity : BaseToolbarActivity() {

    private val viewModel: DonateViewModel by viewModels()

    private lateinit var binding: ActivityDonateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDonateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(binding.toolbarLayout.toolbar, showUpButton = true)

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> RecurringDonateFragment()
                    else -> OneTimeDonateFragment()
                }
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Monthly"
                else -> tab.text = "One-Time"
            }
        }.attach()

        viewModel.onDonated.observe(this) {
            createSnackBar(getString(R.string.donate_complete))
            sharedPreferences.edit { putBoolean(HAS_DONATED_KEY, true) }
        }
    }

    companion object {
        private const val HAS_DONATED_KEY = "has_donated"
    }
}
