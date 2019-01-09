package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
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
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import java.util.*

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
            readyToShowTooltips = true
            showTooltips()
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

                        val adapter = PlayersListAdapter(players, playerNameWidth)
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
                            readyToShowTooltips = true
                            showTooltips()
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

            val prefs = activity!!.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "all_time_games_won", royaleGamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "all_time_games_lost", royaleGamesLost).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "all_time_games_won", suddenDeathGamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "all_time_games_lost", suddenDeathGamesLost).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "30_games_won", royaleGames30GamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.ROYALE.toString() + "30_games_lost", royaleGames30GamesLost).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "30_games_won", suddenDeathGames30GamesWon).apply()
            prefs.edit().putFloat(playerId + GameType.SUDDEN_DEATH.toString() + "30_games_lost", suddenDeathGames30GamesLost).apply()
        }

        recyclerView.adapter?.notifyDataSetChanged()
        pullToRefreshView.refreshComplete()
        setContentShown(true)
        emptyStateTextView.visibility = View.GONE

        readyToShowTooltips = true
        showTooltips()
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

    override fun showTooltips() {
        if(readyToShowTooltips && hasFragmentBeenShown) {
            val firstView = recyclerView.getChildAt(0)

            (activity as MainActivity).queueTooltip(MaterialShowcaseView.Builder(activity)
                    .setTarget(activity!!.findViewById(R.id.fab))
                    .singleUse("AddPlayerTooltip")
                    .setDismissText(getString(R.string.tooltip_next))
                    .setContentText(R.string.add_player_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            if (firstView != null) {
//                val recyclerViewPadding = 4.toPx
//                val listItemMargin = 4.toPx

                (activity as MainActivity).queueTooltip(MaterialShowcaseView.Builder(activity)
                        .setTarget(firstView)
                        .singleUse("EditPlayerTooltip")
                        .setDismissText(getString(R.string.tooltip_next))
                        .setContentText(R.string.edit_player_tooltip)
                        .setDismissOnTouch(true)
                        .withRectangleShape(true)
//                        .setOffset(0, activity!!.findViewById<View>(R.id.top_app_bar).measuredHeight + AndroidUtil.getStatusBarHeight(activity!!) + recyclerViewPadding + listItemMargin)
                        .build())
            }
        }
    }
}
