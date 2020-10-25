/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService

abstract class BaseComplicationProviderService : ComplicationProviderService() {

    abstract fun onCreateComplication(
        complicationId: Int, type: Int, manager: ComplicationManager?
    )

    override fun onComplicationUpdate(
        complicationId: Int, type: Int, manager: ComplicationManager?
    ) {
        onCreateComplication(complicationId, type, manager)
    }

    override fun onComplicationActivated(
        complicationId: Int, type: Int, manager: ComplicationManager?
    ) {
        onCreateComplication(complicationId, type, manager)
    }
}
