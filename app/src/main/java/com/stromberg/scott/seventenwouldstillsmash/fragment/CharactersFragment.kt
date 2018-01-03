package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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
import com.stromberg.scott.seventenwouldstillsmash.MainActivity
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.CharactersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import uk.co.chrisjenx.calligraphy.TypefaceUtils
import java.util.*

class CharactersFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()

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
                getGames()
            }

            override fun onLoadMore() {}
        })

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getGames()
    }

    private fun getGames() {
        setContentShown(false)

        db.reference
            .child("games")
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    handleSnapshot(snapshot)
                }
            })
    }

    fun handleSnapshot(snapshot: DataSnapshot?) {
        var games = ArrayList<Game>()
        val gamesForCharacters = HashMap<Int, List<Game>>()

        snapshot?.children?.reversed()?.forEach {
            val game: Game = it.getValue(Game::class.java)!!
            game.id = it.key
            games.add(game)
        }

        for(id in 0..57) {
            val gamesForCharacter = games.filter {
                it.players.any { it.characterId == id }
            }

            gamesForCharacters.put(id, gamesForCharacter)
        }

        val adapter = CharactersListAdapter(ArrayList(gamesForCharacters.keys), gamesForCharacters)
        recyclerView!!.adapter = adapter
        adapter.setEnableLoadMore(false)

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            (activity as MainActivity).viewCharacter(position)
        }

        recyclerView?.adapter?.notifyDataSetChanged()

        pullToRefreshView!!.refreshComplete()
        setContentShown(true)
    }

    override fun setContentShown(shown: Boolean) {
        pullToRefreshView?.isRefreshing = !shown
    }

    fun getLongestNameLength(names: ArrayList<String>): Int {
        val paint = Paint()
        val bounds = Rect()

        var longestLength = 0

        paint.typeface = TypefaceUtils.load(resources.assets, "Quicksand-Bold.ttf")
        paint.textSize = resources.getDimension(R.dimen.player_list_player_name)

        names.forEach({name -> run {
            paint.getTextBounds(name, 0, name.length, bounds)

            if(bounds.width() > longestLength) {
                longestLength = bounds.width()
            }
        }})

        val oneThirdDisplayWidth = (Resources.getSystem().displayMetrics.widthPixels / 3)
        if(longestLength > oneThirdDisplayWidth) {
            return oneThirdDisplayWidth
        }
        else {
            return (longestLength * 1.15).toInt()
        }
    }
}