package com.boswelja.smartwatchextensions.dndsync

/**
 * Contains values to track the state of DnD Sync feature.
 * @param dndSyncToPhone Whether DnD Sync to Phone is enabled.
 * @param dndSyncWithTheater Whether DnD Sync with Theater is enabled.
 */
data class DnDSyncState(
    val dndSyncToPhone: Boolean,
    val dndSyncWithTheater: Boolean
)
