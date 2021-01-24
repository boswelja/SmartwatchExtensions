/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.communication

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.boswelja.devicemanager.R

enum class WatchStatus(
    @StringRes val stringRes: Int,
    @DrawableRes val iconRes: Int
) {
    UNKNOWN(R.string.watch_status_unknown, R.drawable.ic_error),
    ERROR(R.string.watch_status_error, R.drawable.ic_error),
    MISSING_APP(R.string.watch_status_missing_app, R.drawable.ic_error),
    NOT_REGISTERED(R.string.watch_status_not_registered, R.drawable.ic_error),
    DISCONNECTED(R.string.watch_status_disconnected, R.drawable.ic_disconnected),
    CONNECTED(R.string.watch_status_connected, R.drawable.ic_connected)
}
