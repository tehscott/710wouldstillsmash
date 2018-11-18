package com.stromberg.scott.seventenwouldstillsmash.util

import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import uk.co.chrisjenx.calligraphy.TypefaceUtils

class PlayerHelper {
    companion object {
        fun getLongestNameLength(resources: Resources, fontName: String, fontSize: Float, names: List<String?>): Int {
            val paint = Paint()
            val bounds = Rect()

            var longestLength = 0

            paint.typeface = TypefaceUtils.load(resources.assets, fontName)
            paint.textSize = fontSize

            names?.forEach { name -> run {
                paint.getTextBounds(name, 0, name!!.length, bounds)

                if(bounds.width() > longestLength) {
                    longestLength = bounds.width()
                }
            }}

            Log.d("name", longestLength.toString())

            val oneThirdDisplayWidth = (Resources.getSystem().displayMetrics.widthPixels / 3)
            if(longestLength > oneThirdDisplayWidth) {
                return oneThirdDisplayWidth
            }
            else {
                return (longestLength * 1.10).toInt()
            }
        }
    }
}