package com.boswelja.smartwatchextensions.appmanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CacheValidationTest {

    @Test
    fun hashesMatchForUnsortedData() {
        // Create two lists with the same contents in different order
        val originalPackageList = createPackagesList(100)
        val firstShuffledList = originalPackageList.shuffled()
        val secondShuffledList = firstShuffledList.shuffled()
        assertNotEquals(firstShuffledList, secondShuffledList)

        // Check cache hashcodes
        val firstCacheHash = CacheValidation.getHashCode(firstShuffledList)
        val secondCacheHash = CacheValidation.getHashCode(secondShuffledList)
        assertEquals(firstCacheHash, secondCacheHash)
    }

    @Test
    fun hashesDontMatchForDifferentData() {
        // Create two lists with the same contents in different order
        val originalPackageList = createPackagesList(100)
        val uniquePackageList = createPackagesList(101)

        // Check cache hashcodes
        val firstCacheHash = CacheValidation.getHashCode(originalPackageList)
        val secondCacheHash = CacheValidation.getHashCode(uniquePackageList)
        assertNotEquals(firstCacheHash, secondCacheHash)
    }

    private fun createPackagesList(count: Int): List<Pair<String, Long>> {
        return (0 until count).map { index ->
            Pair(index.toString(), index.toLong())
        }
    }
}
