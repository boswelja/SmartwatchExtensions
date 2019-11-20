/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WatchDao {

    @Query("SELECT * FROM watches")
    fun getAll(): List<Watch>

    @Query("SELECT * FROM watches WHERE id = :id LIMIT 1")
    fun findById(id: String): Watch?

    @Query("SELECT * FROM watches WHERE battery_sync_job_id = :batterySyncJobId LIMIT 1")
    fun findByBatterySyncJobId(batterySyncJobId: Int): Watch?

    @Insert
    fun add(watch: Watch)

    @Delete
    fun remove(watch: Watch)
}
