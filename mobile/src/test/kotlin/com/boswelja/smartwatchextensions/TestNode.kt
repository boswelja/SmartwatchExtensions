package com.boswelja.smartwatchextensions

import com.google.android.gms.wearable.Node

class TestNode(
    private val id: String,
    private val displayName: String,
    private val isNearby: Boolean = true
) : Node {

    override fun getDisplayName(): String = displayName
    override fun getId(): String = id
    override fun isNearby(): Boolean = isNearby

    override fun equals(other: Any?): Boolean {
        if (other !is Node) return super.equals(other)
        return other.id == id &&
            other.displayName == displayName &&
            other.isNearby == isNearby
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + isNearby.hashCode()
        return result
    }
}
