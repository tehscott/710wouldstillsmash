package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.ListPagerAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig

class MainActivity : AppCompatActivity() {
    private lateinit var viewPagerAdapter: ListPagerAdapter

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPagerAdapter = ListPagerAdapter(supportFragmentManager)
        view_pager.adapter = viewPagerAdapter
        view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                setSelectedButton(position)

                when(position) {
                    0, 1 -> fab.show()
                    else -> fab.hide()
                }
            }
        })

        games_button.setOnClickListener {
            if(view_pager.currentItem != 0) {
                view_pager.setCurrentItem(0, true)
            }
        }

        players_button.setOnClickListener {
            if(view_pager.currentItem != 1) {
                view_pager.setCurrentItem(1, true)
            }
        }

        characters_button.setOnClickListener {
            if(view_pager.currentItem != 2) {
                view_pager.setCurrentItem(2, true)
            }
        }

        setupGroupCodeText()
        showTooltips()
        setSelectedButton(0)

        fab.setOnClickListener {
            ((view_pager.adapter!! as FragmentPagerAdapter).getItem(view_pager.currentItem) as BaseListFragment).fabClicked()
        }
    }

    private fun setupGroupCodeText() {
        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        var groups = Gson().fromJson<Array<Group>>(prefs.getString(getString(R.string.shared_prefs_group_codes), ""), Array<Group>::class.java)?.toCollection(ArrayList())

        if (groups == null) {
            groups = ArrayList()
        }

        val selectedGroup = groups.firstOrNull { it.isSelected }

        group_code.text = selectedGroup?.code
        group_code.setOnClickListener {
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

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setSelectedButton(currentItem: Int) {
        games_button.drawable.setTint(resources.getColor(R.color.text_primary, null))
        players_button.drawable.setTint(resources.getColor(R.color.text_primary, null))
        characters_button.drawable.setTint(resources.getColor(R.color.text_primary, null))

        when(currentItem) {
            0 -> games_button.drawable.setTint(resources.getColor(R.color.secondary, null))
            1 -> players_button.drawable.setTint(resources.getColor(R.color.secondary, null))
            2 -> characters_button.drawable.setTint(resources.getColor(R.color.secondary, null))
        }
    }

    private fun showTooltips() {
        group_code.post {
            val config = ShowcaseConfig()
            config.fadeDuration = 50L

            val sequence = MaterialShowcaseSequence(this, "GamesListTooltip")
            sequence.setConfig(config)

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(games_button)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.games_button_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(players_button)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.players_button_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(characters_button)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.characters_button_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(fab)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.add_game_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(group_code)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.group_code_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.start()
        }
    }
}
