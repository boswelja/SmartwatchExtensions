/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

object TestExtensions {

    fun withDrawable(@DrawableRes id: Int) = object : TypeSafeMatcher<View>() {
        override fun describeTo(description: Description) {
            description.appendText("ImageView with drawable same as drawable with id $id")
        }

        override fun matchesSafely(view: View): Boolean {
            val expectedBitmap = ContextCompat.getDrawable(view.context, id)!!
                .toBitmap()
            return view is ImageView && view.drawable
                .toBitmap().sameAs(expectedBitmap)
        }
    }

    fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        afterObserve: (value: T?) -> Unit = {}
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer =
            object : Observer<T?> {
                override fun onChanged(t: T?) {
                    data = t
                    latch.countDown()
                    this@getOrAwaitValue.removeObserver(this)
                }
            }
        this.observeForever(observer)

        afterObserve.invoke(data)

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            this.removeObserver(observer)
            throw TimeoutException("LiveData value was never set.")
        }

        @Suppress("UNCHECKED_CAST")
        return data as T
    }
}
