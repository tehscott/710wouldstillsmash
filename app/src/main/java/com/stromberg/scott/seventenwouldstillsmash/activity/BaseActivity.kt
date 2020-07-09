package com.stromberg.scott.seventenwouldstillsmash.activity

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    abstract fun setContentShown(shown: Boolean)

    fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(null)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}