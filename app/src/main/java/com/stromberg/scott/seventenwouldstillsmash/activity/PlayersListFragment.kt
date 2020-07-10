package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.model.PlayerStatistic
import com.stromberg.scott.seventenwouldstillsmash.util.*
import java.util.*
import kotlin.collections.HashMap

class PlayersListFragment: BaseListFragment() {
    private val db = FirebaseDatabase.getInstance()
    private val games = ArrayList<Game>()
    private val players = ArrayList<Player>()
    private val gamesForPlayers = HashMap<Player, List<Game>>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var pullToRefreshView: EasyRefreshLayout
    private lateinit var emptyStateTextView: TextView
    private lateinit var progress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.fragment_list, container, false)

        pullToRefreshView = contentView.findViewById(R.id.refresh_layout)
        recyclerView = contentView.findViewById(R.id.recycler_view)
        emptyStateTextView = contentView.findViewById(R.id.empty_state_text_view)
        progress = contentView.findViewById(R.id.progress)

        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        pullToRefreshView.loadMoreModel = LoadModel.NONE
        pullToRefreshView.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getPlayers()
            }

            override fun onLoadMore() {}
        })

        emptyStateTextView.text = getString(R.string.no_players_text)

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getPlayers()
    }

    private fun getGames() {
        db.getReference(context = activity!!)
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
            setContentShown(true)

            emptyStateTextView.visibility = if(players.size == 0) View.VISIBLE else View.GONE
        }
    }

    private fun getPlayers() {
        setContentShown(false)

        db.getReference(context = activity!!)
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

                        val adapter = PlayersListAdapter(players, playerNameWidth, HashMap())
                        recyclerView.adapter = adapter as RecyclerView.Adapter<*>

                        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
                            editPlayer(players[position])
                        }

                        adapter.setEnableLoadMore(false)

                        if(players.size > 0) {
                            getGames()
                        }
                        else {
                            setContentShown(true)

                            emptyStateTextView.visibility = View.VISIBLE
                        }
                    }
                })
    }

    override fun setContentShown(shown: Boolean) {
        progress.visibility = if(shown) View.GONE else View.VISIBLE
        pullToRefreshView.visibility = if(shown) View.VISIBLE else View.GONE
    }

    override fun fabClicked() {
        createPlayer()
    }

    private fun calculateWinRates() {
        val playerStats = HashMap<Player, List<PlayerStatistic>>()

        gamesForPlayers.forEach { player, games ->
            val allGamesWonCount = games.count { it.players.any { it.player!!.id == player.id && it.winner } }
            val last30Games = games.sortedByDescending { it.date }.take(30)
            val last30GamesWonCount = last30Games.count { it.players.any { it.player!!.id == player.id && it.winner } }

            val stats = ArrayList<PlayerStatistic>()
            stats.add(PlayerStatistic().also {
                it.gamesPlayed = games.size
                it.gamesWon = allGamesWonCount
                it.player = player
                it.is30GameStat = false
            })

            stats.add(PlayerStatistic().also {
                it.gamesPlayed = last30Games.size
                it.gamesWon = last30GamesWonCount
                it.player = player
                it.is30GameStat = true
            })

            playerStats[player] = stats
        }

        (recyclerView.adapter as PlayersListAdapter).setPlayerStats(playerStats)
        recyclerView.adapter?.notifyDataSetChanged()
        pullToRefreshView.refreshComplete()
        setContentShown(true)
        emptyStateTextView.visibility = View.GONE
    }

    private fun createPlayer() {
        val intent = Intent(activity, PlayerActivity::class.java)
        startActivity(intent)
    }

    private fun editPlayer(player: Player) {
        val intent = Intent(activity, PlayerActivity::class.java)
        intent.putExtra("player", player)
        startActivity(intent)
    }
}
