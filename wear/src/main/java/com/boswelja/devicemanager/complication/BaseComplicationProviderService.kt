package com.boswelja.devicemanager.complication

import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService

abstract class BaseComplicationProviderService : ComplicationProviderService() {

    abstract fun onCreateComplication(complicationId: Int, type: Int, manager: ComplicationManager?)

    override fun onComplicationUpdate(complicationId: Int, type: Int, manager: ComplicationManager?) {
        onCreateComplication(complicationId, type, manager)
    }

    override fun onComplicationActivated(complicationId: Int, type: Int, manager: ComplicationManager?) {
        onCreateComplication(complicationId, type, manager)
    }
}