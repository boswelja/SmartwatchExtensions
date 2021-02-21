package com.boswelja.devicemanager.watchmanager.ui.register

import android.os.Bundle
import androidx.activity.viewModels
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.ui.activity.BaseToolbarActivity
import com.boswelja.devicemanager.databinding.ActivityRegisterWatchBinding

class RegisterWatchActivity : BaseToolbarActivity() {

    private val viewModel: RegisterWatchViewModel by viewModels()

    private lateinit var binding: ActivityRegisterWatchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterWatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar(
            binding.toolbarLayout.toolbar,
            showTitle = false,
            showUpButton = true
        )

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_holder, RegisterWatchFragment())
            .commit()

        viewModel.onFinished.observe(this) {
            finish()
        }
    }
}
