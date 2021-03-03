package com.boswelja.devicemanager.about.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.boswelja.devicemanager.GooglePlayUtils
import com.boswelja.devicemanager.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private val viewModel: AboutViewModel by viewModels()

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)

        viewModel.openPlayStoreEvent.observe(this) {
            if (it) {
                GooglePlayUtils.getPlayStoreIntent(this).also { startActivity(it) }
                viewModel.openPlayStoreEvent.postValue(false)
            }
        }
        viewModel.openAppInfoEvent.observe(this) {
            if (it) {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply { data = Uri.fromParts("package", packageName, null) }
                    .also { startActivity(it) }
                viewModel.openAppInfoEvent.postValue(false)
            }
        }
    }
}
