package com.stromberg.scott.seventenwouldstillsmash.fragment

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import com.ajguan.library.EasyRefreshLayout
import com.ajguan.library.LoadModel
import com.chad.library.adapter.base.BaseQuickAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.stromberg.scott.seventenwouldstillsmash.activity.MainActivity
import com.stromberg.scott.seventenwouldstillsmash.R
import com.stromberg.scott.seventenwouldstillsmash.adapter.CharactersListAdapter
import com.stromberg.scott.seventenwouldstillsmash.model.Game
import com.stromberg.scott.seventenwouldstillsmash.model.GameType
import com.stromberg.scott.seventenwouldstillsmash.util.getReference
import java.util.*

class CharactersFragment : BaseFragment() {
    private var db = FirebaseDatabase.getInstance()

    private var contentView: View? = null
    private var recyclerView: RecyclerView? = null
    private var pullToRefreshView: EasyRefreshLayout? = null
    private lateinit var sortBySpinner: Spinner

    enum class SortBy {
        NAME,
        POPULARITY,
        WIN_RATE_ROYALE,
        WIN_RATE_SUDDEN_DEATH;

        companion object {
            fun fromInt(int: Int): SortBy {
                return when(int) {
                    1 -> POPULARITY
                    2 -> WIN_RATE_ROYALE
                    3 -> WIN_RATE_SUDDEN_DEATH
                    else -> NAME
                }
            }
        }

        fun asInt(): Int {
            return when(this) {
                POPULARITY -> 1
                WIN_RATE_ROYALE -> 2
                WIN_RATE_SUDDEN_DEATH -> 3
                else -> 0
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contentView = View.inflate(activity as Context?, R.layout.fragment_players, null)

        pullToRefreshView = contentView!!.findViewById(R.id.players_pull_to_refresh)
        recyclerView = contentView!!.findViewById<RecyclerView>(R.id.players_recyclerview)
        recyclerView!!.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        pullToRefreshView!!.loadMoreModel = LoadModel.NONE
        pullToRefreshView!!.addEasyEvent(object: EasyRefreshLayout.EasyEvent {
            override fun onRefreshing() {
                val prefs = context.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
                val sortBy = prefs.getInt("SortCharactersBy", 0)

                getGames(SortBy.fromInt(sortBy))
            }

            override fun onLoadMore() {}
        })

        sortBySpinner = contentView!!.findViewById(R.id.sort_by_spinner)
        sortBySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                getGames(SortBy.fromInt(position))
            }
            override fun onNothingSelected(arg0: AdapterView<*>) {}
        }

        val prefs = context.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val sortBy = prefs.getInt("SortCharactersBy", 0)
        sortBySpinner.setSelection(sortBy)

        return contentView
    }

    override fun onResume() {
        super.onResume()

        val prefs = context.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        val sortBy = prefs.getInt("SortCharactersBy", 0)

        getGames(SortBy.fromInt(sortBy))
    }

    private fun getGames(sortBy: SortBy) {
        val prefs = context.getSharedPreferences(getString(R.string.shared_prefs_key), Context.MODE_PRIVATE)
        prefs.edit().putInt("SortCharactersBy", sortBy.asInt()).apply()

        setContentShown(false)

        db.getReference(activity)
            .child("games")
            .addListenerForSingleValueEvent( object : ValueEventListener {
                override fun onCancelled(error: DatabaseError?) { }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    handleSnapshot(snapshot, sortBy)
                }
            })
    }

    fun handleSnapshot(snapshot: DataSnapshot?, sortBy: SortBy) {
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

        var characterIds = ArrayList(gamesForCharacters.keys)

        when(sortBy) {
            SortBy.NAME -> characterIds.sortBy { it }
            SortBy.POPULARITY -> characterIds.sortByDescending { gamesForCharacters[it]?.size }
            SortBy.WIN_RATE_ROYALE -> characterIds.sortByDescending { calculateWinRate(it, gamesForCharacters[it], GameType.ROYALE) }
            SortBy.WIN_RATE_SUDDEN_DEATH -> characterIds.sortByDescending { calculateWinRate(it, gamesForCharacters[it], GameType.SUDDEN_DEATH) }
        }

        val adapter = CharactersListAdapter(characterIds, gamesForCharacters)
        recyclerView!!.adapter = adapter
        adapter.setEnableLoadMore(false)

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            (activity as MainActivity).viewCharacter(characterIds[position])
        }

        recyclerView?.adapter?.notifyDataSetChanged()

        pullToRefreshView!!.refreshComplete()
        setContentShown(true)
    }

    override fun setContentShown(shown: Boolean) {
        pullToRefreshView?.isRefreshing = !shown
    }

    private fun calculateWinRate(characterId: Int, games: List<Game>?, gameType: GameType): Float {
        if(games != null) {
            when (gameType) {
                GameType.ROYALE -> {
                    val royaleGamesPlayed: Float = (games.count { it.players.any { it.characterId == characterId } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()
                    val royaleGamesWon: Float = (games.count { it.players.any { it.characterId == characterId && it.winner } && it.gameType!!.equals(GameType.ROYALE.toString(), true) }).toFloat()

                    return if (royaleGamesPlayed > 0) royaleGamesWon / royaleGamesPlayed else 0f
                }
                GameType.SUDDEN_DEATH -> {
                    val suddenDeathGamesPlayed: Float = (games.count { it.players.any { it.characterId == characterId } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()
                    val suddenDeathGamesWon: Float = (games.count { it.players.any { it.characterId == characterId && it.winner } && it.gameType!!.equals(GameType.SUDDEN_DEATH.toString(), true) }).toFloat()

                    return if (suddenDeathGamesPlayed > 0) suddenDeathGamesWon / suddenDeathGamesPlayed else 0f
                }
            }
        }

        return 0f
    }
}