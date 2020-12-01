/* Copyright (C) 2020 Jack Boswell <boswelja@outlook.com>
 *
 * This file is part of Wearable Extensions
 *
 * This file, and any part of the Wearable Extensions app/s cannot be copied and/or distributed
 * without permission from Jack Boswell (boswelja) <boswela@outlook.com>
 */
package com.boswelja.devicemanager.common

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

class DataEvent<T> : MutableLiveData<T?>(null) {

    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        super.observe(owner) {
            if (it != null) {
                observer.onChanged(it)
                postValue(null)
            }
        }
    }
}
