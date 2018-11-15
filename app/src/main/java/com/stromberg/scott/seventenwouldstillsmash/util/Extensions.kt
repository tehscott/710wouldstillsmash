package com.stromberg.scott.seventenwouldstillsmash.util

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

fun AppCompatActivity.showDialog(message: String) {
    showDialog(this, message)
}

fun FragmentActivity.showDialog(message: String) {
    showDialog(this, message)
}

fun Fragment.showDialog(message: String) {
    showDialog(activity!!, message)
}

private fun showDialog(context: Context, message: String) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle(null)
    builder.setMessage(message)
    builder.setPositiveButton(android.R.string.ok, { dialog, _ -> dialog.dismiss() })
    builder.show()
}