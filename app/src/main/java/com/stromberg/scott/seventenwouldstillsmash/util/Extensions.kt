package com.stromberg.scott.seventenwouldstillsmash.util

import android.content.Context
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import java.util.*


val Int.toDp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.toPx: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
fun ClosedRange<Int>.random() = Math.round(Random().nextFloat() * (endInclusive - start) + start)

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun FirebaseDatabase.getReference(context: Context): DatabaseReference {
    val prefs = context.getSharedPreferences(context.getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
    val groups = Gson().fromJson<Array<Group>>(prefs.getString(context.getString(R.string.shared_prefs_group_codes), ""), Array<Group>::class.java).toCollection(ArrayList())
    val code = groups.firstOrNull { it.isSelected }?.code

    return if(code != null) {
        reference.child(code)
    }
    else {
        reference
    }
}

fun Snackbar.setBackgroundColor(@ColorRes color: Int): Snackbar {
    view.setBackgroundResource(color)

    return this
}

fun Snackbar.setTextColor(@ColorInt color: Int): Snackbar {
    val tv = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
    tv.setTextColor(color)

    return this
}

fun Snackbar.setTextSize(size: Float): Snackbar {
    val tv = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)

    return this
}

fun Snackbar.setTextStyle(style: Int): Snackbar {
    val tv = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
    val typeFace = Typeface.defaultFromStyle(style)
    tv.typeface = typeFace

    return this
}

fun Snackbar.setTextAttributes(@ColorInt color: Int, size: Float = 14f, style: Int = Typeface.NORMAL): Snackbar {
    this.setTextColor(color)
    this.setTextSize(size)
    this.setTextStyle(style)

    return this
}

fun HorizontalScrollView.scrollToView(view: View?) {
    if(view == null) {
        return
    }

    val rect = Rect(0, 0, view.width, view.height)
    view.getHitRect(rect)

    smoothScrollTo(rect.left, 0)
}

fun TextView.setDrawableTintColor(@ColorRes color: Int) {
    compoundDrawables.filterNotNull().forEach { drawable ->
        drawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.SRC_IN)
    }
}