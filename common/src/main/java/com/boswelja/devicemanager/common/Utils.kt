package com.boswelja.devicemanager.common

import android.content.res.Resources
import android.util.TypedValue

object Utils {

    fun complexTypeDp(resources: Resources, dp: Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

}