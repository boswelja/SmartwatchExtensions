package com.boswelja.devicemanager

import android.os.Bundle
import io.mockk.Matcher
import io.mockk.MockKMatcherScope

/**
 * A [Matcher] to match [Bundle] for MockK.
 */
data class BundleMatcher(val expected: Bundle) : Matcher<Bundle> {
    override fun match(arg: Bundle?): Boolean {
        if (arg == null) return false
        val expectedKeys = expected.keySet()
        val sameKeys = arg.keySet().minus(expectedKeys).isEmpty()
        val sameValues = expectedKeys.filterNot { arg.get(it) == expected.get(it) }.isEmpty()
        return sameKeys && sameValues
    }
}

fun MockKMatcherScope.matchBundle(bundle: Bundle) = match(BundleMatcher(bundle))
