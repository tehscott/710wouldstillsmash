package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.TextPaint
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
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import java.util.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig


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

        empty_state_text_view.text = getString(R.string.no_games_text)

        showTooltips()
    }

    override fun onResume() {
        super.onResume()

        getGames()
    }

    private fun showTooltips() {
        group_code.post {
            val config = ShowcaseConfig()
            config.fadeDuration = 50L

            val sequence = MaterialShowcaseSequence(this, "GamesListTooltip")
            sequence.setConfig(config)

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(games_button)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.games_button_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(players_button)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.players_button_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(characters_button)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.characters_button_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(fab)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.add_game_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(group_code)
                    .setDismissText("GOT IT")
                    .setContentText(R.string.group_code_tooltip)
                    .setDismissOnTouch(true)
                    .build())

            sequence.start()
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

        empty_state_text_view.visibility = if(games.size == 0) View.VISIBLE else View.GONE

        setContentShown(true)
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        pullToRefreshView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }
}
