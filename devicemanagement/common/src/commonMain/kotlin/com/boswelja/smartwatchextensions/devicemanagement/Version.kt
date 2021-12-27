package com.boswelja.smartwatchextensions.devicemanagement

import kotlinx.serialization.Serializable

/**
 * Contains information about the installed version of Smartwatch Extensions on a device.
 * @param versionCode The installed version code.
 * @param versionName The installed version name.
 */
@Serializable
data class Version(
    val versionCode: Long,
    val versionName: String
)
