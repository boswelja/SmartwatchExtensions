package com.boswelja.devicemanager

import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Node

class TestCapabilityInfo(
    private val name: String,
    private val nodes: MutableSet<Node>
) : CapabilityInfo {

    override fun getName(): String = name
    override fun getNodes(): MutableSet<Node> = nodes
}
