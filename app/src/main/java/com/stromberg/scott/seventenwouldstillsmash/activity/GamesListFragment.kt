package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.stromberg.scott.seventenwouldstillsmash.adapter.GamesListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.PlayerHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import kotlinx.android.synthetic.main.fragment_list.*
import java.util.*

class GamesListFragment : BaseListFragment() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var adapter: GamesListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.fragment_list, container, false)

        recycler_view.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        setupAdapter(games)

        refresh_layout.loadMoreModel = LoadModel.NONE
        refresh_layout.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getGames()
            }

            override fun onLoadMore() {}
        })

        empty_state_text_view.text = getString(R.string.no_games_text)

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getGames()
    }

    private fun setupAdapter(games: List<Game>) {
        val allNames = HashSet<String>()
        games.forEach { allNames.addAll(it.players.map { gamePlayer -> gamePlayer.player.name }) }
        var loserContainerWidth = PlayerHelper.getLongestNameLength(resources.getDimension(R.dimen.loser_name_text_size), allNames.toList())
        loserContainerWidth += (resources.getDimensionPixelSize(R.dimen.loser_image_margin_size) * 2) + resources.getDimensionPixelSize(R.dimen.loser_image_size)

        adapter = GamesListAdapter(games, GamesListAdapter.SortBy.WINNER, loserContainerWidth)

        adapter?.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            editGame(games[position], games)
        }

        recycler_view.adapter = adapter as RecyclerView.Adapter<*>
    }

    private fun createGame(games: ArrayList<Game>) {
        val allPlayers = ArrayList<Player>()

        games.forEach {
            val players = it.players.map { gamePlayer -> gamePlayer.player }

            players.forEach { player ->
                if(allPlayers.find { allPlayer -> allPlayer.id == player.id } == null) {
                    allPlayers.add(player)
                }
            }
        }

        val intent = Intent(activity, GameActivity::class.java)
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(allPlayers, games))

        games.firstOrNull()?.let {
            intent.putExtra("LastGame", it)
        }

        startActivity(intent)
    }

    private fun editGame(game: Game, games: List<Game>) {
        val intent = Intent(activity, GameActivity::class.java)
        intent.putExtra("Game", game)
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(game.players.map { it.player }, games))
        startActivity(intent)
    }

    private fun getGames() {
        setContentShown(false)

        db.getReference(context = activity!!)
            .child("games")
            .orderByChild("date")
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) { }

                override fun onDataChange(snapshot: DataSnapshot) {
                    handleSnapshot(snapshot)
                }
            })
    }

    fun handleSnapshot(snapshot: DataSnapshot) {
        games.clear()

        snapshot.children.reversed().forEach {
            val game: Game = it.getValue(Game::class.java)!!
            game.id = it.key.orEmpty()
            games.add(game)
        }

        setupAdapter(games)
        recycler_view.adapter?.notifyDataSetChanged()

        refresh_layout.refreshComplete()

        adapter?.loadMoreComplete()

        empty_state_text_view.visibility = if(games.size == 0) View.VISIBLE else View.GONE

        setContentShown(true)
    }

    override fun setContentShown(shown: Boolean) {
        progress.visibility = if(shown) View.GONE else View.VISIBLE
        refresh_layout.visibility = if(shown) View.VISIBLE else View.GONE
    }

    override fun fabClicked() {
        createGame(games)
    }
}
