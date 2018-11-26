package com.stromberg.scott.seventenwouldstillsmash.activity

import android.content.Intent
import android.os.Bundle
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
import com.stromberg.scott.seventenwouldstillsmash.adapter.CharactersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import kotlinx.android.synthetic.main.activity_list.*
import java.util.*

class CharactersListActivity : BaseListActivity() {
    private var db = FirebaseDatabase.getInstance()

    private var recyclerView: RecyclerView? = null
    private var pullToRefreshView: EasyRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        pullToRefreshView = findViewById(R.id.refresh_layout)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView!!.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object : EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getGames()
            }

            override fun onLoadMore() {}
        })

        fab.hide()
    }

    override fun onResume() {
        super.onResume()

        getGames()
    }

    private fun getGames() {
        setContentShown(false)

        db.getReference(context = this)
                .child("games")
                .addListenerForSingleValueEvent( object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) { }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        handleSnapshot(snapshot)
                    }
                })
    }

    fun handleSnapshot(snapshot: DataSnapshot) {
        var games = ArrayList<Game>()
        val gamesForCharacters = HashMap<Int, List<Game>>()

        snapshot.children.reversed().forEach {
            val game: Game = it.getValue(Game::class.java)!!
            game.id = it.key!!
            games.add(game)
        }

        for(id in 0..CharacterHelper.getNumberOfCharacters()) {
            val gamesForCharacter = games.filter { game ->
                game.players.any { it.characterId == id }
            }

            gamesForCharacters[id] = gamesForCharacter
        }

        val characterIds = ArrayList(gamesForCharacters.keys)
        characterIds.sortBy { it }

        val adapter = CharactersListAdapter(characterIds, gamesForCharacters)
        recyclerView!!.adapter = adapter
        adapter.setEnableLoadMore(false)

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            viewCharacter(characterIds[position])
        }

        recyclerView?.adapter?.notifyDataSetChanged()

        pullToRefreshView!!.refreshComplete()
        setContentShown(true)
    }

    private fun viewCharacter(characterId: Int) {
        val intent = Intent(this, CharacterActivity::class.java)
        intent.putExtra("characterId", characterId)
        startActivity(intent)
    }

    override fun setContentShown(shown: Boolean) {
        pullToRefreshView?.isRefreshing = !shown
    }
}
