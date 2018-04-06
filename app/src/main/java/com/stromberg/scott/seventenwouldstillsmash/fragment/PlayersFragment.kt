package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.activity.MainActivity
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.PlayersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import java.util.*

class PlayersFragment : BaseFragment() {
    private val db = FirebaseDatabase.getInstance()
    private val games = ArrayList<Game>()
    private val players = ArrayList<Player>()
    private val gamesForPlayers = HashMap<Player, List<Game>>()

    private var contentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var pullToRefreshView: EasyRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity as Context?, R.layout.fragment_players, null)

        pullToRefreshView = contentView!!.findViewById(R.id.players_pull_to_refresh)
        recyclerView = contentView!!.findViewById<RecyclerView>(R.id.players_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getPlayers()
            }

            override fun onLoadMore() {}
        })

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getPlayers()
    }

    private fun getGames() {
        db.getReference(activity)
            .child("games")
            .orderByKey()
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) {}

                override fun onDataChange(snapshot: DataSnapshot?) {
                    games.clear()

                    snapshot?.children?.reversed()?.forEach {
                        val game: Game = it.getValue(Game::class.java)!!
                        game.id = it.key
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

                gamesForPlayers.put(it, gamesForPlayer)
            }

            calculateWinRates()
        }
    }

    private fun getPlayers() {
        setContentShown(false)

        db.getReference(activity)
            .child("players")
            .orderByKey()
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    players.clear()

                    snapshot?.children?.reversed()?.forEach {
                        val player: Player = it.getValue(Player::class.java)!!
                        player.id = it.key
                        players.add(player)
                    }

                    players.sortBy { it.name }

                    val playerNameWidth = getLongestNameLength(players)

                    val adapter = PlayersListAdapter(players, playerNameWidth)
                    recyclerView!!.adapter = adapter

                    adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
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

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -30)
            val thirtyDaysAgo = calendar.timeInMillis

            val games30Days = it.value.filter { it.date >= thirtyDaysAgo }
            val royaleGames30DaysCount = games30Days.count { it.gameType.equals(GameType.ROYALE.toString()) }
            val suddenDeathGames30DaysCount = games30Days.count { it.gameType.equals(GameType.SUDDEN_DEATH.toString()) }
            val royaleGames30DaysWon: Float = (games30Days.count { it.players.any { it.player!!.id == playerId && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
            val royaleGames30DaysLost: Float = royaleGames30DaysCount - royaleGames30DaysWon
            val suddenDeathGames30DaysWon: Float = (games30Days.count { it.players.any { it.player!!.id == playerId && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()
            val suddenDeathGames30DaysLost: Float = suddenDeathGames30DaysCount - suddenDeathGames30DaysWon

            val prefs = activity.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "all_time_games_won", royaleGamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "all_time_games_lost", royaleGamesLost).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "all_time_games_won", suddenDeathGamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "all_time_games_lost", suddenDeathGamesLost).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "30_day_games_won", royaleGames30DaysWon).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "30_day_games_lost", royaleGames30DaysLost).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "30_day_games_won", suddenDeathGames30DaysWon).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "30_day_games_lost", suddenDeathGames30DaysLost).apply()
        }

        recyclerView?.adapter?.notifyDataSetChanged()
        pullToRefreshView!!.refreshComplete()
        setContentShown(true)
    }

    fun getLongestNameLength(players: ArrayList<Player>): Int {
        val paint = Paint()
        val bounds = Rect()

        var longestLength = 0

        paint.typeface = TypefaceUtils.load(resources.assets, "Quicksand-Bold.ttf")
        paint.textSize = resources.getDimension(R.dimen.player_list_player_name)

        players.forEach({player -> run {
            paint.getTextBounds(player.name, 0, player.name!!.length, bounds)

            if(bounds.width() > longestLength) {
                longestLength = bounds.width()
            }
        }})

        Log.d("name", longestLength.toString())

        val oneThirdDisplayWidth = (Resources.getSystem().displayMetrics.widthPixels / 3)
        if(longestLength > oneThirdDisplayWidth) {
            return oneThirdDisplayWidth
        }
        else {
            return (longestLength * 1.15).toInt()
        }
    }

    override fun addFabClicked() {
        (activity as MainActivity).createPlayer()
    }

    private fun editPlayer(player: Player) {
        (activity as MainActivity).editPlayer(player)
    }

    override fun hasFab(): Boolean {
        return true
    }
}