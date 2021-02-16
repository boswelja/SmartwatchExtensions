package com.boswelja.devicemanager

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A rule to mock [Tasks.await] and make it succeed regardless of thread.
 */
class TasksAwaitRule : TestWatcher() {
    override fun starting(description: Description?) {
        mockkStatic(Tasks::class)
        every { Tasks.await(any<Task<Any>>()) } answers {
            val task = (this.firstArg() as Task<Any>)
            task.result
        }

        super.starting(description)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        unmockkStatic(Tasks::class)
    }
}
