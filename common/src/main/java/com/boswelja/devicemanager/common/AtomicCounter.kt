/* Copyright (C) 2019 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import java.util.concurrent.atomic.AtomicInteger

object AtomicCounter {

    private val atomicInteger = AtomicInteger(0)

    fun getInt(): Int {
        return atomicInteger.incrementAndGet()
    }
}
