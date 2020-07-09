package com.stromberg.scott.seventenwouldstillsmash.util

import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log

class PlayerHelper {
    companion object {
        fun getLongestNameLength(resources: Resources, fontName: String, fontSize: Float, names: List<String?>): Int {
            val paint = Paint()
            val bounds = Rect()

            var longestLength = 0

            paint.textSize = fontSize

            names.forEach { name -> run {
                paint.getTextBounds(name, 0, name!!.length, bounds)

                if(bounds.width() > longestLength) {
                    longestLength = bounds.width()
                }
            }}

            Log.d("name", longestLength.toString())

            val minWidth = (Resources.getSystem().displayMetrics.widthPixels / 8)
            val maxWidth = (Resources.getSystem().displayMetrics.widthPixels / 4)
            return when {
                longestLength > maxWidth -> maxWidth
                longestLength < minWidth -> minWidth
                else -> (longestLength * 1.2).toInt()
            }
        }
    }
}