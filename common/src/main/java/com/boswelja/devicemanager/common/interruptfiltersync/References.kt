/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common.interruptfiltersync

import com.boswelja.devicemanager.common.References

object References {

    const val DND_STATUS_PATH = "/dnd_status"
    const val NEW_DND_STATE_KEY = "${References.packageName}.dnd-enabled"
    const val REQUEST_INTERRUPT_FILTER_ACCESS_STATUS_PATH = "/request_noti_policy_access_status"
}
