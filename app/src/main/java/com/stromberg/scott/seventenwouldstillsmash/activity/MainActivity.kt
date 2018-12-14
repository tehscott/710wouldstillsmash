package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.stromberg.scott.seventenwouldstillsmash.BuildConfig
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.ListPagerAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Group
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewPagerAdapter: ListPagerAdapter
    private val tooltipQueue = LinkedList<MaterialShowcaseView>()

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

                getCurrentFragment().hasFragmentBeenShown = true
                getCurrentFragment().showTooltips()
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
        setSelectedButton(0)
        getCurrentFragment().hasFragmentBeenShown = true
        getCurrentFragment().showTooltips()

        fab.setOnClickListener {
            getCurrentFragment().fabClicked()
        }

        version_text.text = "v${BuildConfig.VERSION_NAME}"
    }

    override fun onResume() {
        super.onResume()

        showTooltips()
    }

    private fun getCurrentFragment(): BaseListFragment {
        return ((view_pager.adapter!! as FragmentPagerAdapter).getItem(view_pager.currentItem) as BaseListFragment)
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
            queueTooltip(MaterialShowcaseView.Builder(this)
                    .setTarget(games_button)
                    .singleUse("GamesButtonTooltip")
                    .setDismissText(getString(R.string.tooltip_next))
                    .setContentText(R.string.games_button_tooltip)
                    .setDismissOnTouch(true)
                    .setShapePadding(8)
                    .build())

            queueTooltip(MaterialShowcaseView.Builder(this)
                    .setTarget(players_button)
                    .singleUse("PlayersButtonTooltip")
                    .setDismissText(getString(R.string.tooltip_next))
                    .setContentText(R.string.players_button_tooltip)
                    .setDismissOnTouch(true)
                    .setShapePadding(8)
                    .build())

            queueTooltip(MaterialShowcaseView.Builder(this)
                    .setTarget(characters_button)
                    .singleUse("CharactersButtonTooltip")
                    .setDismissText(getString(R.string.tooltip_next))
                    .setContentText(R.string.characters_button_tooltip)
                    .setDismissOnTouch(true)
                    .setShapePadding(8)
                    .build())

            queueTooltip(MaterialShowcaseView.Builder(this)
                    .setTarget(group_code)
                    .singleUse("GroupCodeTooltip")
                    .setDismissText(getString(R.string.tooltip_next))
                    .setContentText(R.string.group_code_tooltip)
                    .setDismissOnTouch(true)
                    .withRoundedRectangleShape(24, 24)
                    .setShapePadding(24)
                    .build())
        }
    }

    fun queueTooltip(tooltip: MaterialShowcaseView) {
        tooltip.addShowcaseListener(object: IShowcaseListener {
            override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView?) {}

            override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView?) {
                Log.d("Tooltip", "Dismissing tooltip: " + showcaseView?.showcaseId)
                tooltipQueue.remove()

                if(tooltipQueue.isNotEmpty()) {
                    Log.d("Tooltip", "Showing tooltip: " + tooltipQueue.peek().showcaseId)
                    tooltipQueue.peek().show(this@MainActivity)
                }
            }
        })

        if(tooltipQueue.firstOrNull { it.showcaseId == tooltip.showcaseId } == null) {
            Log.d("Tooltip", "Queueing tooltip: " + tooltip.showcaseId)
            tooltipQueue.add(tooltip)
        }

        if(tooltipQueue.size == 1) {
            Log.d("Tooltip", "Showing tooltip: " + tooltip.showcaseId)
            tooltipQueue.peek().show(this)
        }
    }
}
