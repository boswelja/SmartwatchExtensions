package com.boswelja.smartwatchextensions.common.appmanager

object CacheValidation {

    fun getHashCode(packages: List<Pair<String, Long>>): Int {
        return packages.map {
            "${it.first},${it.second}"
        }.sorted().hashCode()
    }
}
