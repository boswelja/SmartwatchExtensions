package com.boswelja.smartwatchextensions.appmanager

/**
 * An object to aid in validating App Manager caches.
 */
object CacheValidation {

    /**
     * Takes a list of [Pair] containing a package name, and the last update time in milliseconds,
     * and calculates a hash code.
     * @param packages The [List] of [Pair] of package names to last update timestamps.
     * @return A hash code for the provided list.
     */
    fun getHashCode(packages: List<Pair<String, Long>>): Int {
        return packages.map {
            "${it.first},${it.second}"
        }.sorted().hashCode()
    }
}
