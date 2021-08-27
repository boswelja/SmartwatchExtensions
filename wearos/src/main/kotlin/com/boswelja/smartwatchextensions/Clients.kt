package com.boswelja.smartwatchextensions

import android.content.Context
import com.boswelja.smartwatchextensions.common.connection.Capability
import com.boswelja.smartwatchextensions.phoneconnectionmanager.ConnectionHelper.Companion.CAPABILITY_PHONE_APP
import com.boswelja.watchconnection.common.message.serialized.MessageSerializer
import com.boswelja.watchconnection.wearos.discovery.DiscoveryClient
import com.boswelja.watchconnection.wearos.message.MessageClient

fun Context.discoveryClient(): DiscoveryClient =
    DiscoveryClient(
        this,
        CAPABILITY_PHONE_APP,
        Capability.values().map { it.name }
    )

fun Context.messageClient(
    serializers: List<MessageSerializer<*>>
): MessageClient =
    MessageClient(
        this,
        serializers
    )
