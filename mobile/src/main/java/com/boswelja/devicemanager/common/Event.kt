package com.boswelja.devicemanager.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class Event : MutableLiveData<Boolean>(false) {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Boolean>) {
        super.observe(owner) {
            if (it) {
                observer.onChanged(it)
                postValue(false)
            }
        }
    }

    fun fire() {
        postValue(true)
    }
}
