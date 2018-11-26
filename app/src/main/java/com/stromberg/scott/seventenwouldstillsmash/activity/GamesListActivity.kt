package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.github.florent37.viewtooltip.ViewTooltip
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
import kotlinx.android.synthetic.main.activity_list.*
import java.util.*

class GamesListActivity : BaseListActivity() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var adapter: GamesListAdapter? = null

    private var pullToRefreshView: EasyRefreshLayout? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_list)

        pullToRefreshView = findViewById(R.id.refresh_layout)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView!!.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        progressBar = findViewById(R.id.progress)

        fab.setOnClickListener {
            createGame(games)
        }

        setupAdapter(games)

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getGames()
            }

            override fun onLoadMore() {}
        })
    }

    override fun onResume() {
        super.onResume()

        getGames()
    }

    override fun onStart() {
        super.onStart()

        val prefs = getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)

        if(!prefs.getBoolean("ShowedGroupTooltip", false)) {
            val groupCode = findViewById<TextView>(R.id.group_code)

            ViewTooltip
                    .on(this, groupCode)
                    .autoHide(true, 10000)
                    .clickToHide(true)
                    .corner(30)
                    .padding(30, 30, 30, 30)
                    .arrowHeight(32)
                    .position(ViewTooltip.Position.LEFT)
                    .text(R.string.group_code_tooltip)
                    .textSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                    .show()

            prefs.edit().putBoolean("ShowedGroupTooltip", true).apply()
        }
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

        recyclerView!!.adapter = adapter
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

        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(allPlayers, games))
        startActivity(intent)
    }

    private fun editGame(game: Game, games: List<Game>) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("Game", game)
        intent.putExtra("TopCharacters", CharacterHelper.getTopCharacters(game.players.map { it.player!! }, games))
        startActivity(intent)
    }

    private fun getGames() {
        setContentShown(false)

        db.getReference(context = this)
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
        recyclerView?.adapter?.notifyDataSetChanged()

        pullToRefreshView!!.refreshComplete()

        adapter!!.loadMoreComplete()

        setContentShown(true)
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        pullToRefreshView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }
}
