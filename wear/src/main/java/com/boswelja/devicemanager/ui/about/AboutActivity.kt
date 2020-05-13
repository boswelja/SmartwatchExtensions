package com.boswelja.devicemanager.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.boswelja.devicemanager.BuildConfig
import com.boswelja.devicemanager.R
import com.boswelja.devicemanager.common.GooglePlayUtils
import com.boswelja.devicemanager.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAppIcon()
        setVersionText()
        setupPlayStoreLink()
        setupAppInfoLink()
    }

    private fun setAppIcon() {
        binding.appIconView.setImageDrawable(packageManager.getApplicationIcon(packageName))
    }

    private fun setVersionText() {
        binding.appVersionView.text = getString(R.string.version_string, BuildConfig.VERSION_NAME)
    }

    private fun setupPlayStoreLink() {
        binding.openPlayStoreContainer.setOnClickListener {
            openPlayStore()
        }
    }

    private fun setupAppInfoLink() {
        binding.openAppInfoContainer.setOnClickListener {
            openAppInfo()
        }
    }

    private fun openPlayStore() {
        GooglePlayUtils.getPlayStoreIntent(this).also {
            startActivity(it)
        }
    }

    private fun openAppInfo() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }.also {
            startActivity(it)
        }
    }
}