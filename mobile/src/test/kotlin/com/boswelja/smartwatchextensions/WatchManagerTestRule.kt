package com.boswelja.smartwatchextensions

import com.boswelja.smartwatchextensions.watchmanager.WatchManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class WatchManagerTestRule(private val relaxedMock: Boolean = false) : TestRule {

    lateinit var watchManager: WatchManager

    override fun apply(base: Statement?, description: Description?): Statement =
        WatchManagerTestStatement(base)

    inner class WatchManagerTestStatement(private val base: Statement?) : Statement() {
        override fun evaluate() {
            watchManager = mockk(relaxed = relaxedMock)
            mockkObject(WatchManager.Companion)
            every {
                hint(WatchManager::class)
                WatchManager.Companion.getInstance(any())
            } returns watchManager
            try {
                base?.evaluate()
            } finally {
                unmockkObject(WatchManager.Companion)
            }
        }
    }
}
