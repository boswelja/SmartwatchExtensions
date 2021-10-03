package com.boswelja.smartwatchextensions

import android.content.Context
import com.boswelja.watchconnection.common.message.MessageSerializer
import com.boswelja.watchconnection.wear.discovery.DiscoveryClient
import com.boswelja.watchconnection.wear.message.MessageClient

fun Context.discoveryClient(): DiscoveryClient =
    DiscoveryClient(
        this
    )

fun Context.messageClient(
    serializers: List<MessageSerializer<*>>
): MessageClient =
    MessageClient(
        this,
        serializers
    )
