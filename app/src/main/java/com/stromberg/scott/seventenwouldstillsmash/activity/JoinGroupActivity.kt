package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import kotlinx.android.synthetic.main.activity_join_group.*
import java.util.*
import kotlin.collections.ArrayList

class JoinGroupActivity : AppCompatActivity() {
    private var db = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_group)

        join_group_code_edit_text.addTextChangedListener(object: TextWatcher {
            var isConvertingToUppercase = false

            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!isConvertingToUppercase) {
                    isConvertingToUppercase = true
                    join_group_code_edit_text.setText(s.toString().toUpperCase(Locale.getDefault()))
                    join_group_code_edit_text.setSelection(s?.length ?: 0)
                    isConvertingToUppercase = false

                    if(s?.length == 5) {
                        submitGroupCode(s.toString().toUpperCase(Locale.getDefault()))
                    }
                }
            }
        })

        join_group_create_group_button.setOnClickListener { createGroup(null) }

        val forceJoin = intent?.extras?.getBoolean("ForceJoin") ?: false

        if(!forceJoin) {
            val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            val groups = Gson().fromJson(prefs.getString(getString(R.string.shared_prefs_group_codes), ""), Array<Group>::class.java)?.toCollection(ArrayList())

            if (groups?.firstOrNull { it.isSelected } != null) {
//                val intent = Intent(this@JoinGroupActivity, GamesListFragment::class.java)
                val intent = Intent(this@JoinGroupActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
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
        imm.hideSoftInputFromWindow(join_group_code_edit_text.windowToken, 0)

        setContentShown(false)

        db.reference
            .child("groups")
            .child(code)
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) { }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val group: Group? = snapshot.getValue(Group::class.java)

                    if(group != null) {
                        group.isSelected = true

                        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
                        var groups = Gson().fromJson(prefs.getString(getString(R.string.shared_prefs_group_codes), ""), Array<Group>::class.java)?.toCollection(ArrayList())

                        if(groups == null) {
                            groups = ArrayList()
                        }

                        groups.forEach { it.isSelected = false }

                        groups.add(group)

                        prefs.edit().putString(getString(R.string.shared_prefs_group_codes), Gson().toJson(groups)).apply()

//                        val intent = Intent(this@JoinGroupActivity, GamesListFragment::class.java)
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
                            builder.setPositiveButton("Yes, create one") { _, _ -> createGroup(code) }
                            builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
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
