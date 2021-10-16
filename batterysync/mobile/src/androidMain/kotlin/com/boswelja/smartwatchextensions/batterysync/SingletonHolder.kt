package com.boswelja.smartwatchextensions.batterysync

/**
 * A common implementation for creating a singleton class
 * @property T The output class of getInstance.
 * @property A The input types expected to create [T].
 * @param creator The function used to create [T] from [A].
 */
abstract class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val checkInstance = instance
        if (checkInstance != null) {
            return checkInstance
        }

        return synchronized(this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain != null) {
                checkInstanceAgain
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
