package com.boswelja.devicemanager.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

class Event : LiveData<Boolean>(false) {

    /**
     * Keeps track of observers we've been given, and their corresponding wrappers.
     */
    private val observers = HashMap<Observer<in Boolean>, Observer<in Boolean>>()

    override fun observe(owner: LifecycleOwner, observer: Observer<in Boolean>) {
        val newObserver = Observer<Boolean> { t ->
            if (t == true) {
                observer.onChanged(value)
                postValue(false)
            }
        }
        observers[observer] = newObserver
        super.observe(owner, newObserver)
    }

    override fun observeForever(observer: Observer<in Boolean>) {
        val newObserver = Observer<Boolean> { t ->
            if (t == true) {
                observer.onChanged(value)
                postValue(false)
            }
        }
        observers[observer] = newObserver
        super.observeForever(newObserver)
    }

    override fun removeObserver(observer: Observer<in Boolean>) {
        observers[observer]?.let { super.removeObserver(it) }
    }

    fun fire() {
        postValue(true)
    }
}
