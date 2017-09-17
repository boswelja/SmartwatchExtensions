package com.boswelja.devicemanager

object Config {

    val TYPE_EMPTY: Int = 0
    val TYPE_LOCK_PHONE: Int = 1
    val TYPE_TOGGLE_WIFI = 2

    val LOCK_PHONE_PATH: String = "/lock_phone"

    val CAPABILITY_PHONE_APP = "device_manager_mobile_app"

    val INTENT_PERFORM_ACTION = "com.boswelja.devicemanager.intent.action.PERFORM_ACTION"
}