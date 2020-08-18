/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.boswelja.devicemanager.common.GooglePlayUtils
import com.boswelja.devicemanager.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

  private lateinit var binding: ActivityAboutBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityAboutBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupPlayStoreLink()
    setupAppInfoLink()
  }

  private fun setupPlayStoreLink() {
    binding.openPlayStoreContainer.setOnClickListener { openPlayStore() }
  }

  private fun setupAppInfoLink() {
    binding.openAppInfoContainer.setOnClickListener { openAppInfo() }
  }

  private fun openPlayStore() {
    GooglePlayUtils.getPlayStoreIntent(this).also { startActivity(it) }
  }

  private fun openAppInfo() {
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .apply { data = Uri.fromParts("package", packageName, null) }
        .also { startActivity(it) }
  }
}
