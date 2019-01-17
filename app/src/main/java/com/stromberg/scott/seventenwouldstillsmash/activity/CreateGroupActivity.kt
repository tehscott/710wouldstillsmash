package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.GameTypeListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import com.stromberg.scott.seventenwouldstillsmash.model.GroupType
import com.stromberg.scott.seventenwouldstillsmash.util.GameTypeHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import com.stromberg.scott.seventenwouldstillsmash.util.setBackgroundColor
import com.stromberg.scott.seventenwouldstillsmash.util.setTextAttributes
import java.util.*
import kotlin.collections.ArrayList

class CreateGroupActivity : BaseActivity() {
    private var db = FirebaseDatabase.getInstance()

    private lateinit var groupCodeTextView: TextView
    private lateinit var createGroupButton: Button
    private lateinit var ssb4RadioButton: RadioButton
    private lateinit var ssbUltimateRadioButton: RadioButton

    private var code: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        groupCodeTextView = findViewById(R.id.create_group_code_text_view)
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

        createGroupButton.setOnClickListener { createGroup() }
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
        group.type = if (ssb4RadioButton.isChecked) GroupType.SMASH4 else GroupType.ULTIMATE
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

                    createGameType()
                } else {
                    setContentShown(true)

                    showDialog("Failed to create a Group. Try again.")
                }
            }
    }

    private fun createGameType() {
        val gameType = GameType(Calendar.getInstance().timeInMillis.toString(), "FFA", "ic_royale", false)

        db.getReference(context = this)
            .child("gameTypes")
            .child(gameType.id)
            .setValue(gameType)
            .addOnCompleteListener {
                startActivity(Intent(this@CreateGroupActivity, MainActivity::class.java))
            }
            .addOnFailureListener {
                startActivity(Intent(this@CreateGroupActivity, MainActivity::class.java))
            }
    }

    override fun setContentShown(shown: Boolean) {
        runOnUiThread {
            if (shown) {
                findViewById<View>(R.id.progress).visibility = View.GONE
                findViewById<View>(R.id.content).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.progress).visibility = View.VISIBLE
                findViewById<View>(R.id.content).visibility = View.GONE
            }
        }
    }
}
