package com.stromberg.scott.seventenwouldstillsmash.util

import android.content.Context


class AndroidUtil {
    companion object {
        fun getStatusBarHeight(context: Context): Int {
            val resources = context.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            }
            else {
                Math.ceil((24 * resources.displayMetrics.density).toDouble()).toInt()
            }
        }
    }
}