/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchmanager.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.boswelja.devicemanager.watchmanager.item.Watch

@Dao
interface WatchDao {

    @Query("SELECT * FROM watches")
    fun getAll(): List<Watch>

    @Query("SELECT * FROM watches WHERE id = :watchId LIMIT 1")
    fun get(watchId: String): Watch?

    @Query("UPDATE watches SET name = :name WHERE id = :watchId")
    fun setName(watchId: String, name: String)

    @Insert
    fun add(watch: Watch)

    @Query("DELETE FROM watches WHERE id = :watchId")
    fun remove(watchId: String)

    @Query("SELECT * FROM watches")
    fun getAllObservable(): LiveData<List<Watch>>
}
