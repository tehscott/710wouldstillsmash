package com.stromberg.scott.seventenwouldstillsmash.util

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.util.*

val Int.toDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
fun ClosedRange<Int>.random() = Math.round(Random().nextFloat() * (endInclusive - start) + start)

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}