package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.PlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.*
import kotlinx.android.synthetic.main.activity_list.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import java.util.*

class PlayersListActivity : BaseListActivity() {
    private val db = FirebaseDatabase.getInstance()
    private val games = ArrayList<Game>()
    private val players = ArrayList<Player>()
    private val gamesForPlayers = HashMap<Player, List<Game>>()

    private var recyclerView: RecyclerView? = null
    private var pullToRefreshView: EasyRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        pullToRefreshView = findViewById(R.id.refresh_layout)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView!!.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getPlayers()
            }

            override fun onLoadMore() {}
        })

        fab.setOnClickListener {
            createPlayer()
        }

        empty_state_text_view.text = getString(R.string.no_players_text)
    }

    override fun onResume() {
        super.onResume()

        getPlayers()
    }

    private fun getGames() {
        db.getReference(context = this)
                .child("games")
                .orderByKey()
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}

                    override fun onDataChange(snapshot: DataSnapshot) {
                        games.clear()

                        snapshot.children.reversed().forEach {
                            val game: Game = it.getValue(Game::class.java)!!
                            game.id = it.key!!
                            games.add(game)
                        }

                        gameDataFetched()
                    }
                })
    }

    private fun gameDataFetched() {
        if(games.size > 0 && players.size > 0) {
            players.forEach {
                val playerId = it.id

                val gamesForPlayer = games.filter {
                    it.players.any { it.player!!.id == playerId }
                }

                gamesForPlayers[it] = gamesForPlayer
            }

            calculateWinRates()
        }
        else {
            empty_state_text_view.visibility = if(players.size == 0) View.VISIBLE else View.GONE
            showTooltips()
        }
    }

    private fun getPlayers() {
        setContentShown(false)

        db.getReference(context = this)
                .child("players")
                .orderByKey()
                .addListenerForSingleValueEvent( object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) { }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        players.clear()

                        snapshot.children.reversed().forEach {
                            val player: Player = it.getValue(Player::class.java)!!
                            player.id = it.key
                            players.add(player)
                        }

                        players.sortBy { it.name }
                        players.sortBy { it.isLowPriority }
                        players.sortBy { it.isHidden }

                        val playerNameWidth = PlayerHelper.getLongestNameLength(resources, "Quicksand-Bold.ttf", resources.getDimension(R.dimen.player_list_player_name), players.map { it.name })

                        val adapter = PlayersListAdapter(players, playerNameWidth)
                        recyclerView!!.adapter = adapter

                        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
                            editPlayer(players[position])
                        }

                        adapter.setEnableLoadMore(false)

                        getGames()
                    }
                })
    }

    override fun setContentShown(shown: Boolean) {
        pullToRefreshView?.isRefreshing = !shown
    }

    private fun calculateWinRates() {
        gamesForPlayers.forEach {
            val playerId = it.key.id

            val royaleGamesCount = it.value.count { it.gameType.equals(GameType.ROYALE.toString()) }
            val suddenDeathGamesCount = it.value.count { it.gameType.equals(GameType.SUDDEN_DEATH.toString()) }
            val royaleGamesWon: Float = (it.value.count { it.players.any { it.player!!.id == playerId && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
            val royaleGamesLost: Float = royaleGamesCount - royaleGamesWon
            val suddenDeathGamesWon: Float = (it.value.count { it.players.any { it.player!!.id == playerId && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()
            val suddenDeathGamesLost: Float = suddenDeathGamesCount - suddenDeathGamesWon

            val last30RoyaleGames = it.value.filter { it.gameType.equals(GameType.ROYALE.toString()) }.sortedByDescending { it.date }.take(30)
            val last30SuddenDeathGames = it.value.filter { it.gameType.equals(GameType.SUDDEN_DEATH.toString()) }.sortedByDescending { it.date }.take(30)
            val royaleGames30GamesCount = last30RoyaleGames.count { it.gameType.equals(GameType.ROYALE.toString()) }
            val suddenDeathGames30GamesCount = last30SuddenDeathGames.count { it.gameType.equals(GameType.SUDDEN_DEATH.toString()) }
            val royaleGames30GamesWon: Float = (last30RoyaleGames.count { it.players.any { it.player!!.id == playerId && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
            val royaleGames30GamesLost: Float = royaleGames30GamesCount - royaleGames30GamesWon
            val suddenDeathGames30GamesWon: Float = (last30SuddenDeathGames.count { it.players.any { it.player!!.id == playerId && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()
            val suddenDeathGames30GamesLost: Float = suddenDeathGames30GamesCount - suddenDeathGames30GamesWon

            val prefs = this.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "all_time_games_won", royaleGamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "all_time_games_lost", royaleGamesLost).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "all_time_games_won", suddenDeathGamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "all_time_games_lost", suddenDeathGamesLost).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "30_games_won", royaleGames30GamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "30_games_lost", royaleGames30GamesLost).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "30_games_won", suddenDeathGames30GamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "30_games_lost", suddenDeathGames30GamesLost).apply()
        }

        recyclerView?.adapter?.notifyDataSetChanged()
        pullToRefreshView!!.refreshComplete()
        setContentShown(true)

        showTooltips()
    }

    private fun createPlayer() {
        val intent = Intent(this, PlayerActivity::class.java)
        startActivity(intent)
    }

    private fun editPlayer(player: Player) {
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra("player", player)
        startActivity(intent)
    }

    private fun showTooltips() {
        val sequence = MaterialShowcaseSequence(this, "PlayersListTooltip")
        sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                .setTarget(fab)
                .setDismissText("GOT IT")
                .setContentText(R.string.add_player_tooltip)
                .setDismissOnTouch(true)
                .build())

        val firstView = recyclerView!!.getChildAt(0)
        if(firstView != null) {
            val recyclerViewPadding = 4.toPx
            val listItemMargin = 4.toPx

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(firstView)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.edit_player_tooltip)
                    .setDismissOnTouch(true)
                    .withRectangleShape(true)
                    .setOffset(0, top_app_bar.measuredHeight + AndroidUtil.getStatusBarHeight(this) + recyclerViewPadding + listItemMargin)
                    .build())
        }

        sequence.start()
    }
}
