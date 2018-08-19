package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.fragment.*
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class MainActivity : AppCompatActivity() {
    private var mLastFragment: BaseFragment? = null
    private var mCurrentFragment: BaseFragment? = null
    private var mAddFabMenu: FloatingActionMenu? = null
    private var mAddFabButton: FloatingActionButton? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_games -> {
                navigateToFragment(GamesFragment())
            }
            R.id.navigation_players -> {
                navigateToFragment(PlayersFragment())
            }
            R.id.navigation_characters -> {
                navigateToFragment(CharactersFragment())
            }
            else -> hideFabs()
        }

        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAddFabMenu = findViewById(R.id.add_fab_menu)
        mAddFabButton = findViewById(R.id.add_fab_button)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_games
        navigation.itemTextColor = ColorStateList.valueOf(resources.getColor(R.color.text_secondary))

        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        findViewById<TextView>(R.id.group_code).setText(prefs.getString(getString(R.string.shared_prefs_group_code), ""))
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun navigateToFragment(fragment: BaseFragment) {
        mLastFragment = mCurrentFragment
        mCurrentFragment = fragment

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()

        if (fragment.hasFab()) {
            val fabMenuButtons = fragment.getFabButtons(this)

            if (fabMenuButtons.isNotEmpty()) {
                mAddFabMenu?.visibility = View.VISIBLE
                mAddFabButton?.visibility = View.GONE

                mAddFabMenu?.removeAllMenuButtons()
                fabMenuButtons.forEach({ fab -> run { mAddFabMenu?.addMenuButton(fab) } })
            } else {
                mAddFabMenu?.visibility = View.GONE
                mAddFabButton?.visibility = View.VISIBLE
                mAddFabButton?.setOnClickListener { fragment.addFabClicked() }
            }
        } else {
            hideFabs()
        }
    }

    private fun hideFabs() {
        mAddFabMenu?.visibility = View.GONE
        mAddFabButton?.visibility = View.GONE
    }

    fun createGame(games: ArrayList<Game>) {
        val allPlayers = ArrayList<Player>()

        games.forEach {
            val players = it.players.map { it.player!! }

            players.forEach {
                val player = it

               if(allPlayers.find { it.id.equals(player.id) } == null) {
                   allPlayers.add(player)
               }
            }
        }

        val bundle = Bundle()
        bundle.putSerializable("TopCharacters", getTopCharacters(allPlayers, games))

        val fragment = CreateGameFragment()
        fragment.arguments = bundle

        navigateToFragment(fragment)
    }

    fun editGame(game: Game, games: List<Game>) {
        val bundle = Bundle()
        bundle.putParcelable("Game", game)
        bundle.putSerializable("TopCharacters", getTopCharacters(game.players.map { it.player!! }, games))

        val fragment = CreateGameFragment()
        fragment.arguments = bundle

        navigateToFragment(fragment)
    }

    fun createPlayer() {
        navigateToFragment(CreatePlayerFragment())
    }

    fun editPlayer(player: Player) {
        var bundle = Bundle()
        bundle.putSerializable("player", player)

        val fragment = CreatePlayerFragment()
        fragment.arguments = bundle

        navigateToFragment(fragment)
    }

    fun viewCharacter(characterId: Int) {
        var bundle = Bundle()
        bundle.putSerializable("characterId", characterId)

        val fragment = CharacterFragment()
        fragment.arguments = bundle

        navigateToFragment(fragment)
    }

    override fun onBackPressed() {
        if(mCurrentFragment != null) {
            when(mCurrentFragment) {
                is CreateGameFragment -> navigateToFragment(mLastFragment ?: GamesFragment())
                is CreatePlayerFragment -> navigateToFragment(PlayersFragment())
                is CharacterFragment -> navigateToFragment(CharactersFragment())
                else -> super.onBackPressed()
            }
        }
        else {
            super.onBackPressed()
        }
    }

    companion object {
        fun getTopCharacters(players: List<Player>, games: List<Game>): HashMap<String, ArrayList<Int>> {
            val topFiveCharacters = HashMap<String, ArrayList<Int>>()

            players.forEach {
                val gamesWithCharacters = HashMap<Int, Int>()
                val player = it
                (0..CharacterHelper.getNumberOfCharacters()).forEachIndexed { _, characterId ->
                    val numGamesWithThisCharacter = games.count { it.players.any { it.characterId == characterId && it.player!!.id == player.id } }
                    gamesWithCharacters[characterId] = numGamesWithThisCharacter
                }

                val characterIds = ArrayList<Int>()

                gamesWithCharacters.entries.sortedByDescending { it.value }.take(5).forEach {
                    characterIds.add(it.key)
                }

                topFiveCharacters[player.id!!] = characterIds
            }
            return topFiveCharacters
        }
    }
}