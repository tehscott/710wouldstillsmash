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
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import kotlinx.android.synthetic.main.activity_list.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import java.util.*

class CharactersListFragment: BaseListFragment() {
    private var db = FirebaseDatabase.getInstance()

    private var recyclerView: RecyclerView? = null
    private var pullToRefreshView: EasyRefreshLayout? = null
    private lateinit var emptyStateTextView: TextView
    private lateinit var progress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.activity_list, container, false)

        pullToRefreshView = contentView.findViewById(R.id.refresh_layout)
        recyclerView = contentView.findViewById(R.id.recycler_view)
        emptyStateTextView = contentView.findViewById(R.id.empty_state_text_view)
        progress = contentView.findViewById(R.id.progress)

        recyclerView!!.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView!!.addOnChildAttachStateChangeListener(object: RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                MaterialShowcaseView.Builder(activity)
                        .singleUse("CharactersListTooltip")
                        .setTarget(view)
                        .setDismissText("GOT IT")
                        .setContentText(R.string.view_character_tooltip)
                        .setDismissOnTouch(true)
                        .withRectangleShape(true)
                        .show()
            }

            override fun onChildViewDetachedFromWindow(view: View) {}
        })

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
        val intent = Intent(activity, CharacterActivity::class.java)
        intent.putExtra("characterId", characterId)
        startActivity(intent)
    }

    override fun setContentShown(shown: Boolean) {
        pullToRefreshView?.isRefreshing = !shown
    }

    override fun fabClicked() {}
}
