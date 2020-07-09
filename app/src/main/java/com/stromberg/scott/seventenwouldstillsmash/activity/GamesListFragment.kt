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
import com.stromberg.scott.seventenwouldstillsmash.adapter.GamesListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.Player
import com.stromberg.scott.seventenwouldstillsmash.util.*
import java.util.*

class GamesListFragment : BaseListFragment() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var adapter: GamesListAdapter? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: EasyRefreshLayout
    private lateinit var emptyStateTextView: TextView
    private lateinit var progress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.fragment_list, container, false)

        recyclerView = contentView.findViewById(R.id.recycler_view)
        refreshLayout = contentView.findViewById(R.id.refresh_layout)
        emptyStateTextView = contentView.findViewById(R.id.empty_state_text_view)
        progress = contentView.findViewById(R.id.progress)

        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        setupAdapter(games)

        refreshLayout.loadMoreModel = LoadModel.NONE
        refreshLayout.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getGames()
            }

            override fun onLoadMore() {}
        })

        emptyStateTextView.text = getString(R.string.no_games_text)

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getGames()
    }

    private fun setupAdapter(games: List<Game>) {
        val allNames = HashSet<String>()
        games.forEach { allNames.addAll(it.players.map { it.player!!.name!! }) }
        var loserContainerWidth = PlayerHelper.getLongestNameLength(resources, "Quicksand-Light.ttf", resources.getDimension(R.dimen.loser_name_text_size), allNames.toList())
        loserContainerWidth += (resources.getDimensionPixelSize(R.dimen.loser_image_margin_size) * 2) + resources.getDimensionPixelSize(R.dimen.loser_image_size)

        adapter = GamesListAdapter(games, GamesListAdapter.SortBy.WINNER, loserContainerWidth)

        adapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            editGame(games[position], games)
        }

        recyclerView.adapter = adapter as RecyclerView.Adapter<*>
    }

    private fun createGame(games: ArrayList<Game>) {
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
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(game.players.map { it.player!! }, games))
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
            var game: Game = it.getValue(Game::class.java)!!
            game.id = it.key!!
            games.add(game)
        }

        setupAdapter(games)
        recyclerView.adapter?.notifyDataSetChanged()

        refreshLayout.refreshComplete()

        adapter!!.loadMoreComplete()

        emptyStateTextView.visibility = if(games.size == 0) View.VISIBLE else View.GONE

        setContentShown(true)
    }

    override fun setContentShown(shown: Boolean) {
        progress.visibility = if(shown) View.GONE else View.VISIBLE
        refreshLayout.visibility = if(shown) View.VISIBLE else View.GONE
    }

    override fun fabClicked() {
        createGame(games)
    }
}
