package com.boswelja.devicemanager.watchmanager.ui.register

import android.os.Bundle
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityRegisterWatchBinding

class RegisterWatchActivity : BaseToolbarActivity() {

    private lateinit var binding: ActivityRegisterWatchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterWatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(
            binding.toolbarLayout.toolbar,
            showTitle = true,
            showUpButton = true
        )

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_holder, RegisterWatchFragment())
            .commit()
    }
}
