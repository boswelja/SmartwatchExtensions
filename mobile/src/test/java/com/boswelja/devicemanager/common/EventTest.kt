package com.boswelja.devicemanager.common

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.boswelja.devicemanager.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.R])
class EventTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private val testLifecycleOwner = object : LifecycleOwner {

        private val lifecycle = LifecycleRegistry(this)

        init {
            lifecycle.currentState = Lifecycle.State.STARTED
        }

        override fun getLifecycle(): Lifecycle {
            return lifecycle
        }
    }

    private lateinit var event: Event

    @Before
    fun setUp() {
        event = Event()
    }

    @Test
    fun `Observing event automatically resets event fired`() {
        val observer = { _: Boolean -> }
        event.observe(testLifecycleOwner, observer)
        event.fire()
        assertThat(event.value).isFalse()
    }

    @Test
    fun `Observer boolean is never false using observe`() {
        val observer = { value: Boolean ->
            assertThat(value).isTrue()
        }
        event.observe(testLifecycleOwner, observer)
        event.fire()
    }

    @Test
    fun `Observing event forever automatically resets event fired`() {
        val observer = { _: Boolean -> }
        event.observeForever(observer)
        event.fire()
        assertThat(event.value).isFalse()
        event.removeObserver(observer)
    }

    @Test
    fun `Observer boolean is never false using observeForever`() {
        val observer = { value: Boolean ->
            assertThat(value).isTrue()
        }
        event.observeForever(observer)
        event.fire()
        event.removeObserver(observer)
    }

    @Test
    fun `Firing event fires event`() {
        event.fire()
        assertThat(event.getOrAwaitValue()).isTrue()
    }
}
