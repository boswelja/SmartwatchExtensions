/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.watchconnectionmanager

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Watch::class, IntPreference::class, BoolPreference::class], version = 3)
abstract class WatchDatabase : RoomDatabase() {
    abstract fun watchDao(): WatchDao

    abstract fun intPreferenceDao(): IntPreferenceDao
    abstract fun boolPreferenceDao(): BoolPreferenceDao
}
