package com.boswelja.devicemanager.common

import java.util.concurrent.atomic.AtomicInteger

object AtomicCounter {

    private val atomicInteger = AtomicInteger(0)

    fun getInt(): Int {
        return atomicInteger.incrementAndGet()
    }
}