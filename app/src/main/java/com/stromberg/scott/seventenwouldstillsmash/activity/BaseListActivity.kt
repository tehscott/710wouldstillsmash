package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import kotlinx.android.synthetic.main.activity_list.*

abstract class BaseListActivity : BaseActivity() {
    override fun onStart() {
        super.onStart()

        games_button.drawable.setTint(resources.getColor(R.color.text_primary, null))
        players_button.drawable.setTint(resources.getColor(R.color.text_primary, null))
        characters_button.drawable.setTint(resources.getColor(R.color.text_primary, null))

        when(this.javaClass) {
            GamesListActivity::class.java -> games_button.drawable.setTint(resources.getColor(R.color.secondary, null))
            PlayersListActivity::class.java -> players_button.drawable.setTint(resources.getColor(R.color.secondary, null))
            CharactersListActivity::class.java -> characters_button.drawable.setTint(resources.getColor(R.color.secondary, null))
        }

        games_button.setOnClickListener {
            if(this !is GamesListActivity) {
                startActivity(Intent(this, GamesListActivity::class.java))
                finish()
            }
        }

        players_button.setOnClickListener {
            if(this !is PlayersListActivity) {
                startActivity(Intent(this, PlayersListActivity::class.java))
                finish()
            }
        }

        characters_button.setOnClickListener {
            if(this !is CharactersListActivity) {
                startActivity(Intent(this, CharactersListActivity::class.java))
                finish()
            }
        }

        setupGroupCodeText()
    }

    private fun setupGroupCodeText() {
        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        var groups = Gson().fromJson<Array<Group>>(prefs.getString(getString(R.string.shared_prefs_group_codes), ""), Array<Group>::class.java)?.toCollection(ArrayList())

        if (groups == null) {
            groups = ArrayList()
        }

        val selectedGroup = groups.firstOrNull { it.isSelected }

        val groupCode = findViewById<TextView>(R.id.group_code)
        groupCode.text = selectedGroup?.code
        groupCode.setOnClickListener {
            val allGroups = ArrayList(groups.map { "${it.code} (${it.type.shortName()})" })
            allGroups.add("Add Group")

            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setTitle("Select a group")
            dialogBuilder.setSingleChoiceItems(allGroups.toTypedArray(), groups.indexOfFirst { it.isSelected }) { dialog, index ->
                if(allGroups[index] == "Add Group") {
                    val intent = Intent(this, JoinGroupActivity::class.java)
                    intent.putExtra("ForceJoin", true)
                    startActivity(intent)
                }
                else {
                    switchGroups(groups[index])
                }

                dialog.dismiss()
            }
            val alert = dialogBuilder.create()
            alert.show()
        }
    }

    private fun switchGroups(group: Group) {
        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val groups = Gson().fromJson<Array<Group>>(prefs.getString(getString(R.string.shared_prefs_group_codes), ""), Array<Group>::class.java)?.toCollection(ArrayList())

        groups?.forEach {
            it.isSelected = it.code == group.code
        }

        prefs.edit().putString(getString(R.string.shared_prefs_group_codes), Gson().toJson(groups)).apply()

        setupGroupCodeText()

        startActivity(Intent(this, GamesListActivity::class.java))
        finish()
    }
}