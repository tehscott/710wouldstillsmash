package com.stromberg.scott.seventenwouldstillsmash.activity

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

abstract class BaseActivity : AppCompatActivity() {
    abstract fun setContentShown(shown: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(null)
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok, { dialog, _ -> dialog.dismiss() })
        builder.show()
    }
}