package com.stromberg.scott.seventenwouldstillsmash

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.fragment.BaseFragment
import com.stromberg.scott.seventenwouldstillsmash.fragment.CreateGameFragment
import com.stromberg.scott.seventenwouldstillsmash.fragment.GamesFragment
import com.stromberg.scott.seventenwouldstillsmash.fragment.PlayersFragment
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
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
            R.id.navigation_statistics -> {
//                mCurrentFragment = null
            }
            R.id.navigation_settings -> {
//                mCurrentFragment = null
            }
            else -> hideFabs()
        }

        return@OnNavigationItemSelectedListener true
    }

    private fun navigateToFragment(fragment: BaseFragment) {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAddFabMenu = findViewById(R.id.add_fab_menu)
        mAddFabButton = findViewById(R.id.add_fab_button)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_games
        navigation.itemTextColor = ColorStateList.valueOf(resources.getColor(R.color.text_secondary))

        updateStatistics()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun createGame() {
        navigateToFragment(CreateGameFragment())
    }

    fun editGame(game: Game) {
        val bundle = Bundle()
        bundle.putParcelable("Game", game)

        val fragment = CreateGameFragment()
        fragment.arguments = bundle

        navigateToFragment(fragment)
    }

    override fun onBackPressed() {
        if(mCurrentFragment != null) {
            when(mCurrentFragment) {
                is CreateGameFragment -> navigateToFragment(GamesFragment())
                else -> super.onBackPressed()
            }
        }
        else {
            super.onBackPressed()
        }
    }

    private fun updateStatistics() {
        /* todo: save the last game id, pull th emost recent game. if they match, no stats changes (probably)
        allow force refresh?
        include last refreshed date
         */

        Toast.makeText(this@MainActivity, "Updating statistics", Toast.LENGTH_SHORT).show()

        Thread({
            val db = FirebaseDatabase.getInstance()

            val players = ArrayList<Player>()
            getPlayers(db, players)
        }).start()
    }

    private fun getPlayers(db: FirebaseDatabase, players: ArrayList<Player>) {
        db.reference
            .child("players")
            .orderByKey()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {}

                override fun onDataChange(snapshot: DataSnapshot?) {
                    snapshot?.children?.reversed()?.forEach {
                        val player: Player = it.getValue(Player::class.java)!!
                        player.id = it.key
                        players.add(player)
                    }

                    val games = ArrayList<Game>()
                    getGames(db, games, players)
                }
            })
    }

    private fun getGames(db: FirebaseDatabase, games: ArrayList<Game>, players: ArrayList<Player>) {
        db.reference
            .child("games")
            .orderByKey()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {}

                override fun onDataChange(snapshot: DataSnapshot?) {
                    snapshot?.children?.reversed()?.forEach {
                        val game: Game = it.getValue(Game::class.java)!!
                        game.id = it.key
                        games.add(game)
                    }
                }
            })
    }

    private fun gameDataFetched(games: ArrayList<Game>, players: ArrayList<Player>) {
        if(games.size > 0 && players.size > 0) {
            calculateWinRates(games, players)
        }
    }

    private fun calculateWinRates(games: ArrayList<Game>, players: ArrayList<Player>) {
        players.forEach {
            var playerId = it.id

            games.forEach {
                it.players.firstOrNull {
                    it.player!!.id == playerId
                }
            }
        }
    }
}
