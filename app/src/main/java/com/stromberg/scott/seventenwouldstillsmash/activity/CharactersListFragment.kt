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
import com.stromberg.scott.seventenwouldstillsmash.adapter.CharactersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Characters
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import java.util.*
import kotlin.Comparator

class CharactersListFragment: BaseListFragment() {
    private var db = FirebaseDatabase.getInstance()

    private var recyclerView: RecyclerView? = null
    private var pullToRefreshView: EasyRefreshLayout? = null
    private lateinit var emptyStateTextView: TextView
    private lateinit var progress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.fragment_list, container, false)

        pullToRefreshView = contentView.findViewById(R.id.refresh_layout)
        recyclerView = contentView.findViewById(R.id.recycler_view)
        emptyStateTextView = contentView.findViewById(R.id.empty_state_text_view)
        progress = contentView.findViewById(R.id.progress)

        recyclerView!!.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object : EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                getGames()
            }

            override fun onLoadMore() {}
        })

        emptyStateTextView.visibility = View.GONE

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getGames()
    }

    private fun getGames() {
        setContentShown(false)

        db.getReference(context = activity!!)
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
        val gamesForCharacters = HashMap<Characters, List<Game>>()

        snapshot.children.reversed().forEach {
            val game: Game = it.getValue(Game::class.java)!!
            game.id = it.key!!
            games.add(game)
        }

        Characters.values().forEach { character ->
            val gamesForCharacter = games.filter { game ->
                game.players.any { it.characterId == character.id }
            }

            gamesForCharacters[character] = gamesForCharacter
        }

        val sortedGames = gamesForCharacters.toSortedMap(Comparator { o1, o2 -> o1?.characterName.orEmpty().compareTo(o2?.characterName.orEmpty()) })

        val adapter = CharactersListAdapter(sortedGames)
        recyclerView!!.adapter = adapter
        adapter.setEnableLoadMore(false)

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            viewCharacter(sortedGames.keys.toList()[position].id)
        }

        recyclerView?.adapter?.notifyDataSetChanged()
        pullToRefreshView!!.refreshComplete()
        setContentShown(true)
    }

    private fun viewCharacter(characterId: Int) {
        val intent = Intent(activity, CharacterActivity::class.java)
        intent.putExtra("characterId", characterId)
        startActivity(intent)
    }

    override fun setContentShown(shown: Boolean) {
        progress.visibility = if(shown) View.GONE else View.VISIBLE
        pullToRefreshView?.visibility = if(shown) View.VISIBLE else View.GONE
    }

    override fun fabClicked() {}
}
