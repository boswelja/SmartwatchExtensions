/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.widget.database

import androidx.room.Entity

@Entity(tableName = "watch_battery_widget_ids")
class WatchBatteryWidgetId(watchId: String, widgetId: Int) :
    WatchWidgetAssociation(watchId, widgetId)
