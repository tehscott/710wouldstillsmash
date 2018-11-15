package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import com.stromberg.scott.seventenwouldstillsmash.util.showDialog
import java.util.*
import kotlin.collections.ArrayList

class CreateGroupActivity : AppCompatActivity() {
    private var db = FirebaseDatabase.getInstance()

    private lateinit var groupCodeTextView: TextView
    private lateinit var groupNameEditText: EditText
    private lateinit var createGroupButton: Button
    private lateinit var ssb4RadioButton: RadioButton
    private lateinit var ssbUltimateRadioButton: RadioButton

    private var code: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        groupCodeTextView = findViewById(R.id.create_group_code_text_view)
        groupNameEditText = findViewById(R.id.create_group_name_edit_text)
        createGroupButton = findViewById(R.id.create_group_create_button)
        ssb4RadioButton = findViewById(R.id.ssb_4_radio_button)
        ssbUltimateRadioButton = findViewById(R.id.ssb_ultimate_radio_button)

        if(intent?.extras?.containsKey("Code") == true) {
            code = intent.extras?.getString("Code")?.toUpperCase()
            groupCodeTextView.text = code
        }
        else {
            setContentShown(false)

            getGroupCode()
        }

        createGroupButton.setOnClickListener {
            if(groupNameEditText.text.isNotEmpty()) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(createGroupButton.windowToken, 0)

                createGroup()
            }
            else {
                showDialog("Enter a name.")
            }
        }
    }

    private fun getGroupCode() {
        val codeToTry = UUID.randomUUID().toString().substring(0, 5)

        db.reference
            .child("groups")
            .child(codeToTry)
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) { }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val group: Group? = snapshot.getValue(Group::class.java)

                    if(group != null) {
                        getGroupCode()
                    }
                    else {
                        code = codeToTry.toUpperCase()
                        groupCodeTextView.text = code

                        setContentShown(true)
                    }
                }
            })
    }

    private fun createGroup() {
        setContentShown(false)

        val group = Group()
        group.code = code
        group.name = groupNameEditText.text.toString()
        group.isSelected = true

        db.reference
            .child("groups")
            .child(code!!)
            .setValue(group)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
                    var groups = Gson().fromJson<Array<Group>>(prefs.getString(getString(R.string.shared_prefs_group_codes), ""), Array<Group>::class.java)?.toCollection(ArrayList())

                    if(groups == null) {
                        groups = ArrayList()
                    }

                    groups.forEach { it.isSelected = false }

                    groups.add(group)

                    prefs.edit().putString(getString(R.string.shared_prefs_group_codes), Gson().toJson(groups)).apply()

                    startActivity(Intent(this@CreateGroupActivity, MainActivity::class.java))
                } else {
                    setContentShown(true)

                    showDialog("Failed to create a Group. Try again.")
                }
            }
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
