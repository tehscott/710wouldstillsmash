package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.MainActivity
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.GamesListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.util.CharacterHelper
import java.util.*

class CharacterFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()
    private var games = ArrayList<Game>()
    private var gamesAdapter: GamesListAdapter? = null

    private var contentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var characterImage: ImageView? = null

    private var characterId: Int = -1

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity, R.layout.character, null)

        if(arguments != null && arguments.containsKey("characterId")) {
            characterId = arguments.getInt("characterId")
        }

        recyclerView = contentView!!.findViewById(R.id.character_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        progressBar = contentView!!.findViewById(R.id.progress)

        setupGamesAdapter(games)

        characterImage = contentView!!.findViewById(R.id.character_image)
        characterImage?.setImageResource(CharacterHelper.getImage(characterId))

        characterImage!!.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                characterImage!!.viewTreeObserver.removeOnPreDrawListener(this)

                var layoutParams = characterImage!!.layoutParams
                layoutParams.width = characterImage!!.height
                characterImage!!.layoutParams = layoutParams

                return false
            }
        })

        return contentView
    }

    override fun onResume() {
        super.onResume()

        getGames()
    }

    private fun setupGamesAdapter(games: List<Game>) {
        gamesAdapter = GamesListAdapter(games)

        gamesAdapter!!.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            (activity as MainActivity).editGame(games[position])
        }

        recyclerView!!.adapter = gamesAdapter
    }

    private fun getGames() {
        setContentShown(false)

        db.reference
            .child("games")
            .orderByChild("date")
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    snapshot?.children?.reversed()?.forEach {
                        var game: Game = it.getValue(Game::class.java)!!

                        if(game.players.any({ it.characterId == characterId })) {
                            game.id = it.key
                            games.add(game)
                        }
                    }

                    gamesAdapter!!.loadMoreComplete()

                    recyclerView?.adapter?.notifyDataSetChanged()

                    setContentShown(true)
                }
            })
    }

    override fun setContentShown(shown: Boolean) {
        progressBar!!.visibility = if(shown) View.GONE else View.VISIBLE
        recyclerView!!.visibility = if(shown) View.VISIBLE else View.GONE
    }
}