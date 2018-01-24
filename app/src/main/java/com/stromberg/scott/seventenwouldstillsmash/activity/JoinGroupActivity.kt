package com.stromberg.scott.seventenwouldstillsmash.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import android.view.inputmethod.InputMethodManager

class JoinGroupActivity : AppCompatActivity() {
    private var db = FirebaseDatabase.getInstance()

    lateinit var groupCodeEditText: EditText
    private lateinit var createGroupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_group)

        groupCodeEditText = findViewById(R.id.join_group_code_edit_text)
        createGroupButton = findViewById(R.id.join_group_create_group_button)

        groupCodeEditText.addTextChangedListener(object: TextWatcher {
            var isConvertingToUppercase = false

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!isConvertingToUppercase) {
                    isConvertingToUppercase = true
                    groupCodeEditText.setText(s.toString().toUpperCase())
                    groupCodeEditText.setSelection(s?.length ?: 0)
                    isConvertingToUppercase = false

                    if(s?.length == 5) {
                        submitGroupCode(s.toString().toUpperCase())
                    }
                }
            }
        })

        createGroupButton.setOnClickListener { createGroup(null) }

        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val group = prefs.getString(getString(R.string.shared_prefs_group_code), null)

        if(group != null) {
            startActivity(Intent(this@JoinGroupActivity, MainActivity::class.java))
        }
    }

    private fun createGroup(code: String?) {
        val intent = Intent(this@JoinGroupActivity, CreateGroupActivity::class.java)

        if(code != null) {
            intent.putExtra("Code", code)
        }

        startActivity(intent)
    }

    private fun submitGroupCode(code: String) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(groupCodeEditText.windowToken, 0)

        setContentShown(false)

        db.reference
            .child("groups")
            .child(code)
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    val group: Group? = snapshot?.getValue(Group::class.java)

                    if(group != null) {
                        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
                        prefs.edit().putString(getString(R.string.shared_prefs_group_code), group.code).apply()
                        prefs.edit().putString(getString(R.string.shared_prefs_group_name), group.name).apply()

                        val intent = Intent(this@JoinGroupActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        setContentShown(true)

                        runOnUiThread {
                            val builder = AlertDialog.Builder(this@JoinGroupActivity)
                            builder.setTitle(null)
                            builder.setMessage("Couldn't find a Group using the code '$code'.\n\nCreate one?")
                            builder.setPositiveButton("Yes, create one", { _, _ -> createGroup(code) })
                            builder.setNegativeButton("No", { dialog, _ -> dialog.dismiss() })
                            builder.show()
                        }
                    }
                }
            })
    }

    private fun setContentShown(show: Boolean) {
        runOnUiThread {
            if (show) {
                findViewById<View>(R.id.progress).visibility = View.GONE
                findViewById<View>(R.id.content).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.progress).visibility = View.VISIBLE
                findViewById<View>(R.id.content).visibility = View.GONE
            }
        }
    }
}
